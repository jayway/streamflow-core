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
package se.streamsource.streamflow.web.infrastructure.plugin.contact;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.restlet.client.CommandQueryClientFactory;
import se.streamsource.dci.restlet.client.NullResponseHandler;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.infrastructure.plugin.ContactLookupServiceConfiguration;

/**
 * Service that looks up contacts in a REST plugin
 */
@Mixins(ContactLookupService.Mixin.class)
public interface ContactLookupService
      extends ServiceComposite, ContactLookup, Configuration, Activatable
{
   class Mixin
         implements ContactLookup, Activatable
   {
      @This
      Configuration<ContactLookupServiceConfiguration> config;

      @Structure
      Module module;

      private CommandQueryClient cqc;

      Logger log = LoggerFactory.getLogger( ContactLookupService.class );

      public void activate() throws Exception
      {
         config.configuration();

         if (config.configuration().enabled().get())
         {
            Reference serverRef = new Reference( config.configuration().url().get() );
            Client client = new Client( Protocol.HTTP );
            client.start();

            cqc = module.objectBuilderFactory().newObjectBuilder(CommandQueryClientFactory.class).use( client, new NullResponseHandler() ).newInstance().newClient( serverRef );
         }
      }

      public void passivate() throws Exception
      {
      }

      public ContactList lookup( ContactValue contactTemplate )
      {
         try
         {
            return cqc.query( config.configuration().url().get(), ContactList.class, contactTemplate);
         } catch (Exception e)
         {
            log.error( "Could not get contacts from plugin", e );

            // Return empty list
            return module.valueBuilderFactory().newValue(ContactList.class);
         }
      }
   }
}
