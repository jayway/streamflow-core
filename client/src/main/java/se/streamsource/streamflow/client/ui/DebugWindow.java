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

package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.swing.ActionMap;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * JAVADOC
 */
public class DebugWindow
      extends FrameView
{
   public EventTable eventTable;

   public DebugWindow( @Service Application application)
   {
      super( application );

      eventTable = new EventTable( );
      eventTable.setEditable( false );

      JXFrame frame = new JXFrame( "Streamflow Debug" );
      frame.getContentPane().add( new JScrollPane( eventTable ) );

      setFrame( frame );

      JToolBar toolbar = new JToolBar();
      ActionMap am = application.getContext().getActionMap( this );
      toolbar.add( am.get( "clear" ) );

      setToolBar( toolbar );

      frame.setSize( 400, 400 );
   }

   @Action
   public void clear()
   {
      eventTable.clear();
   }

   public static class EventTable
      extends JXTable
      implements TransactionListener
   {
      private DefaultTableModel eventModel;

      public EventTable( )
      {
         eventModel = new DefaultTableModel( new String[]{"Usecase", "Event", "Entity", "Parameters"}, 0 );
         setModel(eventModel);
      }

      public void notifyTransactions( Iterable<TransactionEvents> transactions )
      {
         for( DomainEvent event : Events.events( transactions ))
         {
            eventModel.addRow( new String[]{event.usecase().get(), event.name().get(), event.entity().get(), event.parameters().get()} );

            if (eventModel.getRowCount() > 100)
               eventModel.removeRow( 0 );
         }
      }

      public void clear()
      {
         int count = eventModel.getRowCount();
         for (int i = 0; i < count; i++)
            eventModel.removeRow( 0 );
      }
   }
}
