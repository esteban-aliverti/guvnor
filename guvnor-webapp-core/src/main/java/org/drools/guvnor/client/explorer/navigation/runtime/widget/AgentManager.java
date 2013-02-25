/*
 * Copyright 2005 JBoss Inc
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
package org.drools.guvnor.client.explorer.navigation.runtime.widget;

import org.drools.guvnor.client.messages.Constants;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.drools.guvnor.client.common.ErrorPopup;
import org.drools.guvnor.client.explorer.ClientFactory;
import org.drools.guvnor.client.rpc.AssetPageRequest;
import org.drools.guvnor.client.rpc.AssetPageRow;
import org.drools.guvnor.client.rpc.AssetServiceAsync;
import org.drools.guvnor.client.rpc.DetailedSerializationException;
import org.drools.guvnor.client.rpc.Module;
import org.drools.guvnor.client.rpc.ModuleServiceAsync;
import org.drools.guvnor.client.rpc.PageResponse;

/**
 * This controls category administration.
 */
public class AgentManager extends Composite {

    @UiField
    protected Tree packageTree;
    @UiField
    protected Tree agentTree;
    @UiField
    protected Button btnDeploy;
    @UiField
    protected Button btnUndeploy;
    private Constants constants = ((Constants) GWT.create(Constants.class));
    private static List<String> allowedAssetTypes = new ArrayList<String>() {
        {
            this.add("changeset");
        }
    };
    //Services
    private ModuleServiceAsync packageService;
    private final ClientFactory clientFactory;
    private final AssetServiceAsync assetService;

    // UI
    interface AgentManagerBinder
            extends
            UiBinder<Widget, AgentManager> {
    }
    private static AgentManagerBinder uiBinder = GWT.create(AgentManagerBinder.class);

    public AgentManager(ClientFactory clientFactory) {
        this.initWidget(uiBinder.createAndBindUi(this));

        this.clientFactory = clientFactory;

        this.packageService = this.clientFactory.getModuleService();
        this.assetService = this.clientFactory.getAssetService();

        this.initializePackageTree();
        this.initializeAgentTree();
    }

    private void initializePackageTree() {
        //Global Area Data
        this.packageService.loadGlobalModule(new AsyncCallback<Module>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorPopup.showMessage(new DetailedSerializationException("Error listing Global Area information!",caught.getMessage()));
            }

            @Override
            public void onSuccess(Module result) {
                populatePackageTree(result,
                        null);
            }
        });

        //Packages Data
        this.packageService.listModules(new AsyncCallback<Module[]>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorPopup.showMessage(new DetailedSerializationException("Error listing package information!",caught.getMessage()));
            }

            @Override
            public void onSuccess(Module[] result) {
                for (int i = 0; i < result.length; i++) {
                    final Module packageConfigData = result[i];
                    populatePackageTree(packageConfigData,
                            null);
                }
            }
        });
    }

    private void populatePackageTree(final Module packageConfigData,
            final TreeItem rootItem) {

        final TreeItem packageItem = new TreeItem(packageConfigData.getName());

        AssetPageRequest request = new AssetPageRequest(packageConfigData.uuid, allowedAssetTypes, true);
        request.setPageSize(100);
        
        assetService.findAssetPage(request, new AsyncCallback<PageResponse<AssetPageRow>>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorPopup.showMessage(new DetailedSerializationException("Error listing assets of package '" + packageConfigData.name + "'!",caught.getMessage()));
            }

            @Override
            public void onSuccess(PageResponse<AssetPageRow> result) {
                List<AssetPageRow> pageRowList = result.getPageRowList();
                Collections.sort(pageRowList, new Comparator<AssetPageRow>(){

                    @Override
                    public int compare(AssetPageRow o1, AssetPageRow o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    
                });
                
                for (AssetPageRow assetPageRow : pageRowList) {
                    packageItem.addItem(createPackageTreeItem(assetPageRow.getName(),assetPageRow.getUuid()));
                }

            }
        });

        //if no rootItem, then add the node directly to the tree
        if (rootItem == null) {
            this.packageTree.addItem(packageItem);
        } else {
            rootItem.addItem(packageItem);
        }

    }

    private TreeItem createPackageTreeItem(String label,
            Object userObject) {
        TreeItem treeItem = new TreeItem(label);
        treeItem.setUserObject(userObject);

        return treeItem;
    }

    private void initializeAgentTree() {
        TreeItem agentItem1 = new TreeItem("Agent 1");
        TreeItem agentItem2 = new TreeItem("Agent 2");
        TreeItem agentItem3 = new TreeItem("Agent 5");
        
        this.agentTree.addItem(agentItem1);
        this.agentTree.addItem(agentItem2);
        this.agentTree.addItem(agentItem3);
        
    }
}
