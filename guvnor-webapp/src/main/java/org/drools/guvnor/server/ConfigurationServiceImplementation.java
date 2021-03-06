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

package org.drools.guvnor.server;

import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.drools.guvnor.client.rpc.ConfigurationService;
import org.drools.guvnor.server.configurations.ApplicationPreferencesInitializer;
import org.drools.guvnor.server.configurations.ApplicationPreferencesLoader;
import org.drools.guvnor.server.util.TestEnvironmentSessionHelper;
import org.drools.repository.RulesRepository;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;

public class ConfigurationServiceImplementation
        extends RemoteServiceServlet
        implements ConfigurationService {

    public Map<String, String> loadPreferences() {
        Map<String, String> preferences = ApplicationPreferencesLoader.load();
        ApplicationPreferencesInitializer.setSystemProperties(preferences);
        return preferences;
    }

    protected RulesRepository getRepository() {
        if (Contexts.isApplicationContextActive()) {
            return (RulesRepository) Component.getInstance("repository");
        } else {
            try {
                return new RulesRepository(TestEnvironmentSessionHelper.getSession(false));
            } catch (Exception e) {
                throw new IllegalStateException("Unable to get repo to run tests", e);
            }

        }
    }
}
