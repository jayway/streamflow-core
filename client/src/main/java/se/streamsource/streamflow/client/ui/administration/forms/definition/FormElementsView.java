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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import static org.qi4j.api.util.Iterables.filter;
import static org.qi4j.api.util.Iterables.first;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.events;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.FormElementItemListCellRenderer;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.util.Strings;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class FormElementsView
      extends JSplitPane
      implements TransactionListener

{
   @Service
   private DialogService dialogs;

   @Structure
   Module module;

   private JList list;

   private FormPagesModel model;
   
   private ActionMap am;



   public FormElementsView( @Service ApplicationContext context,
                            @Uses final FormPagesModel model)
   {
      this.model = model;

      am = context.getActionMap( this );

      setBorder( Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      setRightComponent( new JPanel() );
      setBorder( BorderFactory.createEmptyBorder() );

      setDividerLocation( 350 );
      setOneTouchExpandable( true );

      initMaster( new EventListModel<LinkValue>( model.getUnsortedList() ),
            new DetailFactory() {
               public Component createDetail( LinkValue detailLink )
               {
                  if ( detailLink == null ) return new JPanel();
                  LinkValue link = getSelectedValue();
                  if (link.rel().get().equals("page"))
                  {
                     return module.objectBuilderFactory().newObjectBuilder(PageEditView.class).use( model.newResourceModel( link ) ).newInstance();
                  } else
                     return module.objectBuilderFactory().newObjectBuilder(FieldEditView.class).use( model.newResourceModel( link )).newInstance();
               }
            },
            am.get( "addPage" ), am.get( "addField" ), am.get( "remove" ), am.get( "up" ), am.get( "down" ));


      list.setCellRenderer( new FormElementItemListCellRenderer() );
      list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get("addField"), am.get("remove")) );
      list.getSelectionModel().addListSelectionListener(
            new SelectionActionEnabler( am.get( "up" ), am.get( "down" ) )
            {

               @Override
               public boolean isSelectedValueValid( Action action )
               {
                  boolean result = true;
                  try
                  {
                     int selectedIndex = list.getSelectedIndex();
                     LinkValue link = (LinkValue) list.getSelectedValue();

                     if (action.equals( am.get( "up" ) ))
                     {
                        if (link.rel().get().equals("page"))
                        {
                           if (selectedIndex == 0)
                              result = false;
                        } else if (link.rel().get().equals("field"))
                        {
                           LinkValue previous = (LinkValue) list.getModel().getElementAt( selectedIndex - 1 );
                           if (previous.rel().get().equals("page"))
                              result = false;
                        }
                     } else if (action.equals( am.get( "down" ) ))
                     {
                        if (link.rel().get().equals("page"))
                        {
                           if (selectedIndex == lastPageIndex())
                              result = false;
                        } else
                        {
                           if (selectedIndex == list.getModel().getSize() - 1 ||
                                 ((LinkValue)list.getModel().getElementAt( selectedIndex + 1 )).rel().get().equals("page"))
                              result = false;
                        }
                     }
                  } catch (IndexOutOfBoundsException e)
                  {
                     // TODO is there a way to fix the glazedlists outofbounds exception due to concurrent update other than to consume the exception
                     // tried with wrapping the BasicEventList into GlazedLists.threadSafeList( eventlist ) to no avail!!
                     // The problem appears on adding and removing elements causing a server refresh that calls clear and addAll on the event list
                     // resulting in an invalid selection index.
                     result = false;
                  }
                  return result;
               }

               private int lastPageIndex()
               {
                  int lastIndex = -1;
                  ListModel listModel = list.getModel();
                  for (int i = 0; i < listModel.getSize(); i++)
                  {
                     LinkValue link = (LinkValue) listModel.getElementAt( i );
                     if ( link.rel().get().equals( "page" ) )
                        lastIndex = i;
                  }
                  return lastIndex;
               }
            } );

      new RefreshWhenShowing(this, model);

   }

   @org.jdesktop.application.Action
   public Task addField()
   {
      final LinkValue page = findSelectedPage(  getSelectedValue() );
      FieldCreationModel fieldCreationModel = module.objectBuilderFactory().newObjectBuilder( FieldCreationModel.class ).use( model.getClient().getSubClient( page.id().get() ) ).newInstance();
      
      final FieldCreationDialog dialog = module.objectBuilderFactory().newObjectBuilder(FieldCreationDialog.class).use( fieldCreationModel ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_field_to_form ) );

      if ( !Strings.empty( dialog.name() ) && dialog.getAddLink() != null )
      {
         list.clearSelection();
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.addField( page, dialog.name(), dialog.getAddLink() );
            }
         };
      }

      return null;
   }

   private LinkValue findSelectedPage( LinkValue selected )
   {
      if (selected.rel().get().equals("page"))
      {
         return selected;
      } else
      {
         int i1 = selected.href().get().indexOf( selected.id().get() );
         String id = selected.href().get().substring( 0, i1-1 );
         for (LinkValue link : model.getList())
         {
            if (id.equals( link.id().get() ))
               return link;
         }
         return null;
      }
   }

   @org.jdesktop.application.Action
   public Task addPage()
   {
      final NameDialog dialog = module.objectBuilderFactory().newObject(NameDialog.class);
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( AdministrationResources.add_page_title ) );

      if (!Strings.empty( dialog.name() ))
      {
         list.clearSelection();
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.addPage( dialog.name() );
            }
         };
      } else
         return null;
   }


   @org.jdesktop.application.Action
   public Task remove()
   {
      final LinkValue selected = getSelectedValue();
      if (selected != null)
      {
         ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
         dialog.setRemovalMessage( selected.text().get() );
         dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
         if (dialog.isConfirmed())
         {
            return new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.remove( selected );
               }
            };
         }
      }

      return null;
   }

   @org.jdesktop.application.Action
   public Task up()
   {
      final LinkValue selected = getSelectedValue();
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.move( selected, "up" );
         }
      };
   }

   @org.jdesktop.application.Action
   public Task down()
   {
      final LinkValue selected = getSelectedValue();
      if (selected != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.move( selected, "down" );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( withNames("removedPage","removedField" ), transactions ))
      {
         list.clearSelection();
      }

      if (Events.matches( withNames("changedDescription", "removedPage","removedField", "movedField", "movedPage" ), transactions ))
      {
         model.refresh();
      }

      DomainEvent event = first( filter( withNames("createdField", "createdPage", "movedField", "movedPage"), events(transactions ) ));
      if (event != null)
      {
         String id = EventParameters.getParameter( event, 1 );
         for (LinkValue link : model.getUnsortedList())
         {
            if (link.href().get().endsWith( id+"/" ))
            {
               list.setSelectedValue( link, true );
               if( event.name().get().equals( "createdField" ))
               {
                  Component c = Iterables.first( Iterables.filter( new Specification<Component>()
                  {
                     public boolean satisfiedBy( Component c )
                     {
                        if ( c instanceof StreamflowButton  &&
                              ((ApplicationAction) ((StreamflowButton) c).getAction()).getName().equals( "addField" ))
                           return true;
                        return false;
                     }
                  }, WindowUtils.getAllComponents( this ) ) );

                  if( c != null )
                     c.requestFocusInWindow();
               }

               break;
            }
         }
      }
   }

   protected void initMaster( EventListModel<LinkValue> listModel, final DetailFactory factory, Action... actions)
   {
      list = new JList(listModel);
      list.setCellRenderer( new LinkListCellRenderer() );

      JScrollPane scrollPane = new JScrollPane( list );

      JPanel master = new JPanel(new BorderLayout());
      master.add( scrollPane, BorderLayout.CENTER );

      // Toolbar
      JPanel toolbar = new JPanel();
      for (Action action : actions)
      {
         toolbar.add( new StreamflowButton( action ) );
      }

      master.add( toolbar, BorderLayout.SOUTH);

      setLeftComponent( master );

      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               setRightComponent( factory.createDetail( getSelectedValue() ));
            }
         }
      } );
   }

   public interface DetailFactory
   {
      Component createDetail(LinkValue detailLink);
   }

   private LinkValue getSelectedValue()
   {
      return (LinkValue) list.getSelectedValue();
   }
}