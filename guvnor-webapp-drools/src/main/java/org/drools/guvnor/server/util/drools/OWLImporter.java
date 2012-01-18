/*
 * Copyright 2011 JBoss by Red Hat.
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

import com.google.gwt.user.client.rpc.SerializationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.rpc.RuleAsset;
import org.drools.guvnor.client.rpc.WorkingSetConfigData;
import org.drools.guvnor.server.RepositoryCategoryService;
import org.drools.guvnor.server.contenthandler.ContentHandler;
import org.drools.guvnor.server.contenthandler.ContentManager;
import org.drools.guvnor.server.contenthandler.ICanHasAttachment;
import org.drools.guvnor.server.contenthandler.drools.WorkingSetHandler;
import org.drools.io.ResourceFactory;
import org.drools.repository.AssetItem;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;
import org.drools.repository.RulesRepositoryException;
import org.drools.semantics.builder.DLFactory;
import org.drools.semantics.builder.DLFactoryBuilder;
import org.drools.semantics.builder.model.JarModel;
import org.drools.semantics.builder.model.ModelFactory;
import org.drools.semantics.builder.model.OntoModel;
import org.drools.semantics.builder.model.WorkingSetModel;
import org.drools.semantics.builder.model.compilers.ModelCompiler;
import org.drools.semantics.builder.model.compilers.ModelCompilerFactory;
import org.drools.semantics.util.SemanticWorkingSetConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OWLImporter {
   
    private static final Logger log = LoggerFactory.getLogger(OWLImporter.class);
    
    private final RepositoryCategoryService categoryService;
    private final OntoModel ontoModel;
    private final RulesRepository repositoryService;

    public OWLImporter(RulesRepository repository, RepositoryCategoryService categoryService, InputStream owlDefinitionStream) {
        this.categoryService = categoryService;
        this.repositoryService = repository;
        
        DLFactory dLFactory = DLFactoryBuilder.newDLFactoryInstance();
        ontoModel = dLFactory.buildModel("ontomodel", ResourceFactory.newInputStreamResource(owlDefinitionStream));
    }
    
    public String getPackageName(){
        return ontoModel.getPackage();
    }
    
    public void processOWLDefinition(PackageItem pkg) throws SerializationException, IOException{
        
        //Get the model JAR from onto-model
        ModelCompiler jarCompiler = ModelCompilerFactory.newModelCompiler(ModelFactory.CompileTarget.JAR);
        JarModel compiledJarModel = (JarModel) jarCompiler.compile(ontoModel);
        byte[] jarBytes = compiledJarModel.buildJar().toByteArray();
        
        //Get the Working-Set from onto-model
        ModelCompiler wsCompiler = ModelCompilerFactory.newModelCompiler(ModelFactory.CompileTarget.WORKSET);
        WorkingSetModel compiledWSModel = (WorkingSetModel) wsCompiler.compile(ontoModel);
        SemanticWorkingSetConfigData semanticWorkingSet = compiledWSModel.getWorkingSet();
        
        //convert from semantic to guvnor model
        WorkingSetConfigData workingSetConfigData = this.convertSemanticWorkingSetConfigData(semanticWorkingSet);
        
        
        //create categories from working-set data
        this.createCategoryTreeFromWorkingSet(workingSetConfigData);
        
        //create the Jar Model
        this.createJarModelAsset(pkg, jarBytes);
        
        //create the working-set asset
        this.createWSAsset(pkg, workingSetConfigData);
    }
    
    private void createJarModelAsset(PackageItem pkg, byte[] jarBytes) throws IOException{
        AssetItem asset = pkg.addAsset( "OWL Model",
                                                "<imported from OWL>" );
        asset.updateFormat( AssetFormats.MODEL );
        asset.updateBinaryContentAttachment(new ByteArrayInputStream(jarBytes));
        asset.updateExternalSource( "Imported from external OWL" );
        asset.getPackage().updateBinaryUpToDate( false );
        
        asset.checkin( "Imported from external OWL" );
        
        // Special treatment for model and ruleflow attachments.
        ContentHandler handler = ContentManager.getHandler( asset.getFormat() );
        if ( handler instanceof ICanHasAttachment ) {
            ((ICanHasAttachment) handler).onAttachmentAdded( asset );
        }
        
    }
    
    private void createWSAsset(PackageItem pkg, WorkingSetConfigData content) throws SerializationException{
        AssetItem asset = pkg.addAsset( content.getName(),
                                                content.getDescription());
        asset.updateFormat( AssetFormats.WORKING_SET );
        asset.updateExternalSource( "Imported from external OWL" );
        
        RuleAsset ruleAsset = new RuleAsset();
        ruleAsset.setName(content.getName());
        ruleAsset.setContent(content);
        
        new WorkingSetHandler().storeAssetContent(ruleAsset, asset);
        
        asset.checkin( "Imported from external OWL" );
    }
    
    private WorkingSetConfigData convertSemanticWorkingSetConfigData(SemanticWorkingSetConfigData semanticWSData){
        WorkingSetConfigData root = new WorkingSetConfigData();
        root.setName(semanticWSData.getName());
        root.setDescription(semanticWSData.getDescription());
        root.setValidFacts(semanticWSData.getValidFacts());
        
        if (semanticWSData.getWorkingSets() != null){
            List<WorkingSetConfigData> children = new ArrayList<WorkingSetConfigData>();
            for (SemanticWorkingSetConfigData child : semanticWSData.getWorkingSets()) {
                if (child != null){
                    children.add(this.convertSemanticWorkingSetConfigData(child));
                }
            }
            root.setWorkingSets(children.toArray(new WorkingSetConfigData[0]));
        }
        
        return root;
    }
    
    private void createCategoryTreeFromWorkingSet(WorkingSetConfigData workingSet){
        this.createCategoryTreeFromWorkingSet("", workingSet);
    }
    
    private void createCategoryTreeFromWorkingSet(String path, WorkingSetConfigData workingSet){
        String name = workingSet.getName();
        String description = workingSet.getDescription();
        
        //ugly way to check whether a category exists or not
        boolean alreadyExists = false;
        try{
            repositoryService.loadCategory(path+"/"+name);
            alreadyExists = true;        
        } catch (RulesRepositoryException ex){
            if (!ex.getMessage().startsWith("Unable to load the category ")){
                throw ex;
            }
        }
        
        if (!alreadyExists){
            categoryService.createCategory(path, name, description);
        }
        
        
        if (workingSet.getWorkingSets() != null){
            for (WorkingSetConfigData child : workingSet.getWorkingSets()) {
                this.createCategoryTreeFromWorkingSet(path+"/"+name, child);
            }
        }
        
    }
    
}