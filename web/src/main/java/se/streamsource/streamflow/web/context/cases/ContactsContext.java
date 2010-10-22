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

package se.streamsource.streamflow.web.context.cases;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.caze.ContactsDTO;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;

import java.util.List;

/**
 * JAVADOC
 */
public class ContactsContext
{
   @Structure
   Module module;

   public ContactsDTO contacts()
   {
      ValueBuilder<ContactsDTO> builder = module.valueBuilderFactory().newValueBuilder( ContactsDTO.class );
      ValueBuilder<ContactValue> contactBuilder = module.valueBuilderFactory().newValueBuilder( ContactValue.class );
      List<ContactValue> list = builder.prototype().contacts().get();

      Contacts.Data contacts = RoleMap.role( Contacts.Data.class );

      for (ContactValue contact : contacts.contacts().get())
      {
         contactBuilder.prototype().company().set( contact.company().get() );
         contactBuilder.prototype().name().set( contact.name().get() );
         contactBuilder.prototype().isCompany().set( contact.isCompany().get() );
         contactBuilder.prototype().note().set( contact.note().get() );
         contactBuilder.prototype().picture().set( contact.picture().get() );
         contactBuilder.prototype().contactId().set( contact.contactId().get() );
         contactBuilder.prototype().addresses().set( contact.addresses().get() );
         contactBuilder.prototype().emailAddresses().set( contact.emailAddresses().get() );
         contactBuilder.prototype().phoneNumbers().set( contact.phoneNumbers().get() );
         list.add( contactBuilder.newInstance() );
      }
      return builder.newInstance();
   }

   public void add( ContactValue newContact )
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      contacts.addContact( newContact );
   }
}