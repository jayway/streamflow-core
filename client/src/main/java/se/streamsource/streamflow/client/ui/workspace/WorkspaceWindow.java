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
package se.streamsource.streamflow.client.ui.workspace;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.swingx.JXFrame;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.client.ui.account.AccountModel;
import se.streamsource.streamflow.client.ui.account.AccountSelectionView;
import se.streamsource.streamflow.client.ui.account.AccountSelector;
import se.streamsource.streamflow.client.ui.menu.WorkspaceMenuBar;
import se.streamsource.streamflow.client.util.JavaHelp;
import se.streamsource.streamflow.client.util.i18n;

/**
 * Workspace window
 */
public class WorkspaceWindow
        extends FrameView
{
   public CardLayout cardLayout;

   private WorkspaceView currentWorkspace;

   public WorkspaceWindow(@Service Application application,
                          @Service JavaHelp javaHelp,
                          @Uses WorkspaceMenuBar menu,
                          @Uses AccountSelectionView view,
                          @Uses final AccountSelector accountSelector,
                          @Structure final Module module)
   {
      super(application);

      final JXFrame frame = new JXFrame(i18n.text(WorkspaceResources.window_name));
      frame.setLocationByPlatform(true);

      cardLayout = new CardLayout();
      frame.getContentPane().setLayout(cardLayout);
      frame.getContentPane().add(view, "selector");
      frame.getContentPane().add(new JPanel(), "workspace");
      frame.getRootPane().setOpaque(true);
      setFrame(frame);
      setMenuBar(menu);


      frame.setPreferredSize( new Dimension( 1300, 800 ) );
      frame.pack();
      frame.setExtendedState(frame.getExtendedState() | JXFrame.MAXIMIZED_BOTH);
      // Turn off java help for 1.0 release
      //javaHelp.enableHelp( this.getRootPane(), "workspace" );

      ListSelectionListener workspaceListener = new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (!e.getValueIsAdjusting())
            {
               if (accountSelector.isSelectionEmpty())
               {
                  cardLayout.show(frame.getContentPane(), "selector");
               } else
               {
                  final AccountModel accountModel = accountSelector.getSelectedAccount();

                  SwingUtilities.invokeLater(new Runnable()
                  {
                     public void run()
                     {
                        if (currentWorkspace != null)
                        {
                           currentWorkspace.killPopup();
                        }
                        currentWorkspace = module.objectBuilderFactory().newObjectBuilder(WorkspaceView.class).use(accountModel.newWorkspaceModel()).newInstance();
                        frame.getContentPane().add(currentWorkspace, "workspace");
                        cardLayout.show(frame.getContentPane(), "workspace");
                     }
                  });
               }
            }
         }
      };

      accountSelector.addListSelectionListener(workspaceListener);
   }

   public WorkspaceView getCurrentWorkspace()
   {
      return currentWorkspace;
   }


}
