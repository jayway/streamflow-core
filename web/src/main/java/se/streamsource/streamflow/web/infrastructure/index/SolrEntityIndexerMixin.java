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
package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.SchemaField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.service.Activatable;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.util.Translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JAVADOC Add JavaDoc
 */
public class SolrEntityIndexerMixin
      implements StateChangeListener, Activatable
{
   @Service
   private EmbeddedSolrService solr;

   @Uses
   private EntityStateSerializer stateSerializer;

   private ValueFactory valueFactory = new ValueFactoryImpl();

   private SolrServer server;
   private Map<String, SchemaField> indexedFields;

   Logger logger = LoggerFactory.getLogger( getClass() );

   public void activate() throws Exception
   {
      server = solr.getSolrServer( "sf-core" );
      SolrCore solrCore = solr.getSolrCore( "sf-core" );
      if( solrCore == null)
         return;
      try
      {
         indexedFields = solrCore.getLatestSchema().getFields();
      } finally
      {
         solrCore.close();
      }
   }

   public void passivate() throws Exception
   {
      server = null;
      indexedFields = null;
   }

   public void notifyChanges( Iterable<EntityState> entityStates )
   {
      try
      {

         // Figure out what to update
         List<String> deleted = null;
         List<SolrInputDocument> added = new ArrayList<SolrInputDocument>();
         for (EntityState entityState : entityStates)
         {
            if (entityState.entityDescriptor().entityType().queryable())
            {
               if (entityState.status().equals( EntityStatus.REMOVED ))
               {
                  if (deleted == null)
                     deleted = new ArrayList<String>();
                  deleted.add( entityState.identity().identity() );
               } else if (entityState.status().equals( EntityStatus.UPDATED ))
               {
                  added.add( indexEntityState( entityState, server ) );
               } else if (entityState.status().equals( EntityStatus.NEW ))
               {
                  added.add( indexEntityState( entityState, server ) );
               }
            }
         }

         // Send changes to Solr
         if (deleted != null)
            server.deleteById( deleted );
         if (!added.isEmpty())
            server.add( added );
      }
      catch (Throwable e)
      {
         logger.error( "Could not update Solr", e );
         //TODO What shall we do with the exception?
      }
   }

   private SolrInputDocument indexEntityState( final EntityState entityState,
                                               final SolrServer server )
         throws IOException, SolrServerException, JSONException
   {
      Graph graph = new GraphImpl();
      stateSerializer.serialize( entityState, false, graph );

      SolrInputDocument input = new SolrInputDocument();
      input.addField( "id", entityState.identity().identity() );
      input.addField( "type", entityState.entityDescriptor().entityType().type().name() );
      input.addField( "lastModified", new Date( entityState.lastModified() ) );

      for (Statement statement : graph)
      {
         SchemaField field = indexedFields.get( statement.getPredicate().getLocalName() );
         if (field != null)
         {
            if (statement.getObject() instanceof Literal)
            {
               String value = statement.getObject().stringValue();
               if (field.getType().getTypeName().equals( "json" ))
               {
                  if (value.charAt( 0 ) == '[')
                  {
                     JSONArray array = new JSONArray( value );
                     indexJson( input, array, field.getName() );
                  } else if (value.charAt( 0 ) == '{')
                  {
                     JSONObject object = new JSONObject( value );
                     indexJson( input, object, field.getName());
                  }
               } else
               {
                  input.addField( field.getName(), value );
               }
            } else if (statement.getObject() instanceof URI && !"type".equals( field.getName() ))
            {
               String value = statement.getObject().stringValue();
               value = value.substring( value.lastIndexOf( ':' ) + 1, value.length() );
               String name = field.getName();
               input.addField( name, value );
            } else if (statement.getObject() instanceof BNode)
            {
               Iterator<Statement> seq = graph.match( (Resource) statement.getObject(), new URIImpl( "http://www.w3.org/1999/02/22-rdf-syntax-ns#li" ), null, (Resource) null );
               while (seq.hasNext())
               {
                  Statement seqStatement = seq.next();
                  String value = seqStatement.getObject().stringValue();
                  value = value.substring( value.lastIndexOf( ':' ) + 1, value.length() );

                  input.addField( field.getName(), value );
               }
            }
         }

      }

      return input;
   }

   private void indexJson(SolrInputDocument input, Object object, String fieldName) throws JSONException
   {
      if (object instanceof JSONArray)
      {
         JSONArray array = (JSONArray) object;
         for (int i = 0; i < array.length(); i++)
            indexJson( input, array.get( i ), fieldName);
      } else if (object instanceof JSONObject)
      {
         JSONObject jsonObject = (JSONObject) object;
         Iterator keys = jsonObject.keys();
         while (keys.hasNext())
         {
            Object name = keys.next();
            Object value = jsonObject.get( name.toString() );
            if (value instanceof JSONObject || value instanceof JSONArray)
            {
               indexJson( input, value, fieldName);
            } else
            {
               SchemaField field = indexedFields.get( name.toString() );
               if (field != null)
               {
                  // if note is html formatted - remove html tags
                  if( "note".equals( name.toString() )  )
                  {
                     String contentType = "";
                     try
                     {
                        contentType = (String)jsonObject.get( "contentType" );
                     }catch (JSONException je )
                     {
                        //do nothing
                     }
                     if( Translator.HTML.equals( contentType ) )
                     {
                        input.addField( name.toString(), Translator.htmlToText( (String)jsonObject.get( name.toString() ) ) );
                     } else
                     {
                        input.addField( name.toString(), jsonObject.get( name.toString() ) );
                     }
                  } else
                  {
                     input.addField( name.toString(), jsonObject.get( name.toString() ) );
                  }
               }
            }
         }
      } else
      {
         if (object != null)
            input.addField(fieldName, object.toString());
      }
   }
}