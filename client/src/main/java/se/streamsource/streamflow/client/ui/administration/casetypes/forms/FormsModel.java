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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
public class FormsModel
      implements Refreshable, EventListener, EventVisitor

{
   final Logger logger = LoggerFactory.getLogger( "administration" );
   @Uses
   CommandQueryClient client;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private BasicEventList<LinkValue> forms = new BasicEventList<LinkValue>();

   private EventVisitorFilter eventFilter;

   public FormsModel(@Uses CommandQueryClient client)
   {
      eventFilter = new EventVisitorFilter( client.getReference().getParentRef().getLastSegment(), this, "createdForm", "removedForm", "changedDescription" );
   }

   WeakModelMap<String, FormModel> formModels = new WeakModelMap<String, FormModel>()
   {
      protected FormModel newModel( String key )
      {
         return obf.newObjectBuilder( FormModel.class )
               .use( client.getSubClient( key ) ).newInstance();
      }
   };

   public EventList<LinkValue> getForms()
   {
      return forms;
   }

   public void refresh()
   {
      try
      {
         List<LinkValue> formsList = client.query( "index", LinksValue.class ).links().get();
         EventListSynch.synchronize( formsList, forms );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_members, e );
      }
   }

   public void createForm( String formName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( formName );
      try
      {
         client.postCommand( "createform", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.description_cannot_be_more_than_50, e );
      }
   }

   public void removeForm( LinkValue form )
   {
      try
      {
         client.getClient( form ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
      for (FormModel model : formModels)
      {
         model.notifyEvent( event );
      }
   }

   public boolean visit( DomainEvent event )
   {
      logger.info( "Refresh project form definitions" );
      refresh();
      return false;
   }

   public FormModel getFormModel( String identity )
   {
      return formModels.get( identity );
   }
}