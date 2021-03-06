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
package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;

import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;

/**
 * JAVADOC
 */
@Mixins(AccessPointsAdministrationContext.Mixin.class)
public interface AccessPointsAdministrationContext
      extends IndexContext<LinksValue>, Context
{
   public void createaccesspoint( @MaxLength(50) @Name("name") String name );

   abstract class Mixin
         implements AccessPointsAdministrationContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         AccessPoints.Data data = role( AccessPoints.Data.class );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         linksBuilder.rel("accesspoint").addDescribables( data.accessPoints() );

         return linksBuilder.newLinks();
      }

      public void createaccesspoint( String name )
      {
         AccessPoints accessPoints = role( AccessPoints.class );
         accessPoints.createAccessPoint( name );
      }
   }
}