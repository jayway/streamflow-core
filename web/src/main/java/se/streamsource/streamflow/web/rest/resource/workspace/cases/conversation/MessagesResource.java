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
package se.streamsource.streamflow.web.rest.resource.workspace.cases.conversation;

import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 *
 *
 */
public class MessagesResource
      extends CommandQueryResource
      implements SubResources
{
   public MessagesResource()
   {
      super( MessagesContext.class );
   }

   public void resource( String segment ) throws ContextNotFoundException
   {
      if( "messagedraft".equals( segment ))
      {
         subResource( MessageDraftResource.class );
      } else
      {
         findManyAssociation( RoleMap.role( Messages.Data.class ).messages(), segment);
         subResource( MessageResource.class );
      }
   }
}