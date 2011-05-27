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

package se.streamsource.streamflow.domain;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import se.streamsource.streamflow.domain.attachment.AttachmentAssembler;
import se.streamsource.streamflow.domain.contact.ContactAssembler;
import se.streamsource.streamflow.domain.form.FormAssembler;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.domain.organization.EmailAccessPointValue;

/**
 * JAVADOC
 */
public class CommonDomainAssembler
{
   public void assemble( LayerAssembly domainLayer ) throws AssemblyException
   {
      new ContactAssembler().assemble( domainLayer.module( "Contact" ) );
      new FormAssembler().assemble( domainLayer.module( "Form" ) );
      new AttachmentAssembler().assemble( domainLayer.module( "Attachment" ) );

      domainLayer.module("Organization").values(EmailAccessPointValue.class).visibleIn(Visibility.application);

      domainLayer.module( "Case" ).values( Actions.class ).visibleIn( Visibility.application );
   }
}