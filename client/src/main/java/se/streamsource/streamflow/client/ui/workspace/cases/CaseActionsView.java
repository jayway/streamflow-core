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
package se.streamsource.streamflow.client.ui.workspace.cases;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormDraftModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageView;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class CaseActionsView extends JPanel
      implements TransactionListener, Observer
{

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   @Service
   StreamflowApplication main;

   private CaseModel model;

   private JPanel actionsPanel = new JPanel();
   private ApplicationContext context;

   private enum CaseActionButtonTemplate
   {
      open,
      sendto,
      assign,
      assignto,
      unassign,
      onhold,
      resume,
      formondelete,
      reopen,
      delete,
      exportpdf,
      reinstate,
      restrict,
      unrestrict,
      markunread,
      markread,
      requirecasetype,
      resolve,
      formonclose,
      close
   }

   public CaseActionsView( @Service ApplicationContext context, @Uses CaseModel model )
   {
      this.model = model;
      this.context = context;

      model.addObserver( this );

      setLayout( new BorderLayout() );
      setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
      actionsPanel.setLayout( new GridLayout( 0, 1, 5, 5 ) );
      add( actionsPanel, BorderLayout.NORTH );
      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CaseActionsView.class, this ) );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task open()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.open();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task assign()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.assignToMe();
         }
      };
   }
   
   @Action(block = Task.BlockingScope.COMPONENT)
   public Task assignto()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            model.getPossibleAssignTo() ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( WorkspaceResources.choose_assignee_title ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.assignTo( dialog.getSelectedLink() );
            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task close()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.close();
         }
      };
   }


   @Action(block = Task.BlockingScope.COMPONENT)
   public Task resolve()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();

      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder( SelectLinkDialog.class )
            .use( model.getPossibleResolutions() ).newInstance();
      dialogs.showOkCancelHelpDialog(
            WindowUtils.findWindow( this ),
            dialog,
            i18n.text( AdministrationResources.resolve ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.resolve( dialog.getSelectedLink() );
            }
         };
      } else
         return null;

   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task formonclose()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();

      CommandQueryClient formOnCloseClient = model.getClient().getClient( "submitformonclose/" );
      formOnCloseClient.postCommand( "create" );
      LinkValue formDraftLink = formOnCloseClient.query( "formdraft", LinkValue.class );

      if( formWizard( formDraftLink ) )
      {
         return new CommandTask()
         {
            @Override
            protected void command()
                  throws Exception
            {
               model.formOnClose();
            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task formondelete()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();


      StringValue name = model.getClient().query( "formondeletename", StringValue.class );

      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setCustomMessage( i18n.text( WorkspaceResources.formondelete_confirmation, name.string().get() ));
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         CommandQueryClient formOnRemoveClient = model.getClient().getClient( "submitformondelete/" );
         formOnRemoveClient.postCommand( "create" );
         LinkValue formDraftLink = formOnRemoveClient.query( "formdraft", LinkValue.class );

         if( formWizard( formDraftLink ) )
         {
            return new CommandTask()
            {
               @Override
               protected void command()
                     throws Exception
               {
                  model.formOnRemove();
               }
            };
         } else
            return null;
      } else
      {
         return null;
      }
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task delete()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setRemovalMessage( i18n.text( WorkspaceResources.caze ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.delete();
            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task sendto()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            model.getPossibleSendTo() ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( WorkspaceResources.choose_owner_title ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.sendTo( dialog.getSelectedLink() );
            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task onhold( ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.onHold();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task reopen()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.reopen();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task resume()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.resume();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task unassign()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.unassign();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public void exportpdf()
   {

      PdfPrintingDialog dialog = module.objectBuilderFactory().newObjectBuilder( PdfPrintingDialog.class ).use( context ).newInstance();
      dialogs.showOkCancelHelpDialog( (StreamflowButton)getActionMap().get( "exportpdf" ).getValue( "sourceButton" ),
            dialog, i18n.text( WorkspaceResources.printing_configuration ), DialogService.Orientation.right );

      if( dialog.getCaseOutputConfig() != null )
         new PrintCaseTask( dialog.getCaseOutputConfig() ).execute();
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task reinstate()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setCustomMessage( i18n.text( WorkspaceResources.caze_reinstate ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.reinstate();
            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT )
   public Task restrict()
   {
      return new CommandTask()
      {
         @Override
         protected void command() throws Exception
         {
            model.restrict();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task unrestrict()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setCustomMessage( i18n.text( WorkspaceResources.unrestrict_case ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.unrestrict();
            }
         };
      } else
      {
         return null;
      }
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task markunread( ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.markunread();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task markread( ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.markread();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task requirecasetype(){

       ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
       dialog.setCustomMessage( i18n.text( WorkspaceResources.caze_requirescasetype ) );
       dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );

       if (dialog.isConfirmed())
       {
           return new CommandTask()
           {
               @Override
               public void command()
                       throws Exception
               {
                   model.requirecasetype();
               }
           };
       } else
           return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withUsecases( "sendto", "open", "assign", "close", "onhold", "reopen", "resume", "unassign", "resolved", "formonclose", "formondelete", "reinstate", "restrict", "unrestrict", "read" ), transactions )
              || matches( withNames("changedCaseType"), transactions ))
      {
         model.refresh();
      }
   }

   public void update( Observable o, Object arg )
   {
      // Update list of action buttons
      actionsPanel.removeAll();

      ActionMap am = getActionMap();

      for (CaseActionButtonTemplate buttonOrder : CaseActionButtonTemplate.values())
      {
         for (LinkValue commandLink : Iterables.flatten( model.getCommands(), model.getQueries() ))
         {
            if (buttonOrder.toString().equals( commandLink.rel().get() ))
            {
               javax.swing.Action action1 = am.get( commandLink.rel().get() );
               if (action1 != null)
               {
                  StreamflowButton button = new StreamflowButton( action1 );
                  button.registerKeyboardAction( action1, (KeyStroke) action1
                        .getValue( javax.swing.Action.ACCELERATOR_KEY ),
                        JComponent.WHEN_IN_FOCUSED_WINDOW );
                  button.setHorizontalAlignment( SwingConstants.LEFT );
                  actionsPanel.add( button );
                  action1.putValue( "sourceButton", button );
//				NotificationGlassPane.registerButton(button);
               }
            }
         }
      }

      revalidate();
      repaint();
   }


   private boolean formWizard( LinkValue formDraftLink )
   {
      // get the form submission value;
      final CommandQueryClient formDraftClient = model.getClient().getClient( formDraftLink );

      final FormDraftModel formDraftModel = module.objectBuilderFactory().newObjectBuilder(FormDraftModel.class).use(formDraftClient).newInstance();

      FormDraftDTO formDraftDTO = (FormDraftDTO) formDraftModel.getFormDraftDTO().buildWith().prototype();

      final WizardPage[] wizardPages = new WizardPage[ formDraftDTO.pages().get().size() ];
      for (int i = 0; i < formDraftDTO.pages().get().size(); i++)
      {
         PageSubmissionDTO page = formDraftDTO.pages().get().get( i );
         if ( page.fields().get() != null && page.fields().get().size() >0 )
         {
            wizardPages[i] = module.objectBuilderFactory().newObjectBuilder(FormSubmissionWizardPageView.class).
                  use( formDraftModel, page ).newInstance();
         }
      }

     Map initialProperties = new HashMap( );

     Wizard wizard = WizardPage.createWizard( formDraftDTO.description().get(), wizardPages, new WizardPage.WizardResultProducer()
      {
         public Object finish( Map map ) throws WizardException
         {
            // Force focus move before submit
            Component focusOwner = WindowUtils.findWindow( wizardPages[ wizardPages.length - 1 ]  ).getFocusOwner();
            if (focusOwner != null)
            {
               focusOwner.transferFocus();

               formDraftModel.submit();
               map.put( "success", true );
            }
            return map;
         }

         public boolean cancel( Map map )
         {
            formDraftModel.delete();
          return true;
         }
      } );
      Point onScreen = main.getMainFrame().getLocationOnScreen();
      Map result = (Map)WizardDisplayer.showWizard( wizard, new Rectangle( onScreen, new Dimension( 800, 600 ) ), null, initialProperties );

      return (result == null ||result.get( "success" ) == null)
            ? false : (Boolean)result.get( "success" );
   }

   private class PrintCaseTask extends Task<File, Void>
   {
      private CaseOutputConfigDTO config;

      public PrintCaseTask( CaseOutputConfigDTO config )
      {
         super( Application.getInstance() );
         this.config = config;

         setUserCanCancel( false );
      }

      @Override
      protected File doInBackground() throws Exception
      {
         setMessage( getResourceMap().getString( "description" ) );

         File file = model.export(config);

         return file;
      }

      @Override
      protected void succeeded( File file )
      {
         // Open file
         Desktop desktop = Desktop.getDesktop();
         try
         {
            edit(file);
         } catch (Exception e)
         {
            try
            {
               open(file);
            } catch (Exception e1)
            {
               dialogs.showMessageDialog( CaseActionsView.this, i18n.text( WorkspaceResources.could_not_print ), "" );
            }
         }
      }
   }


   public boolean open(File file) {

      if (openSystemSpecific(file.getPath())) return true;

      if (openDESKTOP(file)) return true;

      return false;
   }


   public boolean edit(File file) {

      // you can try something like
      // runCommand("gimp", "%s", file.getPath())
      // based on user preferences.

      if (openSystemSpecific(file.getPath())) return true;

      if (editDESKTOP(file)) return true;

      return false;
   }


   private boolean openSystemSpecific(String what) {

      EnumOS os = getOs();

      if (os.isLinux()) {
         if (runCommand("kde-open", "%s", what)) return true;
         if (runCommand("gnome-open", "%s", what)) return true;
         if (runCommand("xdg-open", "%s", what)) return true;
      }

      if (os.isMac()) {
         if (runCommand("open", "%s", what)) return true;
      }

      if (os.isWindows()) {
         if (runCommand("explorer", "%s", what)) return true;
      }

      return false;
   }

   private boolean openDESKTOP(File file) {

      logOut("Trying to use Desktop.getDesktop().open() with " + file.toString());
      try {
         if (!Desktop.isDesktopSupported()) {
            logErr("Platform is not supported.");
            return false;
         }

         if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            logErr("OPEN is not supported.");
            return false;
         }

         Desktop.getDesktop().open(file);

         return true;
      } catch (Throwable t) {
         logErr("Error using desktop open.", t);
         return false;
      }
   }


   private boolean editDESKTOP(File file) {

      logOut("Trying to use Desktop.getDesktop().edit() with " + file);
      try {
         if (!Desktop.isDesktopSupported()) {
            logErr("Platform is not supported.");
            return false;
         }

         if (!Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
            logErr("EDIT is not supported.");
            return false;
         }

         Desktop.getDesktop().edit(file);

         return true;
      } catch (Throwable t) {
         logErr("Error using desktop edit.", t);
         return false;
      }
   }


   private boolean runCommand(String command, String args, String file) {

      logOut("Trying to exec:\n   cmd = " + command + "\n   args = " + args + "\n   %s = " + file);

      String[] parts = prepareCommand(command, args, file);

      try {
         Process p = Runtime.getRuntime().exec(parts);
         if (p == null) return false;

         try {
            int retval = p.exitValue();
            if (retval == 0) {
               logErr("Process ended immediately.");
               return false;
            } else {
               logErr("Process crashed.");
               return false;
            }
         } catch (IllegalThreadStateException itse) {
            logErr("Process is running.");
            return true;
         }
      } catch (IOException e) {
         logErr("Error running command.", e);
         return false;
      }
   }


   private String[] prepareCommand(String command, String args, String file) {

      List<String> parts = new ArrayList<String>();
      parts.add(command);

      if (args != null) {
         for (String s : args.split(" ")) {
            s = String.format(s, file); // put in the filename thing

            parts.add(s.trim());
         }
      }

      return parts.toArray(new String[parts.size()]);
   }

   private static void logErr(String msg, Throwable t) {
      System.err.println(msg);
      t.printStackTrace();
   }

   private static void logErr(String msg) {
      System.err.println(msg);
   }

   private static void logOut(String msg) {
      System.out.println(msg);
   }

   public static enum EnumOS {
      linux, macos, solaris, unknown, windows;

      public boolean isLinux() {

         return this == linux || this == solaris;
      }


      public boolean isMac() {

         return this == macos;
      }


      public boolean isWindows() {

         return this == windows;
      }
   }


   public static EnumOS getOs() {

      String s = System.getProperty("os.name").toLowerCase();

      if (s.contains("win")) {
         return EnumOS.windows;
      }

      if (s.contains("mac")) {
         return EnumOS.macos;
      }

      if (s.contains("solaris")) {
         return EnumOS.solaris;
      }

      if (s.contains("sunos")) {
         return EnumOS.solaris;
      }

      if (s.contains("linux")) {
         return EnumOS.linux;
      }

      if (s.contains("unix")) {
         return EnumOS.linux;
      } else {
         return EnumOS.unknown;
      }
   }
}
