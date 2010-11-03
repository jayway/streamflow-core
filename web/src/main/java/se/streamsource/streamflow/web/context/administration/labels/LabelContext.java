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

package se.streamsource.streamflow.web.context.administration.labels;

import org.qi4j.api.query.Query;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class LabelContext
      implements DeleteContext
{
   public Query<SelectedLabels> usages()
   {
      return role( Labels.class ).usages( role( Label.class ) );
   }

   public void delete()
   {
      Labels labels = role( Labels.class );
      Label label = role( Label.class );

      // Remove selections
      for (SelectedLabels selectedLabels : usages())
      {
         selectedLabels.removeSelectedLabel( label );
      }

      labels.removeLabel( label );
   }
}