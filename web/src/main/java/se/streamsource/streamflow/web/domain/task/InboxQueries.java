/*
* Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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
package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.web.domain.label.Label;

import java.util.List;

@Mixins(InboxQueries.Mixin.class)
public interface InboxQueries
{
   TaskListDTO inboxTasks();

   boolean inboxHasActiveTasks();

   class Mixin
         implements InboxQueries
   {

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Identity id;

      @This
      Inbox.Data inbox;

      public TaskListDTO inboxTasks()
      {
         // Find all Active tasks with specific owner which have not yet been assigned
         QueryBuilder<TaskEntity> queryBuilder = qbf.newQueryBuilder( TaskEntity.class );
         Property<String> ownableId = templateFor( Ownable.Data.class ).owner().get().identity();
         Association<Assignee> assignee = templateFor( Assignable.Data.class ).assignedTo();
         Association<Delegatee> delegatee = templateFor( Delegatable.Data.class ).delegatedTo();
         Query<TaskEntity> inboxQuery = queryBuilder.where( and(
               eq( ownableId, id.identity().get() ),
               isNull( assignee ),
               isNull( delegatee ),
               eq( templateFor( TaskStatus.Data.class ).status(), TaskStates.ACTIVE ) ) ).
               newQuery( uowf.currentUnitOfWork() );

         inboxQuery.orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );

         return buildTaskList( inboxQuery, InboxTaskDTO.class);
      }

      public boolean inboxHasActiveTasks()
      {
         return inboxTasks().tasks().get().size() > 0;
      }

      protected <V extends TaskDTO> TaskListDTO buildTaskList(
            Query<TaskEntity> inboxQuery,
            Class<V> taskClass)
      {
         ValueBuilder<V> builder = vbf.newValueBuilder( taskClass );
         TaskDTO prototype = builder.prototype();
         ValueBuilder<TaskListDTO> listBuilder = vbf.newValueBuilder( TaskListDTO.class );
         TaskListDTO t = listBuilder.prototype();
         Property<List<TaskDTO>> property = t.tasks();
         List<TaskDTO> list = property.get();
         ValueBuilder<ListItemValue> labelBuilder = vbf.newValueBuilder( ListItemValue.class );
         ListItemValue labelPrototype = labelBuilder.prototype();
         for (TaskEntity task : inboxQuery)
         {
            buildTask( prototype, labelBuilder, labelPrototype, task );

            list.add( builder.newInstance() );
         }
         return listBuilder.newInstance();
      }

      protected <T extends TaskListDTO> void buildTask( TaskDTO prototype, ValueBuilder<ListItemValue> labelBuilder, ListItemValue labelPrototype, TaskEntity task )
      {
         prototype.task().set( EntityReference.getEntityReference( task ) );

         if (task.taskType().get() != null)
            prototype.taskType().set( task.taskType().get().getDescription() );
         else
            prototype.taskType().set( null );

         prototype.creationDate().set( task.createdOn().get() );
         prototype.description().set( task.description().get() );
         prototype.status().set( task.status().get() );
         prototype.isRead().set( !inbox.unreadInboxTasks().contains( task ) );

         ValueBuilder<ListValue> labelListBuilder = vbf.newValueBuilder( ListValue.class );
         List<ListItemValue> labelList = labelListBuilder.prototype().items().get();
         for (Label label : task.labels())
         {
            labelPrototype.entity().set( EntityReference.getEntityReference( label ) );
            labelPrototype.description().set( label.getDescription() );
            labelList.add( labelBuilder.newInstance() );
         }
         prototype.labels().set( labelListBuilder.newInstance() );
      }
   }
}
