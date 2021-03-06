/*
 * Copyright 2011 JBoss Inc
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

package org.drools.simulation.impl;

import java.util.HashMap;
import java.util.Map;

import org.kie.simulation.Simulation;
import org.kie.simulation.SimulationPath;

public class SimulationImpl implements Simulation {

    private Map<String, SimulationPath> paths;
    
    public SimulationImpl() {
        this.paths = new HashMap<String, SimulationPath>();
    }
    
    public Map<String, SimulationPath> getPaths() {
        return this.paths;
    }
        
}
