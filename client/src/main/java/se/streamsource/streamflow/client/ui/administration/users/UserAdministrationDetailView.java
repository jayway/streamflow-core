/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.users;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.account.AccountResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.sound.sampled.BooleanControl;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;
import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class UserAdministrationDetailView
      extends JPanel
      implements Observer, TransactionListener
{

   @Service
   DialogService dialogs;

   private Module module;

   @Service
   StreamflowApplication main;

   private StateBinder contactBinder;
   private StateBinder phoneNumberBinder;
   private StateBinder emailBinder;

   public JPanel contactForm;
   public JRadioButton noneButton;
   public JRadioButton emailButton;


   private UserAdministrationDetailModel model;

   private Box actionsPanel = Box.createVerticalBox();
   private JPanel profilePanel;
   private ApplicationContext context;

   private enum UserAdministrationButtonTemplate
   {
      setdisabled,
      setenabled,
      join,
      leave,
      resetpassword
   }

   public UserAdministrationDetailView( @Service ApplicationContext context, @Uses UserAdministrationDetailModel model, @Structure Module module )
   {
      this.model = model;
      this.context = context;
      this.module = module;

      model.addObserver( this );

      setLayout( new BorderLayout() );
      setBorder( new EmptyBorder( 5, 5, 5, 5 ) );

      setActionMap( context.getActionMap( this ) );

      ActionMap am = getActionMap();
      profilePanel = new JPanel(new BorderLayout());
      profilePanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

      contactForm = new JPanel();
      profilePanel.add(contactForm, BorderLayout.CENTER);
      FormLayout contactLayout = new FormLayout("75dlu, 5dlu, 120dlu:grow",
            "pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref");

      contactBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      contactBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactDTO template = contactBinder.bindingTemplate(ContactDTO.class);

      phoneNumberBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      phoneNumberBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactPhoneDTO phoneTemplate = phoneNumberBinder
            .bindingTemplate(ContactPhoneDTO.class);

      emailBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      emailBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactEmailDTO emailTemplate = emailBinder
            .bindingTemplate(ContactEmailDTO.class);


      DefaultFormBuilder contactBuilder = new DefaultFormBuilder(contactLayout, contactForm);

      contactBuilder.appendSeparator(i18n
            .text( AccountResources.contact_info_for_user_separator));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.name_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add( contactBinder.bind(TEXTFIELD.newField(),
            template.name()));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.email_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(emailBinder.bind( TEXTFIELD.newField(), emailTemplate.emailAddress() ));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.phone_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(phoneNumberBinder.bind(TEXTFIELD.newField(),
            phoneTemplate.phoneNumber()));
      contactBuilder.nextLine(2);

      contactBuilder.add(new JLabel( i18n
            .text(WorkspaceResources.choose_message_delivery_type)));
      noneButton = (JRadioButton) RADIOBUTTON.newField();
      noneButton.setAction(am.get("messageDeliveryTypeNone"));
      noneButton.setSelected(true);
      contactBuilder.nextColumn(2);
      contactBuilder.add(noneButton);
      contactBuilder.nextLine();
      contactBuilder.nextColumn(2);
      emailButton = (JRadioButton) RADIOBUTTON.newField();
      emailButton.setAction(am.get("messageDeliveryTypeEmail"));
      contactBuilder.add(emailButton);

      // Group the radio buttons.
      ButtonGroup group = new ButtonGroup();
      group.add(noneButton);
      group.add(emailButton);

      contactBuilder.nextLine(2);

      contactBinder.addObserver(this);
      phoneNumberBinder.addObserver(this);
      emailBinder.addObserver( this );

      add( profilePanel, BorderLayout.CENTER );
      add( actionsPanel, BorderLayout.EAST );

      new RefreshWhenShowing( this, model );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task setdisabled()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.setdisabled();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task setenabled()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.setenabled();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task join()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.join();
         }
      };
   }


   @Action(block = Task.BlockingScope.COMPONENT)
   public Task leave()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setCustomMessage( i18n.text( WorkspaceResources.unrestrict_case ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.leave();
            }
         };
      } else
         return null;

   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task resetpassword()
   {
      final ResetPasswordDialog dialog = module.objectBuilderFactory().newObject( ResetPasswordDialog.class );
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.reset_password_title ) );

      if (dialog.password() != null)
      {
         return new CommandTask()
         {
            @Override
            protected void command()
                  throws Exception
            {
               model.resetPassword( dialog.password() );
            }
         };
      } else
         return null;
   }

   @Action
   public void messageDeliveryTypeNone() throws Exception
   {
      model.changeMessageDeliveryType("none");
   }

   @Action
   public void messageDeliveryTypeEmail() throws Exception
   {
      model.changeMessageDeliveryType("email");
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withUsecases( "join", "leave", "changedisabled" ), transactions ))
      {
         model.refresh();
      }
   }

   public void update( Observable o, Object arg )
   {
      // Update list of action buttons
      actionsPanel.removeAll();

      ActionMap am = getActionMap();

      for (UserAdministrationButtonTemplate buttonOrder : UserAdministrationButtonTemplate.values())
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
                  actionsPanel.add( Box.createVerticalStrut( 2 ) );
                  action1.putValue( "sourceButton", button );
               }
            }
         }
      }

      if (o == model)
      {
         contactBinder.updateWith( model.getContact() );
         phoneNumberBinder.updateWith(model.getPhoneNumber());
         emailBinder.updateWith(model.getEmailAddress());

         String messageDeliveryType = model.getMessageDeliveryType();
         if ("email".equalsIgnoreCase(messageDeliveryType))
         {
            emailButton.setSelected(true);
         } else
         {
            noneButton.setSelected(true);
         }
      } else
      {
         Property property = (Property) arg;
         if (property.qualifiedName().name().equals("name"))
         {
            try
            {
               model.changeName((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException( CaseResources.could_not_change_name, e);
            }
         } else if (property.qualifiedName().name().equals("phoneNumber"))
         {
            try
            {
               model.changePhoneNumber((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException(
                     CaseResources.could_not_change_phone_number, e);
            }
         } else if (property.qualifiedName().name().equals("emailAddress"))
         {
            try
            {
               model.changeEmailAddress((String) property.get());
            } catch (ResourceException e)
            {
               throw new OperationException(
                     CaseResources.could_not_change_email_address, e);
            }
         }
      }

      revalidate();
      repaint();
   }
}
