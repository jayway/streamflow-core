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

package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import ca.odell.glazedlists.event.*;
import com.jgoodies.forms.builder.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.workspace.cases.form.FieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormDTO;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedPageDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class CaseSubmittedFormView
      extends CaseSubmittedFormAbstractView
      implements Refreshable
{
   private CaseSubmittedFormModel model;

   public CaseSubmittedFormView(@Service ApplicationContext context, @Uses CommandQueryClient client,
                                @Structure ObjectBuilderFactory obf, @Uses Integer index, @Structure  ValueBuilderFactory vbf)
   {
      model = obf.newObjectBuilder( CaseSubmittedFormModel.class ).use( client, new Integer(index) ).newInstance();

      setActionMap( context.getActionMap( this ) );

      new RefreshWhenShowing( this, this );
   }

   public void refresh()
   {
      model.refresh();
      panel().removeAll();
      final DefaultFormBuilder builder = builder( panel() );
      SubmittedFormDTO form = model.getForm();

      JLabel title = new JLabel( form.form().get() + " (" + form.submitter().get() +
            ", " + DateTimeFormat.forPattern(text(date_time_format)).print( new DateTime( form.submissionDate().get()) ) +
            "):");
      //title.setFont( title.getFont().deriveFont( Font. ))

      builder.append( title );
      builder.nextLine();

      if (!form.signatures().get().isEmpty())
      {
         builder.appendSeparator(i18n.text(WorkspaceResources.signatures));
         for (FormSignatureDTO signatureDTO : form.signatures().get())
         {
            builder.append( signatureDTO.name().get()+": "+signatureDTO.signerName().get()+"("+signatureDTO.signerId()+")" );
            builder.nextLine();
         }
      }

      for (SubmittedPageDTO page : form.pages().get())
      {
         builder.appendSeparator(page.name().get());

         for (FieldDTO field : page.fields().get())
         {
            JLabel label = new JLabel( field.field().get()+":", SwingConstants.LEFT );
            label.setForeground(Color.gray);
            JComponent component = getComponent( field.value().get(), field.fieldType().get() );

            builder.append( label );
            builder.nextLine();
            builder.append( component );
            builder.nextLine();
         }
      }
      revalidate();
      repaint();
   }

   @Override
   protected FormAttachmentDownload getModel()
   {
      return model;
   }

   @Action
   public Task open( ActionEvent event )
   {
      return openAttachment( event );
   }
}