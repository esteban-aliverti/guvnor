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

package org.drools.guvnor.server.contenthandler.drools;

import com.google.gwt.user.client.rpc.SerializationException;
import org.drools.compiler.DroolsParserException;
import org.drools.guvnor.server.builder.AssemblyErrorLogger;
import org.drools.guvnor.server.builder.BRMSPackageBuilder;
import org.drools.repository.AssetItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import org.drools.compiler.PMMLCompiler;
import org.drools.compiler.PMMLCompilerFactory;
import org.drools.guvnor.client.rpc.Asset;
import org.drools.guvnor.server.contenthandler.ContentHandler;
import org.drools.guvnor.server.contenthandler.IRuleAsset;

/**
 * This is for handling PMML content (Predictive Model Markup Language).
 * PMML is converted to DRL using an implementation of
 * {@link PMMLCompiler}
 */
public class PMMLHandler extends ContentHandler
        implements
        IRuleAsset {

    /**
     * Compiler class used to convert from PMML -> DRL
     * IMPORTANT: Do not use this variable directly, use 
     * {@link #getPMMLCompiler()} instead.
     */
    private static PMMLCompiler pMMLCompiler;
    
    public void retrieveAssetContent(Asset asset,
                                     AssetItem item) throws SerializationException {
        //do nothing, as we have an attachment
    }

    public void storeAssetContent(Asset asset,
                                  AssetItem repoAsset) throws SerializationException {
        //do nothing, as we have an attachment
    }

    public void assembleDRL(BRMSPackageBuilder builder,
                            Asset asset,
                            StringBuilder stringBuilder) {
        // TODO Auto-generated method stub

    }

    public void assembleDRL(BRMSPackageBuilder builder,
                            AssetItem asset,
                            StringBuilder stringBuilder) {
        stringBuilder.append(getRawDRL(asset));
    }

    public void compile(BRMSPackageBuilder builder,
                        AssetItem asset,
                        AssemblyErrorLogger logger) throws DroolsParserException,
            IOException {
        StringBuilder stringBuilder = new StringBuilder();

        assembleDRL(builder,
                asset,
                stringBuilder);
        builder.addPackageFromDrl(new StringReader(stringBuilder.toString()));

    }

    public String getRawDRL(AssetItem asset) {
        return getDRL(asset.getBinaryContentAttachment());
    }

    /**
     * Convert the content of the stream from PMML to DRL using
     * {@link PMMLCompiler}
     * @param stream
     * @return 
     */
    private String getDRL(InputStream stream) {
        PMMLCompiler compiler = getPMMLCompiler();
        if ( compiler != null ) {

            String theory = compiler.compile( stream,
                                              null );
            return theory;
        } else {
            throw new RuntimeException("No PMML Compiler found!");
        }
    }
    
    /**
     * Get a cached instance of {@link PMMLCompiler}. The first time this 
     * method is called it creates an instance of {@link PMMLCompiler}.
     * Subsequent calls will return the same instance.
     */
    private synchronized static PMMLCompiler getPMMLCompiler() {
        //We don't want to instantiate a new Compiler each time because it 
        //is an expensive operation.
        if (pMMLCompiler == null){
            pMMLCompiler = PMMLCompilerFactory.getPMMLCompiler();
        }
        return pMMLCompiler;
    }

}
