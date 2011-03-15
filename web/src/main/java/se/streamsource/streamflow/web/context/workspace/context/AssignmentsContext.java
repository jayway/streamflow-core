/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.workspace.context;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountAssignmentsConcern.class)
@Mixins(AssignmentsContext.Mixin.class)
public interface AssignmentsContext
        extends Context
{
   public Query<Case> cases(TableQuery tableQuery);

   public void createcase();

   abstract class Mixin
           implements AssignmentsContext
   {
      public Query<Case> cases(TableQuery tableQuery)
      {
         AssignmentsQueries assignments = RoleMap.role(AssignmentsQueries.class);

         Query<Case> query = assignments.assignments(RoleMap.role(Assignee.class)).orderBy(orderBy(templateFor(CreatedOn.class).createdOn()));

         // Paging
         if (tableQuery.offset() != null)
            query.firstResult(Integer.parseInt(tableQuery.offset()));
         if (tableQuery.limit() != null)
            query.maxResults(Integer.parseInt(tableQuery.limit()));

         return query;
      }

      public void createcase()
      {
         Drafts drafts = RoleMap.role(Drafts.class);
         CaseEntity caze = drafts.createDraft();

         Owner owner = RoleMap.role(Owner.class);
         caze.changeOwner(owner);

         caze.open();

         caze.assignTo(RoleMap.role(Assignee.class));
      }
   }
}