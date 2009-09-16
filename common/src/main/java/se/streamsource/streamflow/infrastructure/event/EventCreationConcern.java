/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.infrastructure.event;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.json.JSONObject;
import se.streamsource.streamflow.infrastructure.json.JSONStringer;
import se.streamsource.streamflow.infrastructure.json.JSONWriter;

import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.Principal;
import java.util.Date;
import java.util.Iterator;

/**
 * Generate event
 */
@AppliesTo(Event.class)
public class EventCreationConcern
        extends GenericConcern
{
    @Structure
    UnitOfWorkFactory uowf;

    @This
    EntityComposite entity;

    @Structure
    ValueBuilderFactory vbf;

    @Service
    IdentityGenerator idGenerator;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (args[0] == DomainEvent.CREATE)
        {
            // Create event
            ValueBuilder<DomainEvent> builder = vbf.newValueBuilder(DomainEvent.class);

            DomainEvent prototype = builder.prototype();
            prototype.name().set(method.getName());
            prototype.entityType().set(entity.type().getName());
            prototype.on().set(new Date());
            prototype.entity().set(entity.identity().get());

            Subject subject = Subject.getSubject(AccessController.getContext());
            if (subject == null)
                prototype.by().set("unknown");
            else
            {
                Iterator<Principal> iterator = subject.getPrincipals().iterator();
                if (iterator.hasNext())
                    prototype.by().set(iterator.next().getName());
                else
                    prototype.by().set("unknown");
            }

            prototype.identity().set(idGenerator.generate(DomainEvent.class));
            prototype.usecase().set(uowf.currentUnitOfWork().usecase().name());

            JSONStringer json = new JSONStringer();
            JSONWriter params = json.object();
            for (int i = 1; i < args.length; i++)
            {
                params.key("param" + i);
                if (args == null)
                    params.value(JSONObject.NULL);
                else
                    params.value(args[i]);
            }
            json.endObject();
            prototype.parameters().set(json.toString());
            DomainEvent event = builder.newInstance();

            args[0] = event;
        }

        return next.invoke(proxy, method, args);
    }
}