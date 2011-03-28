/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.resource.user.profile.PerspectiveValue;
import se.streamsource.streamflow.web.domain.interaction.profile.Perspectives;
import se.streamsource.streamflow.web.domain.structure.user.profile.Perspective;

/**
 * JAVADOC
 */
public class PerspectivesContext
      implements IndexContext<Iterable<Perspective>>, CreateContext<PerspectiveValue>
{
   @Structure
   Module module;

   public Iterable<Perspective> index()
   {
      Perspectives.Data searches = RoleMap.role( Perspectives.Data.class );
      return searches.perspectives();
   }

   public void create( PerspectiveValue perspective )
   {
      Perspectives perspectives = RoleMap.role( Perspectives.class );
      perspectives.createPerspective( perspective );
   }
}