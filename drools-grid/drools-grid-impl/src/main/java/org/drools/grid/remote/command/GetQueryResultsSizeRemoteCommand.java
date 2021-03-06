/*
 * Copyright 2011 JBoss Inc..
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
package org.drools.grid.remote.command;


import org.drools.command.impl.GenericCommand;
import org.kie.command.Context;
import org.kie.command.World;
import org.kie.runtime.rule.QueryResults;

/**
 *
 * @author salaboy
 */
public class GetQueryResultsSizeRemoteCommand implements GenericCommand<Integer>{
    private String queryName;
    private String localId;
    
    public GetQueryResultsSizeRemoteCommand(String queryName, String localId) {
        this.queryName = queryName;
        this.localId = localId;
     
    }
    
    public Integer execute(Context context) {
        return ((QueryResults)context.getContextManager().getContext( "__TEMP__" ).get( this.localId )).size();
    }
    
}
