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

package se.streamsource.streamflow.web.resource.users.shared.projects.assignments;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.application.shared.inbox.NewSharedTaskCommand;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.assignment.AssignmentsTaskListDTO;
import se.streamsource.streamflow.resource.inbox.TasksQuery;
import se.streamsource.streamflow.web.domain.task.*;
import se.streamsource.streamflow.web.domain.user.UserEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /users/{user}/shared/projects/{project}/assignments
 */
public class SharedProjectAssignmentsServerResource
        extends CommandQueryServerResource
{
    public AssignmentsTaskListDTO tasks(TasksQuery query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String projectId = (String) getRequest().getAttributes().get("project");
        String userId    = (String) getRequest().getAttributes().get("user");

        // Find all Active tasks owned by "project" and assigned to "user"
        QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
        Property<String> assignedToidProp = templateFor(Assignable.AssignableState.class).assignedTo().get().identity();
        Property<String> ownerIdProp = templateFor(Ownable.OwnableState.class).owner().get().identity();
        queryBuilder.where(and(
                eq(ownerIdProp, projectId),
                eq(assignedToidProp, userId),
                eq(templateFor(TaskStatus.TaskStatusState.class).status(), TaskStates.ACTIVE)));

        Query<TaskEntity> assignmentsQuery = queryBuilder.newQuery(uow);
        assignmentsQuery.orderBy(orderBy(templateFor(CreatedOn.CreatedOnState.class).createdOn()));

        ValueBuilder<AssignedTaskDTO> builder = vbf.newValueBuilder(AssignedTaskDTO.class);
        AssignedTaskDTO prototype = builder.prototype();
        ValueBuilder<AssignmentsTaskListDTO> listBuilder = vbf.newValueBuilder(AssignmentsTaskListDTO.class);
        List<AssignedTaskDTO> list = listBuilder.prototype().tasks().get();
        EntityReference ref = EntityReference.parseEntityReference(projectId);
        for (TaskEntity task : assignmentsQuery)
        {
            prototype.owner().set(ref);
            prototype.task().set(EntityReference.getEntityReference(task));
            prototype.creationDate().set(task.createdOn().get());
            prototype.description().set(task.description().get());
            prototype.status().set(task.status().get());
            list.add(builder.newInstance());
        }

        return listBuilder.newInstance();
    }

    public void newtask(NewSharedTaskCommand command)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("project");
        UserEntity user = uow.get(UserEntity.class, id);

        EntityBuilder<TaskEntity> builder = uow.newEntityBuilder(TaskEntity.class);
        TaskEntity prototype = builder.prototype();
        prototype.description().set(command.description().get());
        prototype.note().set(command.note().get());

        // Check if subtask
        if (command.parentTask().get() != null)
        {
            TaskPath path = uow.get(TaskPath.class, command.parentTask().get().identity());

            // Add parents path first, then parent itself
            for (Task task : path.getPath())
            {
                prototype.path().add(prototype.path().count(), task);
            }
            prototype.path().add(prototype.path().count(), (Task) path);
        }

        TaskEntity task = builder.newInstance();
        user.receiveTask(task);
    }
}