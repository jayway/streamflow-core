/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.task.TaskValue;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContext;

import java.text.SimpleDateFormat;

/**
 * JAVADOC
 */
@Mixins(TaskContext.Mixin.class)
public interface TaskContext
   extends Context, TaskActionsContext
{
   TaskValue info();

   @SubContext
   TaskGeneralContext general();

   @SubContext
   TaskContactsContext contacts();

   @SubContext
   TaskFormsContext forms();

   abstract class Mixin
      extends ContextMixin
      implements TaskContext
   {
      public static TaskValue taskDTO(TaskEntity task, Module module)
      {
         ValueBuilder<TaskValue> builder = module.valueBuilderFactory().newValueBuilder( TaskValue.class );

         TaskValue prototype = builder.prototype();

         prototype.id().set( task.identity().get() );
         prototype.creationDate().set( task.createdOn().get() );
         if (task.createdBy().get() != null)
            prototype.createdBy().set( ((Describable)task.createdBy().get()).getDescription() );
         if (task.taskId().get() != null)
            prototype.taskId().set( task.taskId().get() );
         prototype.href().set( task.identity().get()+"/" );
         prototype.rel().set( "task" );
         prototype.owner().set( ((Describable)task.owner().get()).getDescription() );
         prototype.status().set( task.status().get() );
         prototype.text().set( task.description().get() );

         if (task.taskType().get() != null)
            prototype.taskType().set( task.taskType().get().getDescription() );

         if (task.isAssigned())
            prototype.assignedTo().set( ((Describable)task.assignedTo().get()).getDescription() );

         // Delegation
         if (task.isDelegated())
         {
            prototype.delegatedFrom().set( ((Describable)task.delegatedFrom().get()).getDescription() );
            prototype.delegatedTo().set( ((Describable)task.delegatedTo().get()).getDescription() );
            prototype.delegatedOn().set( task.delegatedOn().get());
         }

         // Labels
         LinksBuilder labelsBuilder = new LinksBuilder(module.valueBuilderFactory()).path( "labels" ).command( "delete" );
         for (Label label : task.labels())
         {
            labelsBuilder.addDescribable( label );
         }
         prototype.labels().set( labelsBuilder.newLinks() );

         return builder.newInstance();
      }

      public TaskValue info()
      {
         return taskDTO(context.role( TaskEntity.class ), module);
      }

      public TaskGeneralContext general()
      {
         return subContext( TaskGeneralContext.class );
      }

      public TaskContactsContext contacts()
      {
         return subContext( TaskContactsContext.class );
      }

      public TaskFormsContext forms()
      {
         return subContext( TaskFormsContext.class );
      }
   }
}
