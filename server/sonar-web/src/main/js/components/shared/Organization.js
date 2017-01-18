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
// @flow
import React from 'react';
import { connect } from 'react-redux';
import { getOrganizationByKey, areThereCustomOrganizations } from '../../store/rootReducer';

type OwnProps = {
  organizationKey: string,
};

type Props = {
  organizationKey: string,
  organization: null | {
    key: string,
    name: string
  },
  shouldBeDisplayed: boolean
};

class Organization extends React.Component {
  props: Props;

  render () {
    const { organization: org, shouldBeDisplayed } = this.props;

    if (!shouldBeDisplayed || !org) {
      return null;
    }

    return (
        <span>{org.name}<span>&nbsp;&#47;&nbsp;</span></span>
    );
  }
}

const mapStateToProps = (state, ownProps: OwnProps) => ({
  organization: getOrganizationByKey(state, ownProps.organizationKey),
  shouldBeDisplayed: areThereCustomOrganizations(state)
});

export default connect(mapStateToProps)(Organization);

export const UnconnectedOrganization = Organization;
