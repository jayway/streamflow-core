package se.streamsource.streamflow.client.util.mapquest;

import java.io.IOException;

import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/** The mapquest nominatim geo query service. See http://open.mapquestapi.com/nominatim.
 */
public class MapquestNominatimService {

   private String nominatimBaseUrl = "http://open.mapquestapi.com/nominatim/v1";
   private JacksonConverter converter = new JacksonConverter();

   MapquestQueryResult reverseLookup(double latitude, double longitude) {
      String url = reverseLookupQueryUrl(latitude, longitude);
      return getObject(MapquestQueryResult.class, url);
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
      return String.format("%s/reverse?lat=%f&lon=%f&format=json", nominatimBaseUrl, latitude, longitude);
   }

   public static void main(String[] args) {
      MapquestQueryResult result = new MapquestNominatimService().reverseLookup(55.681, 12.577);
      System.out.println(result);
   }
}
