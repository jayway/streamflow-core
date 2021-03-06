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
package se.streamsource.streamflow.client.util;

import se.streamsource.dci.value.link.LinkValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormElementItemListCellRenderer
   extends DefaultListCellRenderer
{
   public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
   {
      if ( value instanceof LinkValue )
      {
         LinkValue link = (LinkValue) value;
         String val = link.text().get();

         if (link.rel().get().equals("page"))
         {
            Component component = super.getListCellRendererComponent( list, val, index, isSelected, cellHasFocus );
            setFont( getFont().deriveFont( Font.ITALIC ));
            return component;
         } else
         {
            return super.getListCellRendererComponent( list, "   "+val, index, isSelected, cellHasFocus );
         }

      }
      return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
   }
}