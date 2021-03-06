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
package se.streamsource.streamflow.client.ui.workspace.search;

import se.streamsource.streamflow.client.util.i18n;

/**
 * Class for translating search terms from application language to server default.
 */
public class SearchTerms
{

   public static String translate( String search )
   {
      // strip eventual white spaces around colon
      String translation = search.replaceAll("(\\s+)?:(\\s+)?", ":" );

      for(SearchTermsResources term: SearchTermsResources.values())
      {
         String searchTerm = i18n.text(term);

         if (translation.contains( searchTerm ))
         {
            if( term.equals(SearchTermsResources.today)
                  || term.equals(SearchTermsResources.yesterday)
                  || term.equals(SearchTermsResources.hour)
                  || term.equals(SearchTermsResources.week)
                  || term.equals(SearchTermsResources.me))
            {
               translation = translation.replace( searchTerm, term.name() );
            } else
            {
               translation = translation.replace( searchTerm, term.name() + ":" );
            }

         }
      }

      return translation;
   }
}
