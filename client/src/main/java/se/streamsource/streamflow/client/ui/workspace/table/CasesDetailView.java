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

package se.streamsource.streamflow.client.ui.workspace.table;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.data.Reference;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseDetailView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseModel;
import se.streamsource.streamflow.client.ui.workspace.cases.SubCasesView;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;


/**
 * JAVADOC
 */
public class CasesDetailView
      extends JPanel
      implements TransactionListener
{
   private CaseDetailView current = null;
   private SubCasesView subCasesView = null;

   @Structure
   ObjectBuilderFactory obf;

   private CardLayout layout = new CardLayout();
   private JSplitPane casePanel = new JSplitPane();

   private Reference currentCase;
   private CommandQueryClient currentMainCase;
   private CaseModel model;

   public CasesDetailView(@Service ApplicationContext context)
   {
      setLayout( layout );
      setBorder( BorderFactory.createEmptyBorder() );

      setActionMap( context.getActionMap(this) );

      casePanel.setOneTouchExpandable( true );
      casePanel.setDividerSize( 10 );
      casePanel.setDividerLocation( 0.0 );
      casePanel.setLastDividerLocation( 150 );

      add( new JLabel( i18n.text( WorkspaceResources.choose_case ), JLabel.CENTER ), "blank" );
      add( casePanel, "detail" );

      layout.show( this, "blank" );

      setPreferredSize( new Dimension( getWidth(), 500 ) );
   }

   public void show( final CommandQueryClient client )
   {
      show(client, false);
   }

   public void show( final CommandQueryClient client, boolean isSubCase )
   {
      if (currentCase == null || !currentCase.equals( client.getReference() ))
      {
         model = obf.newObjectBuilder( CaseModel.class ).use( client ).newInstance();

         if (current != null)
         {
            int tab = current.getSelectedTab();
            currentCase = client.getReference();
            current = obf.newObjectBuilder( CaseDetailView.class ).use( client, model ).newInstance();
            current.setSelectedTab( tab );
            casePanel.setRightComponent( current );

            if (!isSubCase)
            {
               subCasesView = obf.newObjectBuilder( SubCasesView.class ).use( client, model ).newInstance();
               casePanel.setLeftComponent( subCasesView );
               casePanel.setDividerLocation( 0.0 );
               casePanel.setLastDividerLocation( 150 );
               casePanel.revalidate();
               currentMainCase = client;
            }
         } else
         {
            currentCase = client.getReference();
            current = obf.newObjectBuilder( CaseDetailView.class ).use( client, model ).newInstance();
            casePanel.setRightComponent( current );

            if (!isSubCase)
            {
               subCasesView = obf.newObjectBuilder( SubCasesView.class ).use( client, model ).newInstance();
               casePanel.setLeftComponent( subCasesView );
               casePanel.setDividerLocation( 0.0 );
               casePanel.setLastDividerLocation( 150 );
               casePanel.revalidate();
               currentMainCase = client;
            }
            layout.show( this, "detail" );
         }

         if (!isSubCase)
         {
            subCasesView.getList().addListSelectionListener( new ListSelectionListener()
            {
               public void valueChanged( ListSelectionEvent e )
               {
                  if (!e.getValueIsAdjusting())
                  {
                     LinkValue link = (LinkValue) subCasesView.getList().getSelectedValue();

                     if (link != null)
                     {
                        show( client.getClient( link ), true );
                     }
                  }
               }
            } );
            subCasesView.getCaseButton().addActionListener( getActionMap().get( "showMainCase" ));
            subCasesView.getParentCaseButton().addActionListener( getActionMap().get( "showParentCase" ) );
         }

         current.requestFocusInWindow();
      }
   }

   public void clear()
   {
      layout.show( this, "blank" );
      casePanel.setLeftComponent( null );
      casePanel.setRightComponent( null );
      current = null;
      currentCase = null;
   }

   @Action
   public void showMainCase()
   {
      show(currentMainCase);
   }

   @Action
   public void showParentCase()
   {
      show(currentMainCase.getClient( subCasesView.getModel().getIndex().parentCase().get()));
   }

   @Override
   public boolean requestFocusInWindow()
   {
      return current == null ? false : current.requestFocusInWindow();
   }

   public CaseDetailView getCurrentCaseView()
   {
      return current;
   }

   public void refresh()
   {
      if (current != null)
      {
         layout.show( this, "blank" );
         layout.show( this, "detail" );
      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (currentCase != null)
      {
         if (matches( onEntityTypes( "se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" ), transactions ))
         {
            if (matches( withNames( "deletedEntity", "createdCase" ), transactions ))
            {
               if (currentMainCase.getReference().equals(currentCase))
                  clear();
               else
                  show(currentMainCase);
            } else if (matches(withUsecases( "createsubcase" ), transactions ))
            {
               // Do nothing
            } // only clear detail if it is not a draft
            else if (matches( withNames( "changedOwner" ), transactions )
                  && !"DRAFT".equals( model.getIndex().status().get().name() ))
            {
               clear();
            }
            // clear detail if status changed from draft to open and it's not a subcase
            else if (matches( withUsecases( "open" ), transactions ) && currentMainCase.getReference().equals(currentCase))
            {
               clear();
            }
         }
      }
   }
}