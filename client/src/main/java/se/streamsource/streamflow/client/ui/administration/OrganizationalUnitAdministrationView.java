/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.injection.scope.Service;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import se.streamsource.streamflow.client.ui.administration.groups.GroupAdminView;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectAdminView;
import se.streamsource.streamflow.client.ui.administration.roles.RolesView;

import javax.swing.JList;
import javax.swing.JTabbedPane;

/**
 * JAVADOC
 */
public class OrganizationalUnitAdministrationView
        extends JTabbedPane
{
    public OrganizationalUnitAdministrationView(@Service ProjectAdminView projectAdmin,
                                                @Service GroupAdminView groupAdmin,
                                                @Service RolesView rolesAdmin)
    {
        addTab(text(AdministrationResources.projects_tab), projectAdmin);
        addTab(text(AdministrationResources.groups_tab), groupAdmin);
        addTab(text(AdministrationResources.roles_tab), rolesAdmin);
        addTab(text(AdministrationResources.metadata_tab), new JList());
    }
}
