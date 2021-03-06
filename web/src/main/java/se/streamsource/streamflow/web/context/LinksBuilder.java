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
package se.streamsource.streamflow.web.context;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.domain.Describable;

/**
 * Builder for making it easier to create LinksValue/LinkValue in a Streamflow roleMap
 */
public class LinksBuilder
   extends se.streamsource.dci.value.link.LinksBuilder<LinksBuilder>
{
   public LinksBuilder( ValueBuilderFactory vbf )
   {
      super(vbf);
   }

   public LinksBuilder addDescribables( Iterable<? extends Describable> items )
   {
      for (Describable item : items)
      {
         addLink( item.getDescription(), item.toString() );
      }

      return this;
   }

   public LinksBuilder addDescribable( Describable item )
   {
      addLink( item.getDescription(), item.toString() );
      return this;
   }

   public LinksBuilder addDescribable( Describable item, Describable group )
   {
      return addDescribable( item, group.getDescription() );
   }

   public LinksBuilder addDescribable( Describable item, String group )
   {
      addLink( item.getDescription(), EntityReference.getEntityReference( item ), group, null );
      return this;
   }

   public LinksBuilder addDescribable( Describable item, String group, String classes )
   {
      addLink( item.getDescription(), EntityReference.getEntityReference( item ), group, classes );
      return this;
   }

   public LinksValue newLinks()
   {
      return linksBuilder.newInstance();
   }
}