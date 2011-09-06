/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.client.explorer;

import org.drools.guvnor.client.explorer.navigation.admin.ManagerActivity;
import org.drools.guvnor.client.explorer.navigation.admin.ManagerPlace;
import org.drools.guvnor.client.explorer.navigation.browse.CategoryActivity;
import org.drools.guvnor.client.explorer.navigation.browse.CategoryPlace;
import org.drools.guvnor.client.explorer.navigation.browse.InboxActivity;
import org.drools.guvnor.client.explorer.navigation.browse.InboxPlace;
import org.drools.guvnor.client.explorer.navigation.browse.StateActivity;
import org.drools.guvnor.client.explorer.navigation.browse.StatePlace;
import org.drools.guvnor.client.explorer.navigation.deployment.SnapshotActivity;
import org.drools.guvnor.client.explorer.navigation.deployment.SnapshotAssetListActivity;
import org.drools.guvnor.client.explorer.navigation.deployment.SnapshotAssetListPlace;
import org.drools.guvnor.client.explorer.navigation.deployment.SnapshotPlace;
import org.drools.guvnor.client.explorer.navigation.qa.TestScenarioListActivity;
import org.drools.guvnor.client.explorer.navigation.qa.TestScenarioListPlace;
import org.drools.guvnor.client.explorer.navigation.qa.VerifierActivity;
import org.drools.guvnor.client.explorer.navigation.qa.VerifierPlace;
import org.drools.guvnor.client.util.Activity;
import org.drools.guvnor.client.util.ActivityMapper;
import org.drools.guvnor.client.widgets.assetviewer.AssetViewerActivity;
import org.drools.guvnor.client.widgets.assetviewer.AssetViewerPlace;
import org.drools.guvnor.client.widgets.wizards.WizardActivity;
import org.drools.guvnor.client.widgets.wizards.WizardPlace;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;

public class GuvnorActivityMapper
        implements
        ActivityMapper {
    private ClientFactory clientFactory;
    private final EventBus eventBus;

    public GuvnorActivityMapper(ClientFactory clientFactory, EventBus eventBus) {
        super();
        this.clientFactory = clientFactory;
        this.eventBus = eventBus;
    }

    public Activity getActivity(Place place) {
        if ( place instanceof FindPlace ) {
            return new FindActivity( clientFactory );
        } else if ( place instanceof AssetEditorPlace ) {
            return new AssetEditorActivity( (AssetEditorPlace) place, clientFactory );
        } else if ( place instanceof ModuleEditorPlace ) {
            return new ModuleEditorActivity( ((ModuleEditorPlace) place).getUuid(),
                    clientFactory );
        } else if ( place instanceof AssetViewerPlace ) {
            return new AssetViewerActivity( ((AssetViewerPlace) place).getUuid(),
                    clientFactory );
        } else if ( place instanceof org.drools.guvnor.client.explorer.navigation.ModuleFormatsGridPlace ) {
            return new org.drools.guvnor.client.explorer.ModuleFormatsGridPlace(
                    (org.drools.guvnor.client.explorer.navigation.ModuleFormatsGridPlace) place,
                    clientFactory );
        } else if ( place instanceof ManagerPlace ) {
            return new ManagerActivity(
                    ((ManagerPlace) place).getId(),
                    clientFactory );
        } else if ( place instanceof TestScenarioListPlace ) {
            return new TestScenarioListActivity(
                    ((TestScenarioListPlace) place).getModuleUuid(),
                    clientFactory );
        } else if ( place instanceof VerifierPlace ) {
            return new VerifierActivity(
                    ((VerifierPlace) place).getModuleUuid(),
                    clientFactory );
        } else if ( place instanceof SnapshotPlace ) {
            return new SnapshotActivity(
                    ((SnapshotPlace) place).getModuleName(),
                    ((SnapshotPlace) place).getSnapshotName(),
                    clientFactory,
                    eventBus);
        } else if ( place instanceof SnapshotAssetListPlace ) {
            return new SnapshotAssetListActivity(
                    (SnapshotAssetListPlace) place,
                    clientFactory );
        } else if ( place instanceof CategoryPlace ) {
            return new CategoryActivity(
                    ((CategoryPlace) place).getCategoryPath(),
                    clientFactory );
        } else if ( place instanceof StatePlace ) {
            return new StateActivity(
                    ((StatePlace) place).getStateName(),
                    clientFactory );
        } else if ( place instanceof InboxPlace ) {
            return new InboxActivity(
                    (InboxPlace) place,
                    clientFactory );
        } else if ( place instanceof MultiAssetPlace ) {
            return new MultiAssetActivity(
                    (MultiAssetPlace) place,
                    clientFactory );
        } else if ( place instanceof WizardPlace ) {
            return new WizardActivity(
                    (WizardPlace<?>) place,
                    clientFactory, 
                    eventBus );
        } else {
            return null;
        }
    }
}
