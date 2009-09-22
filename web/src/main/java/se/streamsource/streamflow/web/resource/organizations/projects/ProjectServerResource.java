/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.resource.organizations.projects;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.project.*;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.security.AccessControlException;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}
 */
public class ProjectServerResource
        extends CommandQueryServerResource
{
    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public EntityReferenceDTO findRole(StringDTO query)
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);

        try
        {
            ProjectRoles.ProjectRolesState roles = uowf.currentUnitOfWork().get(ProjectRoles.ProjectRolesState.class, getRequest().getAttributes().get("organization").toString());
            for (ProjectRole projectRole : roles.projectRoles())
            {
                if (projectRole.getDescription().equals(query.string().get()))
                {
                    builder.prototype().entity().set(EntityReference.getEntityReference(projectRole));
                }
            }
        } catch (NoSuchEntityException e)
        {
        }
        return builder.newInstance();
    }

    public void describe(StringDTO stringValue) throws ResourceException
    {
        String projectId = (String) getRequest().getAttributes().get("project");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, projectId);

        String identity = getRequest().getAttributes().get("organization").toString();

        Projects.ProjectsState projects = uowf.currentUnitOfWork().get(Projects.ProjectsState.class, identity);

        String newName = stringValue.string().get();

        for (Project project : projects.projects())
        {
            if (project.hasDescription(newName))
            {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT);
            }
        }

        checkPermission(describable);
        describable.describe(newName);
    }

    public void deleteOperation() throws ResourceException
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        String org = getRequest().getAttributes().get("organization").toString();

        Projects projects = uow.get(Projects.class, org);

        String identity = getRequest().getAttributes().get("project").toString();
        ProjectEntity projectEntity = uow.get(ProjectEntity.class, identity);

        try
        {
            checkPermission(projects);
            projects.removeProject(projectEntity);
        } catch(AccessControlException ae)
        {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }
    }

}