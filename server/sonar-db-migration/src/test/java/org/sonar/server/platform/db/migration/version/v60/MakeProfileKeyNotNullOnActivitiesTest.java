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

package org.sonar.server.platform.db.migration.version.v60;

import java.sql.SQLException;
import java.sql.Types;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.System2;
import org.sonar.db.DbTester;

public class MakeProfileKeyNotNullOnActivitiesTest {

  private static final String TABLE_ACTIVITIES = "activities";

  @Rule
  public DbTester db = DbTester.createForSchema(System2.INSTANCE, MakeProfileKeyNotNullOnActivitiesTest.class, "activities.sql");
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private MakeProfileKeyNotNullOnActivities underTest = new MakeProfileKeyNotNullOnActivities(db.database());

  @Test
  public void migration_sets_uuid_column_not_nullable_on_empty_table() throws SQLException {
    underTest.execute();

    verifyColumnDefinitions();
  }

  @Test
  public void migration_sets_uuid_column_not_nullable_on_populated_table() throws SQLException {
    insertActivity(true);
    insertActivity(true);

    underTest.execute();

    verifyColumnDefinitions();
  }

  @Test
  public void migration_fails_if_some_row_has_a_null_profile_key() throws SQLException {
    insertActivity(false);

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Fail to execute");

    underTest.execute();
  }

  private void verifyColumnDefinitions() {
    db.assertColumnDefinition(TABLE_ACTIVITIES, "profile_key", Types.VARCHAR, 255, false);
  }

  private void insertActivity(boolean hasProfileKey) {
    db.executeInsert(
      TABLE_ACTIVITIES,
      "user_login", "login",
      "profile_key", hasProfileKey ? "my_profile_key" : null);
  }

}
