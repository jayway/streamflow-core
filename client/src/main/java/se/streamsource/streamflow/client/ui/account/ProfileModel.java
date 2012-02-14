/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.account;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Observable;

/**
 * JAVADOC
 */
public class ProfileModel
      extends Observable
   implements Refreshable
{
   @Structure
   Module module;

   @Uses
   private CommandQueryClient client;

   private ContactDTO contact;

   public void changeMessageDeliveryType( String newDeliveryType )
         throws ResourceException
   {
      Form form = new Form();
      form.set("messagedeliverytype", newDeliveryType);
      client.putCommand( "changemessagedeliverytype", form.getWebRepresentation() );
   }

   public String getMessageDeliveryType()
   {
      return client.query( "messagedeliverytype", StringValue.class )
            .string().get();
   }

   // Contact Details

   public void refresh()
   {
      contact = client.query("index", ContactDTO.class ).<ContactDTO>buildWith().prototype();
      setChanged();
      notifyObservers();
   }

   public ContactDTO getContact()
   {
      return contact;
   }

   public ContactPhoneDTO getPhoneNumber()
   {
      if (contact.phoneNumbers().get().isEmpty())
      {
         ContactPhoneDTO phone = module.valueBuilderFactory().newValue(ContactPhoneDTO.class)
               .<ContactPhoneDTO>buildWith().prototype();
         contact.phoneNumbers().get().add( phone );
      }
      return contact.phoneNumbers().get().get( 0 );
   }

   public ContactEmailDTO getEmailAddress()
   {
      if (contact.emailAddresses().get().isEmpty())
      {
         ContactEmailDTO email = module.valueBuilderFactory().newValue(ContactEmailDTO.class)
               .<ContactEmailDTO>buildWith().prototype();
         contact.emailAddresses().get().add( email );
      }
      return contact.emailAddresses().get().get( 0 );
   }

   public void changeName( String newName )
   {
      Form form = new Form();
      form.set("name", newName);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changePhoneNumber( String newPhoneNumber )
   {
      Form form = new Form();
      form.set("phone", newPhoneNumber);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeEmailAddress( String newEmailAddress )
   {
      Form form = new Form();
      form.set("email", newEmailAddress);
      client.putCommand( "update", form.getWebRepresentation() );
   }
}
