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
package se.streamsource.streamflow.client.util.mapquest;

import java.io.IOException;
import java.util.Locale;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The mapquest nominatim geo query service. See http://open.mapquestapi.com/nominatim.
 */
public class MapquestNominatimService {

   private static final Logger logger = LoggerFactory.getLogger(MapquestNominatimService.class);

   private String mapquestReverseLookupUrlPattern;
   private JacksonConverter converter = new JacksonConverter();

   public MapquestNominatimService(@Uses String mapquestReverseLookupUrlPattern) {
      this.mapquestReverseLookupUrlPattern = mapquestReverseLookupUrlPattern;
   }

   public MapquestQueryResult reverseLookup(double latitude, double longitude) {
      String url = reverseLookupQueryUrl(latitude, longitude);
      logger.info("Reverse geo lookup for: "+url);
      MapquestQueryResult result = getObject(MapquestQueryResult.class, url);
      logger.info("Reverse geo result: " + result);
      return result;
   }

   private <T> T getObject(Class<T> clazz, String url) {
      ClientResource clientResource = new ClientResource(url);
      Representation response = clientResource.get();
      T result;
      try {
         result = converter.toObject(response, clazz, clientResource);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      return result;
   }

   private String reverseLookupQueryUrl(double latitude, double longitude) {
      return String.format(Locale.US, mapquestReverseLookupUrlPattern, latitude, longitude);
   }

   public static void main(String[] args) {
      new MapquestNominatimService("http://open.mapquestapi.com/nominatim/v1/reverse?lat=%f&lon=%f&format=json").reverseLookup(55.681, 12.577);
   }
}
