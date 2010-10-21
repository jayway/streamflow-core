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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.ui.administration.LinkValueListModel;
import se.streamsource.streamflow.domain.form.CreateFieldDTO;
import se.streamsource.streamflow.domain.form.FieldTypes;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

/**
 * JAVADOC
 */
public class FormElementsModel
      extends LinkValueListModel
{
   public FormElementsModel()
   {
      super( "formelements" );
   }

   public void addField( LinkValue pageItem, String name, FieldTypes fieldType )
   {
      ValueBuilder<CreateFieldDTO> builder = vbf.newValueBuilder( CreateFieldDTO.class );
      builder.prototype().name().set( name );
      builder.prototype().fieldType().set( fieldType );

      client.getClient( pageItem ).postCommand( "create", builder.newInstance() );
   }

   public void addPage( String pageName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( pageName );

      client.postCommand( "create", builder.newInstance() );
   }

   public void move( LinkValue item, String direction )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( direction );

      client.getClient( item ).putCommand( "move",  builder.newInstance() );
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches(transactions, Events.onEntities( client.getReference().getLastSegment() )))
      {
         refresh();
      }
   }
}