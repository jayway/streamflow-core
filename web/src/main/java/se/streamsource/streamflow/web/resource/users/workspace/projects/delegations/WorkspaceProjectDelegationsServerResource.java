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

package se.streamsource.streamflow.web.resource.users.workspace.projects.delegations;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.DelegationsQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * Mapped to:
 * /users/{user}/workspace/projects/{project}/delegations
 */
public class WorkspaceProjectDelegationsServerResource
      extends AbstractTaskListServerResource
{
/*
   public TaskListDTO tasks( TasksQuery query )
   {
      UnitOfWork uow = uowf.currentUnitOfWork();
      String id = (String) getRequest().getAttributes().get( "project" );

      DelegationsQueries delegations = uow.get( DelegationsQueries.class, id );

      return delegations.delegations();
   }
*/

   @Override
   protected void buildTask( TaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task )
   {
      ((DelegatedTaskDTO) prototype).delegatedOn().set( task.delegatedOn().get() );
      Owner owner = task.owner().get();
      ((DelegatedTaskDTO) prototype).delegatedFrom().set( ((Describable)owner).getDescription() );

      super.buildTask( prototype, labelBuilder, labelPrototype, task );
   }
}
