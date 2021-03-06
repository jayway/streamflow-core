/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.entity.gtd;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.isNull;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

@Mixins(InboxQueries.Mixin.class)
public interface InboxQueries
        extends AbstractCaseQueriesFilter
{
   QueryBuilder<Case> inbox(@Optional String filter);

   boolean inboxHasActiveCases();

   abstract class Mixin
           implements InboxQueries
   {
      @Structure
      Module module;

      @This
      Owner owner;

      public QueryBuilder<Case> inbox(String filter)
      {
         // Find all Open cases with specific owner which have not yet been assigned
         QueryBuilder<Case> queryBuilder = module.queryBuilderFactory().newQueryBuilder(Case.class);
         Association<Owner> ownableId = templateFor(Ownable.Data.class).owner();
         Association<Assignee> assignee = templateFor(Assignable.Data.class).assignedTo();
         queryBuilder = queryBuilder.where(and(
                 eq(templateFor(Status.Data.class).status(), CaseStates.OPEN),
                 eq( templateFor(Removable.Data.class).removed(), Boolean.FALSE ),
                 eq(ownableId, owner),
                 isNull(assignee)
         ));

         if (!Strings.empty(filter))
            queryBuilder = applyFilter(queryBuilder, filter);

         return queryBuilder;
      }

      public boolean inboxHasActiveCases()
      {
         return inbox("").newQuery(module.unitOfWorkFactory().currentUnitOfWork()).count() > 0;
      }

   }
}
