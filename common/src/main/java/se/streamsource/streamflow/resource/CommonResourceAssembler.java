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

package se.streamsource.streamflow.resource;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.dci.value.ValueAssembler;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;
import se.streamsource.streamflow.infrastructure.application.LinkTree;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.caze.*;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.resource.organization.SelectedTemplatesValue;
import se.streamsource.streamflow.resource.overview.ProjectSummaryValue;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;
import se.streamsource.streamflow.resource.user.*;
import se.streamsource.streamflow.resource.user.profile.PerspectiveValue;

/**
 * JAVADOC
 */
public class CommonResourceAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      // Commands
      module.addValues( RegisterUserCommand.class,
            ChangePasswordCommand.class,
            BooleanDTO.class,
            DateDTO.class,
            IntegerDTO.class,
            NamedIndexDTO.class,
            NewUserCommand.class,
            NewProxyUserCommand.class).visibleIn( Visibility.application );

      new ValueAssembler().assemble( module );

      // Result values
      module.addValues(
            AccessPointValue.class,
            ListItemValue.class,
            CaseDTO.class,
            CaseGeneralDTO.class,
            CaseFormDTO.class,
            ContactsDTO.class,
            EndUserCaseDTO.class,
            ConversationDTO.class,
            MessageDTO.class,
            LinkTree.class,
            ProjectSummaryValue.class,
            FieldDTO.class,
            EffectiveFieldsDTO.class,
            EffectiveFieldDTO.class,
            SubmittedFormDTO.class,
            SubmittedFormListDTO.class,
            SubmittedFormsListDTO.class,
            UserEntityValue.class,
            ProxyUserListDTO.class,
            ProxyUserDTO.class,
            PerspectiveValue.class,
            SelectedTemplatesValue.class,
            CaseOutputConfigValue.class).visibleIn( Visibility.application );
   }
}
