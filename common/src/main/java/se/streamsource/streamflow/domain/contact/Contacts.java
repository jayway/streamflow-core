/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.domain.contact;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Contacts.ContactsMixin.class)
public interface Contacts
{
    public void addContact();

    public void updateContact(int index, ContactValue contact);

    public void deleteContact(int index);

    interface ContactsState
    {
        @UseDefaults
        Property<List<ContactValue>> contacts();
    }
    
    class ContactsMixin
    implements Contacts
    {
        @This ContactsState state;

        @Structure
        ValueBuilderFactory vbf;

        public void addContact()
        {
            ValueBuilder<ContactValue> builder = vbf.newValueBuilder(ContactValue.class);
            // how to localize default name?
            builder.prototype().name().set("Namn");
            List<ContactValue> contacts = state.contacts().get();
            contacts.add(builder.newInstance());
            state.contacts().set(contacts);
        }

        public void updateContact(int index, ContactValue contact)
        {
            List<ContactValue> contacts = state.contacts().get();
            contacts.add(index, contact);
            contacts.remove(index+1);
            state.contacts().set(contacts);
        }

        public void deleteContact(int index)
        {
            List<ContactValue> contacts = state.contacts().get();
            if (index<contacts.size())
            {
                contacts.remove(index);
            }
            state.contacts().set(contacts);

        }
    }
    
}