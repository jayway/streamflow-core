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

package se.streamsource.dci.test.interactions.jmx;

import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.value.StringValue;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JAVADOC
 */
public class MBeanAttributeInteractions
      extends InteractionsMixin
      implements IndexInteraction<Value>
{
   public Value index()
   {
      try
      {
         Object attribute = context.get( MBeanServer.class ).getAttribute( context.get( ObjectName.class ), context.get( MBeanAttributeInfo.class ).getName() );

         if (attribute instanceof TabularDataSupport)
         {
            TabularDataSupport table = (TabularDataSupport) attribute;
            ValueBuilder<TabularDataValue> builder = module.valueBuilderFactory().newValueBuilder( TabularDataValue.class );
            Set<Map.Entry<Object, Object>> entries = table.entrySet();
            List<List<String>> cells = builder.prototype().cells().get();
            for (Map.Entry<Object, Object> entry : entries)
            {
               CompositeDataSupport cds = (CompositeDataSupport) entry.getValue();
               String key = cds.get("key").toString();
               String value = cds.get("value" ).toString();

               List<String> row = new ArrayList<String>();
               row.add( key );
               row.add(value);
               cells.add( row );
            }
            return builder.newInstance();
         } else
         {
            ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
            builder.prototype().string().set( attribute.toString() );
            return builder.newInstance();
         }
      } catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public void update( StringValue newValue ) throws InstanceNotFoundException, InvalidAttributeValueException, ReflectionException, AttributeNotFoundException, MBeanException
   {
      Attribute attribute = new Attribute( context.get( MBeanAttributeInfo.class ).getName(), newValue.string().get() );
      context.get( MBeanServer.class ).setAttribute( context.get( ObjectName.class ), attribute );
   }
}