/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.util.Function;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.dci.value.table.gdq.OrderByDirection;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Collections.reverseOrder;

/**
 * JAVADOC
 */
public class TableBuilder
{
   private static final Logger LOGGER = LoggerFactory.getLogger( TableBuilder.class );
   protected ValueBuilderFactory vbf;
   private Map<String, TableBuilderFactory.Column> columns;
   private TableQuery tableQuery;

   protected ValueBuilder<TableValue> tableBuilder;

   protected ValueBuilder<RowValue> rowBuilder;

   public TableBuilder(ValueBuilderFactory vbf)
   {
      this.vbf = vbf;

      tableBuilder = vbf.newValueBuilder(TableValue.class);
   }

   public TableBuilder(ValueBuilderFactory vbf, Map<String, TableBuilderFactory.Column> columns, TableQuery tableQuery)
   {
      this.vbf = vbf;
      this.columns = columns;
      this.tableQuery = tableQuery;

      tableBuilder = vbf.newValueBuilder(TableValue.class);

      if (tableQuery.select().equals("*"))
      {
         for (TableBuilderFactory.Column column : columns.values())
         {
            column(column.getId(), column.getLabel(), column.getType());
         }
      } else
      {
         for (String columnName : tableQuery.select())
         {
            TableBuilderFactory.Column column = columns.get(columnName.trim());
            if (column != null)
               column(column.getId(), column.getLabel(), column.getType());
         }
      }
   }

   public TableBuilder column(String id, String label, String type)
   {
      ValueBuilder<ColumnValue> builder = vbf.newValueBuilder(ColumnValue.class);
      builder.prototype().id().set(id);

      builder.prototype().label().set(label);
      builder.prototype().columnType().set(type);
      tableBuilder.prototype().cols().get().add(builder.newInstance());
      return this;
   }

   public TableBuilder rows(Iterable<?> rowObjects)
   {
      boolean no_format = false;
      boolean no_values = false;
      if (tableQuery != null && tableQuery.options() != null)
      {
         if (tableQuery.options().contains("no_format"))
            no_format = true;
         if (tableQuery != null && tableQuery.options().contains("no_values"))
            no_values = true;
      }

      for (Object rowObject : rowObjects)
      {
         if( rowObject == null )
         {
            LOGGER.warn( "Encountered null object while building row.");
            continue;
         }
         row();
         for (ColumnValue columnValue : tableBuilder.prototype().cols().get())
         {
            Object v = null;
            String f = null;
            Function valueFunction = columns.get(columnValue.id().get()).getValueFunction();
            if (!no_values && valueFunction != null)
               v = valueFunction.map(rowObject);
            Function formattedFunction = columns.get(columnValue.id().get()).getFormattedFunction();
            if (!no_format && formattedFunction != null)
               f = (String) formattedFunction.map(rowObject);
            else if (v != null)
            {
               if (columnValue.columnType().get().equals(TableValue.DATETIME))
                  f = DateFunctions.toUtcString((Date) v);
               else if (columnValue.columnType().get().equals(TableValue.DATE))
                  f = new SimpleDateFormat( "yyyy-MM-dd").format((Date) v);
               else if (columnValue.columnType().get().equals(TableValue.TIME_OF_DAY))
                  f = new SimpleDateFormat( "HH:mm:ss").format((Date) v);
               else
                  f = v.toString();
            }

            cell(v, f);
         }
         endRow();
      }

      return this;
   }

   public TableBuilder row()
   {
      if (rowBuilder != null)
         endRow();

      rowBuilder = vbf.newValueBuilder(RowValue.class);
      return this;
   }

   public TableBuilder endRow()
   {
      tableBuilder.prototype().rows().get().add(rowBuilder.newInstance());
      rowBuilder = null;
      return this;
   }

   public TableBuilder cell(Object v, String f)
   {
      ValueBuilder<CellValue> cellBuilder = vbf.newValueBuilder(CellValue.class);
      cellBuilder.prototype().v().set(v);
      cellBuilder.prototype().f().set(f);
      rowBuilder.prototype().c().get().add(cellBuilder.newInstance());
      return this;
   }

   public TableBuilder orderBy()
   {
      // TODO support multiple order by
      if (tableQuery.orderBy() != null && tableQuery.orderBy().size() == 1)
      {
         // Sort table
         // Find sort column index

         String orderByName = tableQuery.orderBy().get(0).name;
         boolean descending = tableQuery.orderBy().get(0).direction == OrderByDirection.DESCENDING;

         int sortIndex = -1;
         List<ColumnValue> columnValues = tableBuilder.prototype().cols().get();
         for (int i = 0; i < columnValues.size(); i++)
         {
            ColumnValue columnValue = columnValues.get(i);
            if (columnValue.id().get().equals(orderByName))
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

            if (descending)
            {
               // Flip it
               comparator = reverseOrder(comparator);
            }

            Collections.sort(tableBuilder.prototype().rows().get(), comparator);
         }
      }

      return this;
   }

//   public TableBuilder orderBy()
//   {
//      if (tableQuery.orderBy() != null)
//      {
//         // Sort table
//         // Find sort column index
//         int sortIndex = -1;
//         List<ColumnValue> columnValues = tableBuilder.prototype().cols().get();
//         for (int i = 0; i < columnValues.size(); i++)
//         {
//            ColumnValue columnValue = columnValues.get(i);
//            if (columnValue.id().equals(tableQuery.orderBy()))
//            {
//               sortIndex = i;
//               break;
//            }
//
//         }
//
//         if (sortIndex != -1)
//         {
//            final int idx = sortIndex;
//            Comparator<RowValue> comparator = new Comparator<RowValue>()
//            {
//               public int compare(RowValue o1, RowValue o2)
//               {
//                  Object o = o1.c().get().get(idx).v().get();
//
//                  if (o != null && o instanceof Comparable)
//                  {
//                     Comparable c1 = (Comparable) o;
//                     Comparable c2 = (Comparable) o2.c().get().get(idx).v().get();
//                     return c1.compareTo(c2);
//                  } else
//                  {
//                     String f1 = o1.c().get().get(idx).f().get();
//                     String f2 = o2.c().get().get(idx).f().get();
//                     return f1.compareTo(f2);
//                  }
//               }
//            };
//
//            Collections.sort(tableBuilder.prototype().rows().get(), comparator);
//         }
//      }
//
//      return this;
//   }

   public TableBuilder paging()
   {
      // Paging
      int start = 0;
      int end = tableBuilder.prototype().rows().get().size();
      if (tableQuery.offset() != null)
         start = tableQuery.offset();
      if (tableQuery.limit() != null)
         end = Math.min(end, start + tableQuery.limit());

      if (!(start == 0 && end == tableBuilder.prototype().rows().get().size()))
         tableBuilder.prototype().rows().set(tableBuilder.prototype().rows().get().subList(start, end));

      return this;

   }

   public TableValue newTable()
   {
      if (rowBuilder != null)
         endRow();

      return tableBuilder.newInstance();
   }

   public void abortRow()
   {
      rowBuilder = null;
   }
}
