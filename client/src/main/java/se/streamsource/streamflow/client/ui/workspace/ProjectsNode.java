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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectClientResource;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class ProjectsNode
        extends DefaultMutableTreeNode
    implements Refreshable
{
    private AccountModel account;
    private ObjectBuilderFactory obf;

    public ProjectsNode(@Uses AccountModel account,
                              @Structure final ObjectBuilderFactory obf) throws Exception
    {
        super(account);
        this.account = account;
        this.obf = obf;

        refresh();
    }

    @Override
    public WorkspaceNode getParent()
    {
        return (WorkspaceNode) super.getParent();
    }

    public void refresh()
            throws Exception
    {
        se.streamsource.streamflow.client.resource.users.UserClientResource user = account.userResource();
        ListValue projects = user.workspace().projects().listProjects();

        super.removeAllChildren();

        for (ListItemValue project : projects.items().get())
        {
            WorkspaceProjectClientResource workspaceProjectResource =  user.workspace().projects().project(project.entity().get().identity());
            add(obf.newObjectBuilder(ProjectNode.class).use(workspaceProjectResource, project.description().get()).newInstance());
        }
    }
}