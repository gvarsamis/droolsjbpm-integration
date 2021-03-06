/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools;

import org.kie.event.rule.ObjectInsertedEvent;
import org.kie.event.rule.ObjectRetractedEvent;
import org.kie.event.rule.ObjectUpdatedEvent;
import org.kie.event.rule.WorkingMemoryEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChangeCollector implements WorkingMemoryEventListener {

    private List<String> retracted;
    private List changes;




    public List<String> getRetracted() {
        return retracted;
    }


    public List getChanges() {
        return changes;
    }


    public void objectInserted(ObjectInsertedEvent event) {
        
    }

    public void objectUpdated(ObjectUpdatedEvent event) {
        if (changes == null) changes = new ArrayList();
        if (event.getObject() instanceof Cheese) {
            Cheese c = (Cheese) event.getObject();
            changes.add(c);
        }
    }

    public void objectRetracted(ObjectRetractedEvent event) {
        if (retracted == null) retracted = new ArrayList<String>();
        if (event.getOldObject() instanceof Cheese) {
            Cheese c = (Cheese) event.getOldObject();
            retracted.add(c.getType());
        }
    }
}
