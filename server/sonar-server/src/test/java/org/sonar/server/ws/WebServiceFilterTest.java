/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.ws;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.server.ws.RailsHandler;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.server.ws.WebServiceFilterTest.WsUrl.newWsUrl;

public class WebServiceFilterTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  WebServiceEngine webServiceEngine = mock(WebServiceEngine.class);

  HttpServletRequest request = mock(HttpServletRequest.class);
  HttpServletResponse response = mock(HttpServletResponse.class);
  FilterChain chain = mock(FilterChain.class);
  ServletOutputStream responseOutput = mock(ServletOutputStream.class);

  WebServiceFilter underTest;

  @Before
  public void setUp() throws Exception {
    when(request.getContextPath()).thenReturn("");
    when(response.getOutputStream()).thenReturn(responseOutput);
  }

  @Test
  public void do_get_pattern() throws Exception {
    initWebServiceEngine(
      newWsUrl("api/issues", "search"),
      newWsUrl("batch", "index"),
      newWsUrl("api/authentication", "login").setHandler(ServletFilterHandler.INSTANCE),
      newWsUrl("api/resources", "index").setHandler(RailsHandler.INSTANCE),
      newWsUrl("api/issues", "deprecatedSearch").setHandler(RailsHandler.INSTANCE));

    assertThat(underTest.doGetPattern().matches("/api/issues/search")).isTrue();
    assertThat(underTest.doGetPattern().matches("/api/issues/search.protobuf")).isTrue();
    assertThat(underTest.doGetPattern().matches("/batch/index")).isTrue();

    assertThat(underTest.doGetPattern().matches("/api/resources/index")).isFalse();
    assertThat(underTest.doGetPattern().matches("/api/authentication/login")).isFalse();
    assertThat(underTest.doGetPattern().matches("/api/issues/deprecatedSearch")).isFalse();
    assertThat(underTest.doGetPattern().matches("/api/properties")).isFalse();
    assertThat(underTest.doGetPattern().matches("/foo")).isFalse();
  }

  @Test
  public void execute_ws() throws Exception {
    underTest = new WebServiceFilter(webServiceEngine);

    underTest.doFilter(request, response, chain);

    verify(webServiceEngine).execute(any(ServletRequest.class), any(org.sonar.server.ws.ServletResponse.class));
  }

  private void initWebServiceEngine(WsUrl... wsUrls) {
    List<WebService.Controller> controllers = new ArrayList<>();

    for (WsUrl wsUrl : wsUrls) {
      String controller = wsUrl.getController();
      WebService.Controller wsController = mock(WebService.Controller.class);
      when(wsController.path()).thenReturn(controller);

      List<WebService.Action> actions = new ArrayList<>();
      for (String action : wsUrl.getActions()) {
        WebService.Action wsAction = mock(WebService.Action.class);
        when(wsAction.path()).thenReturn(controller + "/" + action);
        when(wsAction.key()).thenReturn(action);
        when(wsAction.handler()).thenReturn(wsUrl.getRequestHandler());
        actions.add(wsAction);
      }
      when(wsController.actions()).thenReturn(actions);
      controllers.add(wsController);
    }
    when(webServiceEngine.controllers()).thenReturn(controllers);
    underTest = new WebServiceFilter(webServiceEngine);
  }

  static final class WsUrl {
    private String controller;
    private String[] actions;
    private RequestHandler requestHandler = EmptyRequestHandler.INSTANCE;

    WsUrl(String controller, String... actions) {
      this.controller = controller;
      this.actions = actions;
    }

    WsUrl setHandler(RequestHandler requestHandler) {
      this.requestHandler = requestHandler;
      return this;
    }

    String getController() {
      return controller;
    }

    String[] getActions() {
      return actions;
    }

    RequestHandler getRequestHandler() {
      return requestHandler;
    }

    static WsUrl newWsUrl(String controller, String... actions) {
      return new WsUrl(controller, actions);
    }
  }

  private enum EmptyRequestHandler implements RequestHandler {
    INSTANCE;

    @Override
    public void handle(Request request, Response response) throws Exception {
      // Nothing to do
    }
  }

}
