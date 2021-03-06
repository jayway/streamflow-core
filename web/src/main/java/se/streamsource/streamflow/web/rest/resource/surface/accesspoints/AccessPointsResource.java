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
package se.streamsource.streamflow.web.rest.resource.surface.accesspoints;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.surface.accesspoints.AccessPointsContext;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;

/**
 * JAVADOC
 */
public class AccessPointsResource
      extends CommandQueryResource
      implements SubResources
{
   public AccessPointsResource()
   {
      super( AccessPointsContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      ProxyUser proxyUser = RoleMap.role( ProxyUser.class );

      AccessPoints.Data data = (AccessPoints.Data) proxyUser.organization().get();

      SelectedForms.Data ap = (SelectedForms.Data) findManyAssociation( data.accessPoints(), segment );
      if (ap.selectedForms().count() == 0)
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND ); // Not valid AccessPoint

      subResource( AccessPointResource.class );
   }
}