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

package org.drools.guvnor.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.drools.guvnor.client.rpc.RepositoryService;
import org.drools.guvnor.client.rpc.RepositoryServiceAsync;
import org.drools.guvnor.client.rpc.SecurityService;
import org.drools.guvnor.client.rpc.SecurityServiceAsync;
import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * This will verify that the interfaces are kosher for GWT to use.
 */
public class AsyncInterfaceTest {

    @Test
    public void testService() throws Exception {
        try {
            checkService( RepositoryService.class, RepositoryServiceAsync.class );
            checkService( SecurityService.class, SecurityServiceAsync.class );
        } catch (Exception e) {
            fail("Async interface is not in sync with service interface. For RepositoryService you can run AsyncInterfaceGenerator.");
        }

    }

    private void checkService(Class<?> clsInt, Class<?> clsAsync) throws NoSuchMethodException {
        for ( Method m : clsInt.getMethods()) {

            Class<?>[] paramClasses = new Class[m.getParameterTypes().length + 1];
            Class<?>[] sourceParamClasses = m.getParameterTypes();
            for ( int i = 0; i < sourceParamClasses.length; i++ ) {
                paramClasses[i] = sourceParamClasses[i];
            }
            paramClasses[sourceParamClasses.length] = AsyncCallback.class;
            assertNotNull("Async interface not in sync (hahaha) with RepositoryService. Run AsyncInterfaceGenerator.main() to regenerate the content of that class." , clsAsync.getMethod( m.getName(), paramClasses ));
        }
    }

}
