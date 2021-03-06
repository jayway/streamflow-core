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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.StateBinder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import se.streamsource.streamflow.util.MultiFieldHelper;

public class CheckboxesPanel
      extends AbstractFieldPanel
{

   Map<String, JCheckBox> checkBoxMap;

   public CheckboxesPanel( @Uses FieldSubmissionDTO field, @Uses CheckboxesFieldValue fieldValue )
   {
      super( field );

      JPanel panel = new JPanel( new BorderLayout( ));
      FormLayout formLayout = new FormLayout( "200dlu", "" );
      DefaultFormBuilder formBuilder = new DefaultFormBuilder( formLayout, panel );

      checkBoxMap = new HashMap<String, JCheckBox>();
      for ( String element : fieldValue.values().get() )
      {
         JCheckBox checkBox = new JCheckBox( element );
         checkBoxMap.put( element, checkBox );
         formBuilder.append( checkBox );
         formBuilder.nextLine();
      }

      add( panel, BorderLayout.WEST );
   }

   @Override
   public void setValue( String newValue )
   {
      if ( newValue == null || newValue.equals( "" )) return;
      for (String box : MultiFieldHelper.options( newValue ))
      {
         checkBoxMap.get( box ).setSelected( true );
      }
   }

   @Override
   public String getValue()
   {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (JCheckBox checkBox : checkBoxMap.values())
      {
         if ( checkBox.isSelected() )
         {
            if (!first) sb.append( ", " );
            String text = checkBox.getText();
            if ( text.contains( "," ) )
            {
               sb.append( "[" ).append( text ).append( "]" );
            } else
            {
               sb.append( text );
            }
            first = false;
         }
      }
      return sb.toString();
   }

   @Override
   public void setBinding( final StateBinder.Binding binding )
   {
      ActionListener listener = new ActionListener() {
         public void actionPerformed( ActionEvent e )
         {
            binding.updateProperty( getValue()  );
         }
      };
      for (JCheckBox box : checkBoxMap.values())
      {
         box.addActionListener( listener );
      }
   }
}
