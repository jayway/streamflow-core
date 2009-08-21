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

package se.streamsource.streamflow.client.resource.users.workspace.projects;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class WorkspaceProjectsClientResource
        extends CommandQueryClientResource
{
    public WorkspaceProjectsClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public WorkspaceProjectClientResource project(String project)
    {
        return getSubResource(project, WorkspaceProjectClientResource.class);
    }

    public ListValue listProjects() throws ResourceException
    {
        return query("listProjects", ListValue.class);        
    }

    public List<WorkspaceProjectClientResource> projects() throws ResourceException
    {
        List<WorkspaceProjectClientResource> projectResources = new ArrayList<WorkspaceProjectClientResource>();
        ListValue projects = listProjects();
        for (ListItemValue project : projects.items().get())
        {
            WorkspaceProjectClientResource workspaceProjectResource =  project(project.entity().get().identity());
            projectResources.add(workspaceProjectResource);
        }
        return projectResources;
    }
}