/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.history;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationView;

/**
 * JAVADOC
 */
public class HistoryView extends JPanel
{

   /**
    * 
    */
   private static final long serialVersionUID = 3540488243544689740L;

   public HistoryView( @Service ApplicationContext appContext,
                           @Uses CommandQueryClient client,
                           @Structure ObjectBuilderFactory obf )
   {
      this.setBorder( BorderFactory.createEmptyBorder() );
      
      this.setLayout( new BorderLayout());
      this.add( obf.newObjectBuilder( ConversationView.class ).use( client ).newInstance(), BorderLayout.CENTER );
   }
}