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
package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.types.CollectionType;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entitystore.helpers.JSONEntityState;
import org.qi4j.spi.property.PropertyType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link AbstractExportHelper} that exports 1 entity with values to database.
 * It supports all entities from {@link EntityInfo}.
 * <br/>
 * The main idea of export following: exists simple, complex property types of entity and collections of both.
 * One entity is mapped to one table in SQL (naming strategy is camel case to snack case).
 * <br/>
 * Simple types are native simple types ({@link Integer}, {@link Boolean}, {@link String} etc),
 * date types ({@link java.util.Date}, {@link org.joda.time.DateTime}), custom enums.
 * They are stored in the same table.
 * <br/>
 * Complex types are values. Value is an interface that extends {@link ValueComposite},
 * but not extends {@link org.qi4j.api.entity.Identity}. They are stored in the separate table.
 * They have relation on entity and auto generated id to help linking.
 * <br/>
 * Collection can be of both types. They are stored in the separate table with foreign key on entity.
 * <br/>
 * Value export is running through {@link AbstractExportHelper#processValueComposite(ValueComposite)}.
 *
 * @see ValueExportHelper
 */
public class EntityExportHelper extends AbstractExportHelper
{
   private List<PropertyType> existsProperties;
   private Iterable<AssociationType> existsAssociations;
   private Iterable<ManyAssociationType> existsManyAssociations;
   private Map<String, Object> subProps;
   private JSONObject entity;
   private ArrayList<PropertyType> allProperties;
   private ArrayList<ManyAssociationType> allManyAssociations;
   private ArrayList<AssociationType> allAssociations;
   private String className;

   public Map<String, Set<String>> help() throws Exception
   {
      final String identity = entity.getString( JSONEntityState.JSON_KEY_IDENTITY );

      try ( final ResultSet isExistRS = selectFromWhereId( tableName(), identity ) )
      {
         checkEntityExists( className, identity );

         if ( isExistRS.next() )
         {
            deleteEntityAndRelations( isExistRS.getString( JSONEntityState.JSON_KEY_IDENTITY ) );
         }
      }

      if ( entity.optBoolean( "_removed" ) )
      {
         return tables;
      }

      final StringBuilder query = mainUpdate();

      final Map<String, String> associations = updateAssociations( query );

      saveManyAssociations();

      final List<SingletonMap> subProperties = saveSubProperties( query );

      if ( !query.substring( query.length() - 4 ).equals( "SET " ) )
      {
         query
                 .deleteCharAt( query.length() - 1 )
                 .append( " WHERE " )
                 .append( escapeSqlColumnOrTable( "identity" ) )
                 .append( " = ?" );

         try ( final PreparedStatement statement = connection.prepareStatement( query.toString() ) )
         {
            addArguments( statement, associations, subProperties, identity );
            statement.executeUpdate();
         }

      }

      saveTablesState();

      return tables;

   }

   private void saveManyAssociations() throws Exception
   {
      final JSONObject manyAssociationHolder = entity.getJSONObject(JSONEntityState.JSON_KEY_MANYASSOCIATIONS);
      for ( ManyAssociationType existsManyAssociation : existsManyAssociations )
      {

         final Class<?> clazz = Class.forName( existsManyAssociation.type() );

         String associationTable = null;

         int i = 0;
         for ( EntityInfo info : EntityInfo.values() )
         {
            if ( clazz.isAssignableFrom( info.getEntityClass() ) )
            {
               if ( i == 0 )
               {
                  associationTable = toSnakeCaseFromCamelCase( info.getClassSimpleName() );
               } else
               {
                  associationTable = null;
               }
               i++;
            }
         }

         final String tableName = tableName() + "_" + toSnakeCaseFromCamelCase( existsManyAssociation.qualifiedName().name() ) + "_cross_ref";

         createCrossRefTableIfNotExists( tableName, associationTable, stringSqlType( 255 ), stringSqlType( 255 ) );

         final String name = existsManyAssociation.qualifiedName().name();
         final JSONArray array = manyAssociationHolder.getJSONArray( name );

         final String query = "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                 " (owner_id,link_id) VALUES (?,?)";

         try ( final PreparedStatement preparedStatement = connection.prepareStatement( query ) )
         {
            for ( int j = 0; ; j++ )
            {
               final String associationIdentity = array.optString( j );
               if ( associationIdentity.isEmpty() )
               {
                  break;
               }

               final Class<?> assocClassName = Class.forName( existsManyAssociation.type() );
               int k = 0;
               String associationClass = null;
               for ( EntityInfo entityInfo : EntityInfo.values() )
               {
                  if ( assocClassName.isAssignableFrom( entityInfo.getEntityClass() ) )
                  {
                     associationClass = entityInfo.getEntityClass().getName();
                     k++;
                  }
               }
               if ( k == 1 )
               {
                  checkEntityExists( associationClass, associationIdentity );
               }

               preparedStatement.setString( 1, entity.getString( JSONEntityState.JSON_KEY_IDENTITY ) );
               preparedStatement.setString( 2, associationIdentity );
               preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
         }


      }

   }

   private StringBuilder mainUpdate() throws SQLException, JSONException, ClassNotFoundException
   {

      StringBuilder query = new StringBuilder( "UPDATE  " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " SET " );


      for ( PropertyType existsProperty : existsProperties )
      {
         final String name = existsProperty.qualifiedName().name();

         if ( !name.equals( JSONEntityState.JSON_KEY_IDENTITY ) && subProps.get( name ) == null )
         {
            query
                    .append( escapeSqlColumnOrTable( toSnakeCaseFromCamelCase( name ) ) )
                    .append( " = ?," );
         }
      }

      return query;

   }

   private List<SingletonMap> saveSubProperties( StringBuilder query ) throws Exception
   {

      List<SingletonMap> subProperties = new ArrayList<>();
      final Set<String> keys = subProps.keySet();

      Set<String> triggerStatements = new LinkedHashSet<>();

      try ( final Statement statement = connection.createStatement() )
      {
         for ( String key : keys )
         {

            final Object value = subProps.get( key );

            if ( value instanceof Collection || value instanceof Map )
            {

               if ( value instanceof Collection
                       && Iterables.first( ( Iterable<?> ) value ) instanceof ValueComposite )
               {
                  List<SingletonMap> collectionOfValues = new ArrayList<>();
                  for ( Object o : ( Collection<?> ) value )
                  {
                     collectionOfValues.add( processValueComposite( ( ValueComposite ) o ) );
                  }

                  final String tableName = tableName() + "_" + toSnakeCaseFromCamelCase( key ) + "_cross_ref";

                  final String associationTable = ( String ) Iterables.first( collectionOfValues ).getValue();

                  createCrossRefTableIfNotExists( tableName, associationTable, stringSqlType( 255 ), detectSqlType( Integer.class ) );

                  final String insertSubProperties = "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                          " (owner_id,link_id) VALUES (?,?)";

                  try ( final PreparedStatement preparedStatement = connection.prepareStatement( insertSubProperties ) )
                  {

                     for ( SingletonMap val : collectionOfValues )
                     {
                        preparedStatement.setString( 1, entity.getString( JSONEntityState.JSON_KEY_IDENTITY ) );
                        preparedStatement.setInt( 2, ( Integer ) val.getKey() );
                        preparedStatement.addBatch();
                     }

                     preparedStatement.executeBatch();
                  }
               } else
               {
                  processCollection( key, value, new PreparedStatementStringBinder( entity.getString( JSONEntityState.JSON_KEY_IDENTITY ), stringSqlType( 255 ) ) );
               }
            } else if ( value instanceof ValueComposite )
            {

               query
                       .append( escapeSqlColumnOrTable( toSnakeCaseFromCamelCase( key ) ) )
                       .append( "=?," );

               final ValueComposite valueComposite = ( ValueComposite ) value;

               subProperties.add( processValueComposite( valueComposite ) );

               final String triggerStatement = addColumn( toSnakeCaseFromCamelCase( key ), detectType( valueComposite ), statement );

               if ( !triggerStatement.isEmpty() )
               {
                  triggerStatements.add( triggerStatement );
               }

            }

         }

         statement.executeBatch();
      }

      createTrigger( triggerStatements );

      return subProperties;
   }

   private Map<String, String> updateAssociations( StringBuilder query ) throws Exception
   {
      Map<String, String> associations = new LinkedHashMap<>();

      final JSONObject associationsHolder = entity.getJSONObject(JSONEntityState.JSON_KEY_ASSOCIATIONS);
      for ( AssociationType existsAssociation : existsAssociations )
      {
         final String name = existsAssociation.qualifiedName().name();
         final String identity = associationsHolder.getString( name );

         final Class<?> assocClassName = Class.forName( existsAssociation.type().name() );
         String associationClass = null;
         for ( EntityInfo entityInfo : EntityInfo.values() )
         {
            if ( assocClassName.isAssignableFrom( entityInfo.getEntityClass() ) )
            {
               associationClass = entityInfo.getEntityClass().getName();
            }
         }

         checkEntityExists( associationClass, identity );
         associations.put( toSnakeCaseFromCamelCase( name ), identity );
      }

      if ( associations.size() > 0 )
      {
         final Set<String> keys = associations.keySet();

         for ( String key : keys )
         {
            query
                    .append( escapeSqlColumnOrTable( toSnakeCaseFromCamelCase( key ) ) )
                    .append( " = ?," );
         }

      }

      return associations;
   }

   private void checkEntityExists( String type, String identity ) throws Exception
   {
      final String tableName = toSnakeCaseFromCamelCase( classSimpleName( type ) );
      try ( final ResultSet resultSet = selectFromWhereId( tableName, identity ) )
      {
         if ( !resultSet.next() )
         {
            final String qeury = "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                    " (" + escapeSqlColumnOrTable( "identity" ) + ")" + " VALUES (?)";
            try ( final PreparedStatement preparedStatement = connection.prepareStatement( qeury ) )
            {
               preparedStatement.setString( 1, identity );
               preparedStatement.executeUpdate();
            }
         }
      }

   }

   private void deleteEntityAndRelations( String identity ) throws SQLException, ClassNotFoundException, JSONException
   {

      //Delete sub property collection
      for ( PropertyType property : allProperties )
      {
         if ( property.type() instanceof CollectionType )
         {

            final String tableName;
            if ( ( ( CollectionType ) property.type() ).collectedType().isValue() )
            {
               tableName = tableName() + "_" + toSnakeCaseFromCamelCase( property.qualifiedName().name() ) + "_cross_ref";
            } else
            {
               tableName = tableName() + "_" + toSnakeCaseFromCamelCase( property.qualifiedName().name() ) + "_coll";
            }

            if ( tables.containsKey( tableName ) )
            {
               final String delete = "DELETE FROM " + escapeSqlColumnOrTable( tableName ) + " WHERE owner_id = ?";
               try ( final PreparedStatement preparedStatement = connection.prepareStatement( delete ) )
               {
                  preparedStatement.setString( 1, identity );
                  preparedStatement.executeUpdate();
               }
            }

         }
      }

      //Delete many associations
      for ( ManyAssociationType manyAssociation : allManyAssociations )
      {
         final String tableName = tableName() + "_" + toSnakeCaseFromCamelCase( manyAssociation.qualifiedName().name() ) + "_cross_ref";
         if ( tables.containsKey( tableName ) )
         {
            final String delete = "DELETE FROM " + escapeSqlColumnOrTable( tableName ) + " WHERE owner_id = ?";
            try ( final PreparedStatement preparedStatement = connection.prepareStatement( delete ) )
            {
               preparedStatement.setString( 1, identity );
               preparedStatement.executeUpdate();
            }
         }
      }

      //Set main entity all columns to NULL except identity

      final StringBuilder queryNullUpdate = new StringBuilder( "UPDATE " )
              .append( tableName() )
              .append( " SET " );

      final Set<String> columns = tables.get( tableName() );

      for ( PropertyType property : allProperties )
      {
         final String name = property.qualifiedName().name();
         final String columnName = toSnakeCaseFromCamelCase( name );
         if ( !name.equals( JSONEntityState.JSON_KEY_IDENTITY ) && columns.contains( columnName ) )
         {
            queryNullUpdate.append( escapeSqlColumnOrTable( columnName ) )
                    .append( "=NULL," );
         }
      }

      for ( AssociationType association : allAssociations )
      {
         final String name = association.qualifiedName().name();
         queryNullUpdate.append( escapeSqlColumnOrTable( toSnakeCaseFromCamelCase( name ) ) )
                 .append( "=NULL," );
      }

      final String query = queryNullUpdate
              .deleteCharAt( queryNullUpdate.length() - 1 )
              .append( " WHERE " )
              .append( escapeSqlColumnOrTable( "identity" ) )
              .append( " = ?" )
              .toString();

      if ( allProperties.size() + allAssociations.size() > 1 )
      {
         try ( final PreparedStatement preparedStatement = connection.prepareStatement( query ) )
         {
            preparedStatement.setString( 1, identity );
            preparedStatement.executeUpdate();
         }
      }

   }

   private void addArguments( PreparedStatement statement,
                              Map<String, String> associations,
                              List<SingletonMap> subProperties,
                              String identity
   ) throws JSONException, SQLException, ClassNotFoundException
   {
      final JSONObject propertiesHolder = entity.getJSONObject(JSONEntityState.JSON_KEY_PROPERTIES);
      int i = 1;
      for ( PropertyType existsProperty : existsProperties )
      {
         final Class type = Class.forName( existsProperty.type().type().name() );

         final String name = existsProperty.qualifiedName().name();

         if ( name.equals( JSONEntityState.JSON_KEY_IDENTITY ) || subProps.get( name ) != null )
         {
            continue;
         }

         setSimpleType( statement, type, propertiesHolder, name, i++ );

      }

      for ( String key : associations.keySet() )
      {
         statement.setString( i++, associations.get( key ) );
      }

      for ( SingletonMap subProperty : subProperties )
      {
         statement.setInt( i++, ( Integer ) subProperty.getKey() );
      }

      statement.setString( i, identity );

   }

   @Override
   protected String tableName()
   {
      return toSnakeCaseFromCamelCase( classSimpleName( className ) );
   }

   //setters


   public void setExistsProperties( Iterable<PropertyType> existsProperties )
   {
      this.existsProperties = new LinkedList<>();
      for ( PropertyType existsProperty : existsProperties )
      {
         this.existsProperties.add( existsProperty );
      }
   }

   public void setExistsAssociations( Iterable<AssociationType> existsAssociations )
   {
      this.existsAssociations = existsAssociations;
   }

   public void setExistsManyAssociations( Iterable<ManyAssociationType> existsManyAssociations )
   {
      this.existsManyAssociations = existsManyAssociations;
   }

   public void setSubProps( Map<String, Object> subProps )
   {
      this.subProps = subProps;
   }

   public void setEntity( JSONObject entity )
   {
      this.entity = entity;
   }

   public void setAllProperties( Set<PropertyType> allProperties )
   {
      this.allProperties = new ArrayList<>( allProperties );
   }

   public void setAllManyAssociations( Set<ManyAssociationType> allManyAssociations )
   {
      this.allManyAssociations = new ArrayList<>( allManyAssociations );
   }

   public void setAllAssociations( Set<AssociationType> allAssociations )
   {
      this.allAssociations = new ArrayList<>( allAssociations );
   }

   public void setClassName( String className )
   {
      this.className = className;
   }
}
