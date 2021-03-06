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

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.administration.CaseAccessDefaultsContext;
import se.streamsource.streamflow.web.context.administration.ProjectContext;
import se.streamsource.streamflow.web.context.administration.RequiresCaseTypContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.rest.resource.organizations.filters.FiltersResource;
import se.streamsource.streamflow.web.rest.resource.organizations.forms.FormsResource;

/**
 * JAVADOC
 */
public class ProjectResource
      extends CommandQueryResource
{
   public ProjectResource()
   {
      super( ProjectContext.class, DescribableContext.class );
   }

   @SubResource
   public void members( )
   {
      subResource( MembersResource.class );
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

   @SubResource
   public void selectedcasetypes()
   {
      subResource( SelectedCaseTypesResource.class );
   }

   @SubResource
   public void caseaccessdefaults()
   {
      subResourceContexts( CaseAccessDefaultsContext.class );
   }

   @SubResource
   public void filters()
   {
      subResource(FiltersResource.class);
   }

   @SubResource
   public void dueonnotification()
   {
      subResource(DueOnNotificationSettingsResource.class);
   }

   @SubResource
    public void requirescasetype()
   {
       subResourceContexts(RequiresCaseTypContext.class);
   }
}
