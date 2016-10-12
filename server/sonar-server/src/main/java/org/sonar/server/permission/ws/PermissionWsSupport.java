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
package org.sonar.server.permission.ws;

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.api.resources.ResourceTypes;
import org.sonar.api.server.ws.Request;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.organization.OrganizationDto;
import org.sonar.db.permission.template.PermissionTemplateDto;
import org.sonar.db.user.UserDto;
import org.sonar.server.component.ComponentFinder;
import org.sonar.server.permission.ProjectRef;
import org.sonar.server.permission.UserId;
import org.sonar.server.permission.ws.template.WsTemplateRef;
import org.sonar.server.usergroups.ws.GroupIdOrAnyone;
import org.sonar.server.usergroups.ws.GroupWsRef;
import org.sonar.server.usergroups.ws.GroupWsSupport;
import org.sonarqube.ws.client.permission.PermissionsWsParameters;

import static org.sonar.server.ws.WsUtils.checkFound;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_GROUP_ID;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_GROUP_NAME;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_ORGANIZATION_KEY;

public class PermissionWsSupport {

  private final DbClient dbClient;
  private final ComponentFinder componentFinder;
  private final GroupWsSupport groupWsSupport;
  private final ResourceTypes resourceTypes;

  public PermissionWsSupport(DbClient dbClient, ComponentFinder componentFinder, GroupWsSupport groupWsSupport, ResourceTypes resourceTypes) {
    this.dbClient = dbClient;
    this.componentFinder = componentFinder;
    this.groupWsSupport = groupWsSupport;
    this.resourceTypes = resourceTypes;
  }

  public OrganizationDto findOrganization(DbSession dbSession, @Nullable String organizationKey) {
    return groupWsSupport.findOrganizationByKey(dbSession, organizationKey);
  }

  /**
   * @throws org.sonar.server.exceptions.NotFoundException if a project does not exist
   */
  public ProjectRef findProject(DbSession dbSession, WsProjectRef ref) {
    ComponentDto project = componentFinder.getRootComponentOrModuleByUuidOrKey(dbSession, ref.uuid(), ref.key(), resourceTypes);
    return new ProjectRef(project.getId(), project.uuid());
  }

  public Optional<ProjectRef> findProject(DbSession dbSession, Request request) {
    String uuid = request.param(PermissionsWsParameters.PARAM_PROJECT_ID);
    String key = request.param(PermissionsWsParameters.PARAM_PROJECT_KEY);
    if (uuid != null || key != null) {
      WsProjectRef ref = WsProjectRef.newWsProjectRef(uuid, key);
      return Optional.of(findProject(dbSession, ref));
    }
    return Optional.empty();
  }

  public ComponentDto getRootComponentOrModule(DbSession dbSession, WsProjectRef projectRef) {
    return componentFinder.getRootComponentOrModuleByUuidOrKey(dbSession, projectRef.uuid(), projectRef.key(), resourceTypes);
  }

  public GroupIdOrAnyone findGroup(DbSession dbSession, Request request) {
    Long groupId = request.paramAsLong(PARAM_GROUP_ID);
    String orgKey = request.param(PARAM_ORGANIZATION_KEY);
    String groupName = request.param(PARAM_GROUP_NAME);
    GroupWsRef groupRef = GroupWsRef.create(groupId, orgKey, groupName);
    return groupWsSupport.findGroupOrAnyone(dbSession, groupRef);
  }

  public UserId findUser(DbSession dbSession, String login) {
    UserDto dto = dbClient.userDao().selectActiveUserByLogin(dbSession, login);
    checkFound(dto, "User with login '%s' is not found'", login);
    return new UserId(dto.getId(), dto.getLogin());
  }

  public PermissionTemplateDto findTemplate(DbSession dbSession, WsTemplateRef ref) {
    if (ref.uuid() != null) {
      return checkFound(
        dbClient.permissionTemplateDao().selectByUuid(dbSession, ref.uuid()),
        "Permission template with id '%s' is not found", ref.uuid());
    } else {
      return checkFound(
        dbClient.permissionTemplateDao().selectByName(dbSession, ref.name()),
        "Permission template with name '%s' is not found (case insensitive)", ref.name());
    }
  }
}
