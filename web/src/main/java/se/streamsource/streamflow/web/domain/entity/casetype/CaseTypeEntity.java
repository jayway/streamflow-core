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

package se.streamsource.streamflow.web.domain.entity.casetype;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolutions;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedResolutions;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * JAVADOC
 */
@Concerns(CaseTypeEntity.RemovableConcern.class)
public interface CaseTypeEntity
      extends DomainEntity,

      // Structure
      CaseType,
      CaseAccessDefaults.Data,
      Describable.Data,
      Notable.Data,
      Labels.Data,
      Ownable.Data,
      SelectedLabels.Data,
      Forms.Data,
      SelectedForms.Data,
      Resolutions.Data,
      SelectedResolutions.Data
{
   abstract class RemovableConcern
      extends ConcernOf<Removable>
      implements Removable
   {
      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      CaseType caseType;

      public boolean removeEntity()
      {
         boolean removed = next.removeEntity();

         // Remove all usages of this case-type
         if (removed)
         {
            {
               SelectedCaseTypes.Data selectedCaseTypes = QueryExpressions.templateFor( SelectedCaseTypes.Data.class );
               Query<SelectedCaseTypes> caseTypeUsages = qbf.newQueryBuilder( SelectedCaseTypes.class ).
                     where( QueryExpressions.contains(selectedCaseTypes.selectedCaseTypes(), caseType )).
                     newQuery( uowf.currentUnitOfWork() );

               for (SelectedCaseTypes caseTypeUsage : caseTypeUsages)
               {
                  caseTypeUsage.removeSelectedCaseType( caseType );
               }
            }
         }

         return removed;
      }

      public void deleteEntity()
      {
         next.deleteEntity();
      }
   }
}

