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

package se.streamsource.streamflow.client.ui.administration.projects;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import java.util.Set;

/**
 * JAVADOC
 */
public class ProjectMembersModel
      implements Refreshable, EventListener

{
   @Uses
   CommandQueryClient client;

   @Uses
   OrganizationalUnitAdministrationModel ouAdminModel;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private SortedList<LinkValue> members = new SortedList<LinkValue>( new BasicEventList<LinkValue>( ), new LinkComparator() );

   public SortedList<LinkValue> getMembers()
   {
      return members;
   }

   public void refresh()
   {
      try
      {
         LinksValue membersList = client.query( "index", LinksValue.class);
         EventListSynch.synchronize( membersList.links().get(), members );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_members, e );
      }
   }

   public void addMembers( Set<String> newMembers )
   {
      try
      {
         for (String value : newMembers)
         {

            ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
            builder.prototype().entity().set( EntityReference.parseEntityReference(value ));
            client.postCommand( "addmember", builder.newInstance() );
         }
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_members, e );
      }
   }

   public void removeMember( LinkValue member)
   {
      try
      {
         client.getClient( member ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_member, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }

   public CommandQueryClient getFilterResource()
   {
      return client;
   }
}