/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.dci.restlet.client;

import org.restlet.Request;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public interface RequestWriter
{
   /**
    * Write the given request object to the request.
    *
    * @param requestObject
    * @param request
    * @return
    * @throws org.restlet.resource.ResourceException
    */
   public boolean writeRequest(Object requestObject, Request request)
      throws ResourceException;
}
