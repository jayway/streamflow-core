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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.structure.Module;
import org.restlet.representation.Representation;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PermissionsDTO;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.caselog.CaseLogModel;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationModel;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormsModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralModel;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Model for the info and actions on a case.
 */
public class CaseModel
   extends ResourceModel<CaseDTO>
{
   @Structure
   Module module;

   @Uses
   CasesModel casesModel;

   public void refresh()
   {
      super.refresh();

      CaseDTO caseDTO = getIndex();
   }

   public void open()
   {
      client.command( "open" );
   }

   public void assignToMe()
   {
      client.command( "assign" );
   }
   
   public void assignTo( LinkValue link)
   {
      client.postLink( link );
   }

   public void close()
   {
      client.command( "close" );
   }

   public void delete()
   {
      client.delete();
   }

   public void restrict()
   {
      client.command( "restrict" );
   }

   public void unrestrict()
   {
      client.command( "unrestrict" );
   }

   public EventList<TitledLinkValue> getPossibleAssignTo()
   {
      BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

      LinksValue linksValue = client.query( "possibleassignees", LinksValue.class );
      list.addAll( (Collection) linksValue.links().get() );

      return list;
   }

   public EventList<TitledLinkValue> getPossibleSendTo()
   {
      BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

      LinksValue linksValue = client.query( "possiblesendto", LinksValue.class );
      list.addAll( (Collection) linksValue.links().get() );

      return list;
   }

   public void sendTo( LinkValue linkValue )
   {
      client.postLink( linkValue );
   }

   public void reopen()
   {
      client.command( "reopen" );
   }

   public void unassign()
   {
      client.command( "unassign" );
   }

   public void onHold()
   {
      client.command( "onhold" );
   }

   public void resume()
   {
      client.command( "resume" );
   }

   public EventList<TitledLinkValue> getPossibleResolutions()
   {
      BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

      LinksValue linksValue = client.query( "possibleresolutions", LinksValue.class );
      list.addAll( (Collection) linksValue.links().get() );

      return list;
   }

   public void resolve( LinkValue linkValue )
   {
      client.postLink( linkValue );
   }

   public void formOnClose()
   {
      client.command( "formonclose" );
   }

   public void formOnRemove()
   {
      client.command( "formondelete" );
   }

   public PermissionsDTO permissions()
   {
      return client.query( "permissions", PermissionsDTO.class );
   }

   public File export(CaseOutputConfigDTO config) throws IOException
   {
      Representation representation = client.query("exportpdf", Representation.class, config);

      String name = representation.getDisposition().getFilename();
      String[] fileNameParts = name.split( "\\." );
      File file = File.createTempFile( fileNameParts[0] + "_", "." + fileNameParts[1] );

      Inputs.byteBuffer( representation.getStream(), 1024 ).transferTo( Outputs.byteBuffer( file ) );

      return file;
   }

   public void reinstate()
   {
      client.command( "reinstate" );
   }

   public void read()
   {
      client.postCommand( "read" );
   }

   public void markunread()
   {
      client.postCommand( "markunread" );
   }

   public void markread()
   {
      client.postCommand( "markread" );
   }

    public void requirecasetype()
    {
        //client.postCommand( "requirecasetype" );
    }

   public CaseGeneralModel newGeneralModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(CaseGeneralModel.class).use(client.getSubClient("general" )).newInstance();
   }

   public CaseLogModel newCaseLogModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(CaseLogModel.class).use(client.getSubClient("caselog" )).newInstance();
   }

   public CaseSubmittedFormsModel newSubmittedFormsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(CaseSubmittedFormsModel.class).use(client.getSubClient("submittedforms" )).newInstance();
   }

   public ContactsModel newContactsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(ContactsModel.class).use(client.getSubClient("contacts" )).newInstance();
   }

   public ConversationsModel newConversationsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(ConversationsModel.class).use(client.getSubClient("conversations" )).newInstance();
   }

   public AttachmentsModel newAttachmentsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(AttachmentsModel.class).use(client.getSubClient("attachments" )).newInstance();
   }

   public ConversationModel newHistoryModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(ConversationModel.class).use(client.getSubClient("history" )).newInstance();
   }
}