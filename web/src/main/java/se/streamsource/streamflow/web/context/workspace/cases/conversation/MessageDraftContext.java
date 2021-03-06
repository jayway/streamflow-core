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
package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageDraft;

/**
 *
 */
public class MessageDraftContext
   implements IndexContext<StringValue>
{
   @Structure
   Module module;

   public StringValue index()
   {
      String draftMessage = RoleMap.role( MessageDraft.class ).getDraftMessage();
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
      builder.prototype().string().set( draftMessage != null ? draftMessage : ""  );
      return builder.newInstance();
   }

   @RequiresPermission(PermissionType.write)
   public void changemessage( @Optional @Name( "message" ) String message )
   {
      MessageDraft draft = RoleMap.role( MessageDraft.class );
      draft.changeDraftMessage( message );
   }
}
