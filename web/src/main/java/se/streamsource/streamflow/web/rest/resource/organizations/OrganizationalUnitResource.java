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
package se.streamsource.streamflow.web.rest.resource.organizations;

import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitContext;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;
import se.streamsource.streamflow.web.rest.resource.organizations.forms.FormsResource;

/**
 * JAVADOC
 */
@RequiresPermission(PermissionType.administrator)
public class OrganizationalUnitResource
      extends CommandQueryResource
{
   public OrganizationalUnitResource()
   {
      super( OrganizationalUnitContext.class, OrganizationalUnitsContext.class, DescribableContext.class );
   }
   
   @SubResource
   public void administrators( )
   {
      subResource( AdministratorsResource.class );
   }

   @SubResource @ServiceAvailable( service = LdapImporterService.class, availability = false )
   public void groups()
   {
      subResource( GroupsResource.class );
   }

   @SubResource
   public void projects()
   {
      subResource( ProjectsResource.class );
   }

   @SubResource
   public void forms()
   {
      subResource( FormsResource.class );
   }

   @SubResource
   public void casetypes()
   {
      subResource( CaseTypesResource.class );
   }

   @SubResource
   public void labels()
   {
      subResource( LabelsResource.class );
   }

   @SubResource
   public void selectedlabels()
   {
      subResource( SelectedLabelsResource.class );
   }
   
}
