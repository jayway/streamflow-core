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

package se.streamsource.streamflow.web.context.surface.endusers;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.util.Function;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.surface.api.ClosedCaseDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

import java.util.Date;

/**
 * Context for closed case
 */
public class ClosedCaseContext
        implements IndexContext<ClosedCaseDTO>
{
   @Structure
   Module module;

   public ClosedCaseDTO index()
   {
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      ValueBuilder<ClosedCaseDTO> builder = vbf.newValueBuilder(ClosedCaseDTO.class);
      CaseEntity aCase = RoleMap.role(CaseEntity.class);
      builder.prototype().description().set(aCase.description().get());
      builder.prototype().creationDate().set(aCase.createdOn().get());
      Date closedDate = aCase.getHistoryMessage("closed").createdOn().get();
      builder.prototype().closeDate().set(closedDate);

      if (aCase.resolution().get() != null)
         builder.prototype().resolution().set(aCase.resolution().get().getDescription());

      builder.prototype().caseId().set(aCase.caseId().get());

      Owner owner = aCase.owner().get();
      builder.prototype().project().set(((Describable) owner).getDescription());

      return builder.newInstance();
   }

   public Iterable<Message> history(TableQuery tq)
   {
      Messages.Data messages = (Messages.Data) RoleMap.role(History.class).getHistory();
      return messages.messages();
   }
}