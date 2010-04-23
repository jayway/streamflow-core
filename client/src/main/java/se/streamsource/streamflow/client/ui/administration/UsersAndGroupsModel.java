package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import java.util.Collection;

/**
 */
public class UsersAndGroupsModel
{
   @Uses
   CommandQueryClient client;

   public EventList<TitledLinkValue> getPossibleUsers()
   {
      try
      {
         BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

         LinksValue listValue = client.query( "possibleusers", LinksValue.class );
         list.addAll( (Collection) listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }

   public EventList<TitledLinkValue> getPossibleGroups()
   {
      try
      {
         BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

         LinksValue linksValue = client.query( "possiblegroups", LinksValue.class );
         list.addAll( (Collection) linksValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }

}
