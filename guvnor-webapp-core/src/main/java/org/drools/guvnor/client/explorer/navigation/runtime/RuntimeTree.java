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
package org.drools.guvnor.client.explorer.navigation.runtime;

import com.google.gwt.user.client.ui.IsWidget;
import org.drools.guvnor.client.explorer.ClientFactory;
import org.drools.guvnor.client.resources.Images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import org.drools.guvnor.client.explorer.navigation.NavigationItem;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.util.Util;

public class RuntimeTree extends Tree
        implements
        SelectionHandler<TreeItem>, NavigationItem {

    private static Constants constants = GWT.create(Constants.class);
    private static Images images = GWT.create(Images.class);
    private final ClientFactory clientFactory;

    Object[][] treeStructure = new Object[][]{
            {constants.Agents(), images.categorySmall(), 0}
    };
    
    public RuntimeTree(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        
        populateTree();
        this.setStyleName( "guvnor-Tree" );

        this.addSelectionHandler(this);
    }

    public final void populateTree() {
        for (int i = 0; i < treeStructure.length; i++) {

            Object[] packageData = treeStructure[i];
            TreeItem localChildNode = new TreeItem(Util.getHeader((ImageResource) packageData[1], (String) packageData[0]));
            localChildNode.setUserObject(packageData[2]);
            addItem(localChildNode);
        }
    }

    @Override
    public String getName() {
        return constants.Runtime();
    }

    @Override
    public ImageResource getImage() {
        return images.rules();
    }

    @Override
    public IsWidget createContent() {
        return this;
    }

    @Override
    public void refreshTree() {
    }

    // Show the associated widget in the deck panel
    @Override
    public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem item = event.getSelectedItem();

        int id = (Integer)item.getUserObject();
        clientFactory.getPlaceController().goTo(new RuntimePlace(id));
    }

    @Override
    public MenuBar createMenu() {
        return null;
    }
}
