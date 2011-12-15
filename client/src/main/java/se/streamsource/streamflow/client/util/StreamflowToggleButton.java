/**
 *
 * Copyright 2009-2011 Streamsource AB
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
package se.streamsource.streamflow.client.util;

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;

/**
 * Common ToggleButton class that handles styling
 * 
 * @author henrikreinhold
 *
 */
public class StreamflowToggleButton extends JButton
{
   private static final long serialVersionUID = -6757445621191820671L;

   private static final Insets INSETS = new Insets( 3, 1, 1, 3 );

   public StreamflowToggleButton(Action action)
   {
      super(action);
      setMargin( INSETS );
   }

}
