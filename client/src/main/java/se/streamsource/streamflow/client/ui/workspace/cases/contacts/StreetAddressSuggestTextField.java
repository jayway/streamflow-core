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
package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.SuggestTextField;
import se.streamsource.streamflow.client.util.ValueBinder;

import javax.swing.JTextField;
import java.util.Observable;
import java.util.Observer;

/**
 * Specific Class to handle the live search for Street Names. It should also
 * update the City attribute which explains the need for this class.
 * 
 * @author henrikreinhold
 * 
 * @param
 */
public class StreetAddressSuggestTextField extends SuggestTextField<StreetSearchDTO>
   implements Observer
{

   private static final long serialVersionUID = -1952912369783423979L;
   private StreetAddressSuggestModel model;
   private final ValueBinder addressViewBinder;
   private final JTextField cityField;

   public StreetAddressSuggestTextField(StreetAddressSuggestModel model, JTextField cityField,
         ValueBinder addressViewBinder)
   {
      super( model );
      this.model = model;
      this.cityField = cityField;
      this.addressViewBinder = addressViewBinder;
      model.addObserver( this );

   }

   public void handleAcceptAction(StreetSearchDTO selectedItem)
   {
      getTextField().setText( selectedItem.address().get() );
      cityField.setText( selectedItem.area().get() );
      model.getContactModel().getAddress().address().set( selectedItem.address().get() );
      model.getContactModel().getAddress().city().set( selectedItem.area().get() );
      addressViewBinder.update( model.getContactModel().getAddress() );
      model.getContactModel().changeAddressAndCity( selectedItem.address().get(), selectedItem.area().get() );
   }

   public void handleSaveAction(final String text)
   {
      if (text != null && !text.equals( model.getContactModel().getAddress().address().get() ))
      {
         new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.getContactModel().changeAddress( text );
            }
         }.execute();

      }
   }

   public void update( Observable o, Object arg )
   {
      addressViewBinder.update( model.getContactModel().getAddress() );
   }
}
