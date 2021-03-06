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
package org.streamsource.streamflow.statistic.web;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.util.prefs.Preferences;

/**
 * Data access object
 */
public class Dao
{
   private static String dbVendor;
   private static BasicDataSource ds;
   private static boolean bDataSourceInitialized=false;
   private static void initializeDataSource()
   {
      Preferences preference = Preferences.userRoot().node( "/streamsource/streamflow/StreamflowServer/streamflowds/properties" );
      String dbUrl = preference.get( "url", "n/a" );
      String dbUser = preference.get( "username", "n/a" );
      String dbPwd = preference.get( "password", "n/a" );
      String dbDriver = preference.get( "driver", "n/a" );

      dbVendor = preference.get( "dbVendor", "mysql" );

      ds = new BasicDataSource();
      ds.setDriverClassName( dbDriver );
      ds.setUrl( dbUrl );
      ds.setUsername( dbUser );
      ds.setPassword( dbPwd );

      bDataSourceInitialized=true;
   }

   public static void closeDataSource() throws Exception
   {
      ds.close();
   }

   public static DataSource getDataSource()
   {
      if (bDataSourceInitialized==false)
      {
         initializeDataSource();
      }

      return(ds);
   }

   public static String getDbVendor()
   {
      if( bDataSourceInitialized == false )
      {
         initializeDataSource();
      }

      return dbVendor;
   }
}
