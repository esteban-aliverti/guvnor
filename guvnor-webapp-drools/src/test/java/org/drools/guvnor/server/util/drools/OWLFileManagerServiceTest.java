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

package org.drools.guvnor.server.util.drools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.rpc.Asset;
import org.drools.guvnor.client.rpc.WorkingSetConfigData;
import org.drools.guvnor.server.GuvnorTestBase;
import org.drools.guvnor.server.contenthandler.ContentHandler;
import org.drools.guvnor.server.contenthandler.ContentManager;
import org.drools.guvnor.server.files.FileManagerService;
import org.drools.guvnor.server.files.drools.OWLFileManagerService;
import org.drools.guvnor.server.util.DroolsHeader;
import org.drools.repository.AssetItem;
import org.drools.repository.ModuleItem;
import org.junit.Test;

public class OWLFileManagerServiceTest extends GuvnorTestBase {

    @Inject
    private FileManagerService fileManagerService;
    
    @Inject
    private OWLFileManagerService owlFileManagerService;


    @Test
    public void testImportOWL() throws Exception{
        
        String packageName = "org.drools.guvnor.test.fact";
        
        InputStream in = OWLFileManagerServiceTest.class.getResourceAsStream("/org/drools/guvnor/server/util/sample_owl.ttl");
        
        owlFileManagerService.importOWL(in);

        ModuleItem pkg = rulesRepository.loadModule(packageName);
        assertNotNull( pkg );
        
        //check some of the generated categories
        String[] firstLevelCategories = repositoryCategoryService.loadChildCategories("/Thing");
        
        assertEquals(4, firstLevelCategories.length);
        
        List<String> categoriesList = java.util.Arrays.asList(firstLevelCategories);

        assertTrue(categoriesList.contains("CalendarCycleTwoLetter"));
        assertTrue(categoriesList.contains("ClinicalFact"));
        assertTrue(categoriesList.contains("OrganizationFact"));
        assertTrue(categoriesList.contains("SystemFact"));
        
        String[] secondLevelCategories = repositoryCategoryService.loadChildCategories("/Thing/SystemFact");
        assertEquals(5, secondLevelCategories.length);
        
        categoriesList = java.util.Arrays.asList(secondLevelCategories);

        assertTrue(categoriesList.contains("CodeSystem"));
        assertTrue(categoriesList.contains("CodeSystemEntry"));
        assertTrue(categoriesList.contains("ConceptPointer"));
        assertTrue(categoriesList.contains("Task"));
        assertTrue(categoriesList.contains("ValueUnitPair"));
        
        Iterator<AssetItem> assets = pkg.getAssets();
        List<AssetItem> assetsList = iteratorToList(assets);

        //3 assets: The package, Model and WorkingSet
        assertEquals(3, assetsList.size());
        
        AssetItem modelAsset = null;
        AssetItem workingSetAsset = null;
        for (AssetItem assetItem : assetsList) {
            if (assetItem.getFormat().equals(AssetFormats.MODEL)){
                modelAsset = assetItem;
            }else if (assetItem.getFormat().equals(AssetFormats.WORKING_SET)){
                workingSetAsset = assetItem;
            }
        }
        
        assertNotNull(modelAsset);
        assertNotNull(workingSetAsset);
        
        //assert model asset
        assertEquals("OWL Model", modelAsset.getName());
        
        String droolsHeader = DroolsHeader.getDroolsHeader(pkg);
        assertNotNull(droolsHeader);
        
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.CodeSystem" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.CodeSystemEntry" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.ConceptPointer" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.Task" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.ValueUnitPair" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.CalendarCycleTwoLetter" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.ClinicalFact" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.OrganizationFact" ) > -1 );
        assertTrue( droolsHeader.indexOf( "import org.drools.guvnor.test.fact.SystemFact" ) > -1 );
        
        //assert working-set
        Asset workingSetRuleAsset = new Asset();
        workingSetRuleAsset.setName(workingSetAsset.getName());
        ContentHandler handler = ContentManager.getHandler( workingSetAsset.getFormat() );
        handler.retrieveAssetContent(workingSetRuleAsset, workingSetAsset);
        
        WorkingSetConfigData workingSetContent = (WorkingSetConfigData) workingSetRuleAsset.getContent();
        assertNotNull(workingSetContent);
        
        assertEquals("Thing", workingSetContent.name);

        assertEquals(4, workingSetContent.workingSets.length);

        List<String> secondLevelWorkingSetList = new ArrayList<String>();
        for (WorkingSetConfigData workingSetConfigData : workingSetContent.workingSets) {
            secondLevelWorkingSetList.add(workingSetConfigData.getName());
        }
        

        assertTrue(secondLevelWorkingSetList.contains("CalendarCycleTwoLetter"));
        assertTrue(secondLevelWorkingSetList.contains("ClinicalFact"));
        assertTrue(secondLevelWorkingSetList.contains("OrganizationFact"));
        assertTrue(secondLevelWorkingSetList.contains("SystemFact"));
        
    }
    

    private List<AssetItem> iteratorToList(Iterator<AssetItem> assets) {
        List<AssetItem> list = new ArrayList<AssetItem>();
        for ( Iterator<AssetItem> iter = assets; iter.hasNext(); ) {
            AssetItem rule = (AssetItem) iter.next();
            list.add( rule );
        }
        return list;

    }
}
