/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.dci.value.table;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query value for Google Data queries.
 */
@Mixins(TableQuery.Mixin.class)
public interface TableQuery
        extends ValueComposite
{
   Property<String> tq();

   public String select();

   public String where();

   public String groupBy();

   public String pivot();

   public String orderBy();

   public String limit();

   public String offset();

   public String label();

   public String options();

   public TableValue applyTo(TableValue table);

   abstract class Mixin
           implements TableQuery
   {
      private static Collection<String> keywords = Arrays.asList("select", "where", "group by", "pivot", "order by", "limit", "offset", "label", "options");

      private Map<String, String> parts;

      public String select()
      {
         return getParts().get("select");
      }

      public String where()
      {
         return getParts().get("where");
      }

      public String groupBy()
      {
         return getParts().get("group by");
      }

      public String pivot()
      {
         return getParts().get("pivot");
      }

      public String orderBy()
      {
         return getParts().get("order by");
      }

      public String limit()
      {
         return getParts().get("limit");
      }

      public String offset()
      {
         return getParts().get("offset");
      }

      public String label()
      {
         return getParts().get("label");
      }

      public String options()
      {
         return getParts().get("options");
      }

      private Map<String, String> getParts()
      {
         if (parts == null)
         {
            parts = new HashMap<String, String>();

            String value = tq().get();
            List<String> values = Arrays.asList(value.split(" "));
            Collections.reverse(values);
            String currentPhrase = "";
            for (String str : values)
            {
               currentPhrase = str + currentPhrase;
               boolean found = false;
               for (String keyword : keywords)
               {
                  if (currentPhrase.startsWith(keyword + " "))
                  {
                     found = true;
                     parts.put(keyword, currentPhrase.substring(keyword.length() + 1));
                     currentPhrase = "";
                     break;
                  }
               }

               if (!found)
                  currentPhrase = " " + currentPhrase;
            }
         }

         return parts;
      }

      public TableValue applyTo(TableValue table)
      {
         ValueBuilder<TableValue> tableBuilder = table.buildWith();

/* TODO Finish label setting
         if (label() != null)
         {
            String[] labels = label().split(",");
            for (String label : labels)
            {
               String[] labelDef = label.split(" ",1);
               labelDef[1] = labelDef[1].trim();

            }
            for (int i = 0; i < tableBuilder.prototype().cols().get().size(); i++)
            {
               ColumnValue cellValue = tableBuilder.prototype().cols().get().get(i);
            }
         }
*/

         if (orderBy() != null)
         {
            // Sort table
            // Find sort column index
            int sortIndex = -1;
            List<ColumnValue> columnValues = table.cols().get();
            for (int i = 0; i < columnValues.size(); i++)
            {
               ColumnValue columnValue = columnValues.get(i);
               if (columnValue.id().equals(orderBy()))
               {
                  sortIndex = i;
                  break;
               }

            }

            if (sortIndex != -1)
            {
               final int idx = sortIndex;
               Comparator<RowValue> comparator = new Comparator<RowValue>()
               {
                  public int compare(RowValue o1, RowValue o2)
                  {
                     Object o = o1.c().get().get(idx).v().get();

                     if (o != null && o instanceof Comparable)
                     {
                        Comparable c1 = (Comparable) o;
                        Comparable c2 = (Comparable) o2.c().get().get(idx).v().get();
                        return c1.compareTo(c2);
                     } else
                     {
                        String f1 = o1.c().get().get(idx).f().get();
                        String f2 = o2.c().get().get(idx).f().get();
                        return f1.compareTo(f2);
                     }
                  }
               };

               Collections.sort(tableBuilder.prototype().rows().get(), comparator);
            }
         }

         // Paging
         int start = 0;
         int end = tableBuilder.prototype().rows().get().size();
         if (offset() != null)
            start = Integer.parseInt(offset());
         if (limit() != null)
            end = Math.min(end, start+ Integer.parseInt(limit()));

         if (!(start == 0 && end == tableBuilder.prototype().rows().get().size()))
            tableBuilder.prototype().rows().set(tableBuilder.prototype().rows().get().subList(start, end));

         return tableBuilder.newInstance();
      }
   }
}
