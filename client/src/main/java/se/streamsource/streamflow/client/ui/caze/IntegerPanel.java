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

package se.streamsource.streamflow.client.ui.caze;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IntegerPanel
      extends AbstractFieldPanel
{
   private JTextField textField;

   public IntegerPanel( @Uses FieldSubmissionValue field, @Uses NumberFieldValue fieldValue )
   {
      super( field );
      setLayout( new BorderLayout( ) );

      textField = new JTextField();
      textField.setColumns( 20 );
      add( textField, BorderLayout.WEST );
   }

   @Override
   public String getValue()
   {
      return textField.getText();
   }

   @Override
   public void setValue( String newValue )
   {
      textField.setText( newValue );
   }

   @Override
   public boolean validateValue( Object newValue )
   {
      return true;
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      textField.setInputVerifier( new InputVerifier()
      {

         NumberFormatException exception;

         @Override
         public boolean verify( JComponent input )
         {
            JTextField field = (JTextField)input;
            try
            {
               binding.updateProperty( Integer.parseInt( field.getText() ) );
            }  catch ( NumberFormatException e)
            {
               exception = e;
               return false;
            }
            return true;
         }

         @Override
         public boolean shouldYieldFocus( JComponent input )
         {
            boolean result = super.shouldYieldFocus( input );

            if ( !result )
            {
               if ( exception != null )
               {
                  Window window = WindowUtils.findWindow( input );
                  StringBuilder message = new StringBuilder( "<html>" );
                  message.append( "<p>" ).append( exception.getLocalizedMessage() ).append( "</p>" );
                  message.append( "</html>" );



                  JLabel main = new JLabel( message.toString() );

                  JXDialog dialog;
                  if (window instanceof Frame)
                     dialog = new JXDialog( (Frame) window, main );
                  else
                     dialog = new JXDialog( (Dialog) window, main );

                  dialog.setModal( true );

                  dialog.pack();
                  dialog.setLocationRelativeTo( SwingUtilities.windowForComponent( input ) );
                  dialog.setVisible( true );
               }
            }
            return result;
         }
      });
}
}