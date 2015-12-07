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
package se.streamsource.streamflow.api.administration.surface;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;

import java.util.Map;

/**
 * Link value
 */
public interface AccessPointDTO
      extends ValueComposite
{
   Property<LinkValue> accessPoint();

   @Optional
   Property<LinkValue> project();

   @Optional
   Property<LinkValue> caseType();

   @Optional
   Property<LinkValue> form();

   @Optional
   Property<LinkValue> template();

   @Optional
   Property<String> mailSelectionMessage();

   @Optional
   Property<RequiredSignatureValue> primarysign();

   @Optional
   Property<RequiredSignatureValue> secondarysign();

   @Optional
   Property<String> cssfile();
   
   @Optional
   Property<String> location();
   
   @Optional
   Property<Integer> zoomLevel();
   
   @UseDefaults
   Property<String> subject();

   @UseDefaults
   Property<Map<String, String>> messages();

   @UseDefaults
    Property<Boolean> replacementValues();

   @Optional
   Property<Integer> cookieExpirationHours();
}