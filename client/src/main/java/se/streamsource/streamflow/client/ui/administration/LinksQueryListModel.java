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

package se.streamsource.streamflow.client.ui.administration;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;

import javax.swing.AbstractListModel;
import java.util.List;

public class LinksQueryListModel
      extends AbstractListModel
{
   private List<LinkValue> users;

   public LinksQueryListModel( @Uses CommandQueryClient client, @Uses String query )
   {
      try
      {
         users = client.query( query, LinksValue.class ).links().get();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_get_users, e );
      }
   }

   public int getSize()
   {
      return users == null ? 0 : users.size();
   }

   public Object getElementAt( int index )
   {
      return users == null ? null : users.get( index );
   }
}
