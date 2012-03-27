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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.rpc.Asset;
import org.drools.guvnor.client.rpc.WorkingSetConfigData;
import org.drools.guvnor.server.RepositoryCategoryService;
import org.drools.guvnor.server.contenthandler.ContentHandler;
import org.drools.guvnor.server.contenthandler.ContentManager;
import org.drools.guvnor.server.contenthandler.ICanHasAttachment;
import org.drools.guvnor.server.contenthandler.drools.WorkingSetHandler;
import org.drools.guvnor.server.files.FileManagerService;
import org.drools.io.ResourceFactory;
import org.drools.repository.AssetItem;
import org.drools.repository.ModuleItem;
import org.drools.repository.RulesRepository;
import org.drools.repository.RulesRepositoryException;
import org.drools.semantics.builder.DLFactory;
import org.drools.semantics.builder.DLFactoryBuilder;
import org.drools.semantics.builder.model.Concept;
import org.drools.semantics.builder.model.DRLModel;
import org.drools.semantics.builder.model.JarModel;
import org.drools.semantics.builder.model.ModelFactory;
import org.drools.semantics.builder.model.OntoModel;
import org.drools.semantics.builder.model.PropertyRelation;
import org.drools.semantics.builder.model.WorkingSetModel;
import org.drools.semantics.builder.model.XSDModel;
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
    
    private final FileManagerService fileManagerService;

    public OWLImporter(RulesRepository repository, RepositoryCategoryService categoryService, FileManagerService fileManagerService, InputStream owlDefinitionStream) {
        this.categoryService = categoryService;
        this.repositoryService = repository;
        this.fileManagerService = fileManagerService;
        
        DLFactory dLFactory = DLFactoryBuilder.newDLFactoryInstance();
        ontoModel = dLFactory.buildModel("ontomodel", ResourceFactory.newInputStreamResource(owlDefinitionStream));
    }
    
    public String getPackageName(){
        return ontoModel.getPackage();
    }
    
    public void processOWLDefinition(ModuleItem pkg) throws SerializationException, IOException{
        
        //Get the model JAR from onto-model
        ModelCompiler jarCompiler = ModelCompilerFactory.newModelCompiler(ModelFactory.CompileTarget.JAR);
        JarModel compiledJarModel = (JarModel) jarCompiler.compile(ontoModel);
        byte[] jarBytes = compiledJarModel.buildJar().toByteArray();
        
        //Get the Working-Set from onto-model
        ModelCompiler wsCompiler = ModelCompilerFactory.newModelCompiler(ModelFactory.CompileTarget.WORKSET);
        WorkingSetModel compiledWSModel = (WorkingSetModel) wsCompiler.compile(ontoModel);
        SemanticWorkingSetConfigData semanticWorkingSet = compiledWSModel.getWorkingSet();
        
        //Get the Fact Types DRL from onto-model
        ModelCompiler drlCompiler = ModelCompilerFactory.newModelCompiler(ModelFactory.CompileTarget.DRL);
        DRLModel drlModel = (DRLModel)drlCompiler.compile(ontoModel);
        
        //Get the Fact Types XSD from onto-model
        ModelCompiler xsdCompiler = ModelCompilerFactory.newModelCompiler(ModelFactory.CompileTarget.XSD);
        XSDModel xsdModel = (XSDModel)xsdCompiler.compile(ontoModel);
        
        //convert from semantic to guvnor model
        WorkingSetConfigData workingSetConfigData = this.convertSemanticWorkingSetConfigData(semanticWorkingSet);
        
        //create a second Working-Set for the Configuration (Cohort) Facts
        WorkingSetConfigData cohortWorkingSetConfigData = this.convertSemanticWorkingSetConfigData("Configuration Facts", semanticWorkingSet);
        
        //create categories from working-set data
        this.createCategoryTreeFromWorkingSet(workingSetConfigData);
        
        //create the Jar Model
        this.createJarModelAsset(pkg, jarBytes);
        
        //create the working-set assets
        this.createWSAsset(pkg, workingSetConfigData);
        this.createWSAsset(pkg, cohortWorkingSetConfigData);
        
        //store the fact type drl as a generic resource
        this.storeFactTypeDRL(pkg, drlModel);
        
        //create and store the Fact Type Descriptor
        this.createFactTypeDescriptor(pkg, xsdModel);
    }
    
    private void createJarModelAsset(ModuleItem pkg, byte[] jarBytes) throws IOException{
        AssetItem asset = pkg.addAsset( "OWL Model",
                                                "<imported from OWL>" );
        asset.updateFormat( AssetFormats.MODEL );
        asset.updateBinaryContentAttachment(new ByteArrayInputStream(jarBytes));
        asset.updateExternalSource( "Imported from external OWL" );
        asset.getModule().updateBinaryUpToDate( false );
        
        asset.checkin( "Imported from external OWL" );
        
        // Special treatment for model and ruleflow attachments.
        ContentHandler handler = ContentManager.getHandler( asset.getFormat() );
        if ( handler instanceof ICanHasAttachment ) {
            ((ICanHasAttachment) handler).onAttachmentAdded( asset );
        }
        
    }
    
    private void createWSAsset(ModuleItem pkg, WorkingSetConfigData content) throws SerializationException{
        AssetItem asset = pkg.addAsset( content.getName(),
                                                content.getDescription());
        asset.updateFormat( AssetFormats.WORKING_SET );
        asset.updateExternalSource( "Imported from external OWL" );
        
        Asset ruleAsset = new Asset();
        ruleAsset.setName(content.getName());
        ruleAsset.setContent(content);
        
        new WorkingSetHandler().storeAssetContent(ruleAsset, asset);
        
        asset.checkin( "Imported from external OWL" );
    }
    
    private WorkingSetConfigData convertSemanticWorkingSetConfigData(SemanticWorkingSetConfigData semanticWSData){
        return this.convertSemanticWorkingSetConfigData(semanticWSData.getName(), semanticWSData);
    }
    
    private WorkingSetConfigData convertSemanticWorkingSetConfigData(String name, SemanticWorkingSetConfigData semanticWSData){
        WorkingSetConfigData root = new WorkingSetConfigData();
        root.setName(name);
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

    private void storeFactTypeDRL(ModuleItem pkg, DRLModel drlModel) throws IOException {
        AssetItem asset = pkg.addAsset( "Fact Model",
                                                "<imported from OWL>" );
        asset.updateFormat( AssetFormats.ATTACHED_DRL );
        asset.updateBinaryContentAttachment(new ByteArrayInputStream(drlModel.getDRL().getBytes()));
        asset.updateExternalSource( "Imported from external OWL" );
        asset.getModule().updateBinaryUpToDate( false );
        
        asset.checkin( "Imported from external OWL" );
        
        // Special treatment for model and ruleflow attachments.
        ContentHandler handler = ContentManager.getHandler( asset.getFormat() );
        if ( handler != null && handler instanceof ICanHasAttachment ) {
            ((ICanHasAttachment) handler).onAttachmentAdded( asset );
        }
        
    }
    
    private void createFactTypeDescriptor(ModuleItem pkg, XSDModel xsdModel) throws IOException {
        //get the concepts from the sxdModel
        Map<String, Map<String, String>> concepts = this.getConcepts(xsdModel.getConcepts());
        
        //create the Descriptor file using a template
        StringTemplate template = new StringTemplate(IOUtils.toString(this.getClass().getResourceAsStream("/template/factTypesDescriptor.tpl")));
        
        template.setAttribute("concepts", concepts);
        
        AssetItem asset = pkg.addAsset( "Fact Types Descriptor",
                                                "<imported from OWL>" );
        asset.updateFormat( AssetFormats.ATTACHED_MODEL_DESCRIPTOR );
        asset.updateBinaryContentAttachment(new ByteArrayInputStream(template.toString().getBytes()));
        asset.updateExternalSource( "Imported from external OWL" );
        asset.getModule().updateBinaryUpToDate( false );
        
        asset.checkin( "Imported from external OWL" );
        
        // Special treatment for model and ruleflow attachments.
        ContentHandler handler = ContentManager.getHandler( asset.getFormat() );
        if ( handler != null && handler instanceof ICanHasAttachment ) {
            ((ICanHasAttachment) handler).onAttachmentAdded( asset );
        }
        
    }
    
    /**
     * Returns a Map where the key is the Concept name and the value is 
     * another Map of  "property name" -> "property type"
     * @return 
     */
    private Map<String,Map<String, String>> getConcepts(List<Concept> concepts){
        Map<String,Map<String, String>> result = new HashMap<String, Map<String, String>>();
        
        for (Concept concept : concepts) {
            Map<String, String> conceptProperties = this.getConceptProperties(concept);
            result.put(concept.getName(), conceptProperties);
        }
        
        return result;
    }
    
    private Map<String, String> getConceptProperties(Concept concept){
        Map<String, String> properties = new HashMap<String, String>();
        
        //super-concepts properties first
        Set<Concept> superConcepts = concept.getSuperConcepts();
        if (superConcepts != null){
            for (Concept superConcept : superConcepts) {
                properties.putAll(this.getConceptProperties(superConcept));
            }
        }
        
        //concept properties
        for (Map.Entry<String, PropertyRelation> entry : concept.getProperties().entrySet()) {
           String propertyName = entry.getKey();
           propertyName = propertyName.substring(propertyName.lastIndexOf("#")+1, propertyName.lastIndexOf(">"));
           properties.put(propertyName, entry.getValue().getTypeName());
        }
        
        return properties;
    }
    
}