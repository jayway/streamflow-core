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

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.dci.resource.CommandQueryClient;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.roles.StringDTO;

import java.util.Observable;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class FormModel
      extends Observable
      implements Refreshable, EventListener, EventVisitor

{
   @Structure
   ObjectBuilderFactory obf;

   private EventVisitorFilter eventFilter = new EventVisitorFilter( this, "changedNote", "movedField", "changedDescription" );

   @Uses
   CommandQueryClient client;

   private FormValue formValue;

   WeakModelMap<String, FieldsModel> fieldsModels = new WeakModelMap<String, FieldsModel>()
   {

      protected FieldsModel newModel( String key )
      {
         return obf.newObjectBuilder( FieldsModel.class )
               .use( client.getSubClient( "pages" ) ).newInstance();
      }
   };


   public void refresh() throws OperationException
   {
      try
      {
         formValue = client.query( "form", FormValue.class );
         setChanged();
         notifyObservers( this );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_get_form, e);
      }

   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
      for (FieldsModel fieldsModel : fieldsModels)
      {
         fieldsModel.notifyEvent( event );
      }

   }

   public boolean visit( DomainEvent event )
   {
      if (formValue.form().get().identity().equals( event.entity().get() ))
      {
         if (event.name().get().equals( "movedField" ))
         {
            getFieldsModel().refresh();
         }
         Logger.getLogger( "administration" ).info( "Refresh the note" );
         refresh();
      }
      return false;
   }

   public String getNote()
   {
      return formValue.note().get();
   }

   public FormValue getFormValue()
   {
      return formValue;
   }

   public void changeDescription( StringDTO description ) throws ResourceException
   {
      client.putCommand( "changedescription", description );
   }

   public void changeNote( StringDTO note ) throws ResourceException
   {
      client.putCommand( "changenote", note );
   }

   public FieldsModel getFieldsModel()
   {
      return fieldsModels.get( formValue.form().get().identity() );
   }
}