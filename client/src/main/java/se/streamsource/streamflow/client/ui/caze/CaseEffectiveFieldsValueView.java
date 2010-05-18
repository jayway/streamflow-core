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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.ToolTipTableCellRenderer;
import se.streamsource.streamflow.domain.contact.ContactValue;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class CaseEffectiveFieldsValueView
      extends JPanel
{
   public ValueBuilder<ContactValue> valueBuilder;
   private JXTable effectiveValueTable;
   public RefreshWhenVisible refresher;

   public CaseEffectiveFieldsValueView( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );
      this.setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      effectiveValueTable = new JXTable();
      effectiveValueTable.setDefaultRenderer( Object.class, new ToolTipTableCellRenderer() );

      JScrollPane effectiveFields = new JScrollPane();

      effectiveFields.setViewportView( effectiveValueTable );

      add( effectiveFields, BorderLayout.CENTER );

      refresher = new RefreshWhenVisible( this );
      addAncestorListener( refresher );
   }


   public void setModel( CaseEffectiveFieldsValueModel model )
   {
      effectiveValueTable.setModel( model );

      refresher.setRefreshable( model );
   }
}