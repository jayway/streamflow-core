/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.context.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.task.TaskContactsDTO;
import se.streamsource.streamflow.web.domain.structure.task.Contacts;
import se.streamsource.streamflow.web.infrastructure.web.context.Context;
import se.streamsource.streamflow.web.infrastructure.web.context.ContextMixin;
import se.streamsource.streamflow.web.infrastructure.web.context.SubContexts;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(TaskContactsContext.Mixin.class)
public interface TaskContactsContext
   extends
      SubContexts<TaskContactContext>, Context
{
   public void add( ContactValue newContact );

   TaskContactsDTO contacts();

   abstract class Mixin
      extends ContextMixin
      implements TaskContactsContext
   {
      @Structure
      Module module;

      public TaskContactsDTO contacts()
      {
         ValueBuilder<TaskContactsDTO> builder = module.valueBuilderFactory().newValueBuilder( TaskContactsDTO.class );
         ValueBuilder<ContactValue> contactBuilder = module.valueBuilderFactory().newValueBuilder( ContactValue.class );
         List<ContactValue> list = builder.prototype().contacts().get();

         Contacts.Data contacts = context.role( Contacts.Data.class );

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
         Contacts contacts = context.role(Contacts.class);
         contacts.addContact( newContact );
      }

      public TaskContactContext context( String id )
      {
         Integer index = Integer.decode( id );
         context.playRoles( index, Integer.class);

         ContactValue contact = context.role( Contacts.Data.class ).contacts().get().get( index );
         context.playRoles(contact, ContactValue.class);

         return subContext( TaskContactContext.class );
      }
   }
}