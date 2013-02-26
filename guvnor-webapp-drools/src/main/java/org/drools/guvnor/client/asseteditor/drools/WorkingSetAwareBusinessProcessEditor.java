/*
 * Copyright 2011 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.client.asseteditor.drools;

import com.google.gwt.event.shared.EventBus;
import java.util.Set;
import org.drools.guvnor.client.asseteditor.BusinessProcessEditor;
import org.drools.guvnor.client.asseteditor.RuleViewer;
import org.drools.guvnor.client.explorer.ClientFactory;
import org.drools.guvnor.client.moduleeditor.drools.WorkingSetManager;
import org.drools.guvnor.client.rpc.Asset;

/**
 * An extension of org.drools.guvnor.client.asseteditor.BusinessProcessEditor
 * that sends information about active working-sets to process designer
 */
public class WorkingSetAwareBusinessProcessEditor extends BusinessProcessEditor {

    private Asset asset;
    
    public WorkingSetAwareBusinessProcessEditor(Asset asset, RuleViewer viewer, ClientFactory clientFactory, EventBus eventBus) {
        super(asset, viewer, clientFactory, eventBus);
        this.asset = asset;
    }

    /**
     * Adds information about active working-sets to designer's URL
     */
    @Override
    protected String preprocessURL(String url) {
        //Send workingset information
        Set<String> activeWorkingSetsUUIDs = WorkingSetManager.getInstance().getActiveAssetUUIDs(asset.metaData.moduleName);
        
        if (activeWorkingSetsUUIDs != null){
            for (String workingSetUUID : activeWorkingSetsUUIDs) {
                url += "&wsUuid="+workingSetUUID;
            }
        }
        return url;
    }
    
}
