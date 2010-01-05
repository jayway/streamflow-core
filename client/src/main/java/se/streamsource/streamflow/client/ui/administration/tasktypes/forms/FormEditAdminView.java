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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.PageBreakFieldValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.roles.StringDTO;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;


/**
 * JAVADOC
 */
public class FormEditAdminView
      extends JSplitPane
      implements Observer
{
   StateBinder formValueBinder;
   private ValueBuilderFactory vbf;
   private FormModel model;

   public FormEditAdminView( @Service ApplicationContext context,
                             @Uses FormModel model,
                             @Uses final FieldsView fieldsView,
                             @Structure final ObjectBuilderFactory obf,
                             @Structure ValueBuilderFactory vbf )
   {
      super();

      this.model = model;
      JPanel formAttributePanel = new JPanel( new BorderLayout() );
      JPanel leftPanel = new JPanel( new BorderLayout() );
      leftPanel.add( formAttributePanel, BorderLayout.CENTER );
      leftPanel.add( fieldsView, BorderLayout.SOUTH );

      this.vbf = vbf;
      FormLayout formLayout = new FormLayout(
            "200dlu", "" );

      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, formAttributePanel );
      formBuilder.setDefaultDialogBorder();

      formValueBinder = new StateBinder();
      formValueBinder.setResourceMap( context.getResourceMap( getClass() ) );
      FormValue formValueTemplate = formValueBinder.bindingTemplate( FormValue.class );

      BindingFormBuilder bb = new BindingFormBuilder( formBuilder, formValueBinder );

      bb.appendLine( AdministrationResources.name_label, TEXTFIELD, formValueTemplate.description() ).
            appendLine( AdministrationResources.description_label, TEXTAREA, formValueTemplate.note() );

      formValueBinder.addObserver( this );

      FormValue value = vbf.newValueBuilder( FormValue.class ).withPrototype( model.getFormValue() ).prototype();
      formValueBinder.updateWith( value );

      setLeftComponent( leftPanel );
      setRightComponent( new JPanel() );

      setDividerLocation( 400 );

      final JList list = fieldsView.getFieldList();

      fieldsView.getFieldList().addListSelectionListener( new ListSelectionListener()
      {

         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = list.getSelectedIndex();

               if (idx < list.getModel().getSize() && idx >= 0)
               {
                  ListItemValue fieldValue = (ListItemValue) list.getModel().getElementAt( idx );
                  FieldValueEditModel editModel = fieldsView.getModel().getFieldModel( fieldValue.entity().get().identity() );

                  FieldValue value = editModel.getFieldDefinition().fieldValue().get();

                  // switch on value
                  if (value instanceof TextFieldValue)
                  {
                     setRightComponent(
                           obf.newObjectBuilder( FieldValueTextEditView.class ).
                                 use( editModel ).newInstance() );
                  } else if (value instanceof PageBreakFieldValue)
                  {
                     setRightComponent( obf.newObjectBuilder( FieldValuePageBreakEditView.class ).
                                 use( editModel ).newInstance() );
                  }

               } else
               {
                  setRightComponent( new JPanel() );
               }
            }

         }
      } );
   }

   public void update( Observable observable, Object arg )
   {
      Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "description" ))
      {
         try
         {
            ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
            builder.prototype().string().set( (String) property.get() );
            model.changeDescription( builder.newInstance() );
         } catch (ResourceException e)
         {
            throw new OperationException( TaskResources.could_not_change_name, e );
         }
      } else if (property.qualifiedName().name().equals( "note" ))
      {
         try
         {
            ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
            builder.prototype().string().set( (String) property.get() );
            model.changeNote( builder.newInstance() );
         } catch (ResourceException e)
         {
            throw new OperationException( TaskResources.could_not_change_note, e );
         }
      }
   }
}