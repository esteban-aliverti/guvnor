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

package org.drools.guvnor.server.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.drools.repository.JCRRepositoryConfigurator;
import org.drools.repository.RepositorySessionUtil;
import org.drools.repository.RulesRepositoryAdministrator;
import org.drools.repository.RulesRepositoryConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is only to be used for testing, eg in hosted mode, or unit tests.
 * This is deliberately in the src/main path.
 */
public class TestEnvironmentSessionHelper {

    private static final Logger log = LoggerFactory.getLogger(TestEnvironmentSessionHelper.class);

    private static Repository repository;

    public static synchronized Session getSession() {
        return getSession(true);
    }

    public static synchronized Session getSession(boolean erase) {
        if (repository == null) {
            try {

                if (erase) {
                    File repoDir = new File("repository");
                    log.info("DELETE test repo dir: " + repoDir.getAbsolutePath());
                    RepositorySessionUtil.deleteDir(repoDir);
                    log.info("TEST repo dir deleted.");
                }

                RulesRepositoryConfigurator config = RulesRepositoryConfigurator.getInstance(null);
                String home = System.getProperty("guvnor.repository.dir");
                Properties properties = new Properties();
                if (home != null) {
                    properties.setProperty(JCRRepositoryConfigurator.REPOSITORY_ROOT_DIRECTORY,
                            home);
                }
                repository = config.getJCRRepository();

                //-->Workaround for lock problems
                int retries = 3;
                Session testSession = null;
                while (retries > 0) {
                    try {
                        testSession = repository.login(new SimpleCredentials("alan_parsons",
                                "password".toCharArray()));

                        retries = 0;
                    } catch (RepositoryException re) {
                        System.err.println("Failed to get the repository session: Retrying... ");
                        re.printStackTrace();

                        System.err.println("Attemptng to shutdown repository... ");
                        shutdown();
                        repository = config.getJCRRepository();

                        retries--;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                        }
                    }
                }
                if (testSession == null) {
                    throw new IllegalStateException("Unable to get Repository Session. Refer to previous messages for details.");
                }
                //<--

                RulesRepositoryAdministrator admin = new RulesRepositoryAdministrator(testSession);
                if (erase && admin.isRepositoryInitialized()) {
                    admin.clearRulesRepository();
                }
                config.setupRepository(testSession);
                File file = File.createTempFile("pete",
                        "txt");
                file.deleteOnExit();
                PrintWriter out = new PrintWriter(new FileOutputStream(file));
                out.close();
                return testSession;
            } catch (Exception e) {
                System.err.println("Failed to get the repository session: ");
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        } else {
            try {
                return repository.login(new SimpleCredentials("alan_parsons",
                        "password".toCharArray()));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Uses the given user name.
     */
    public static synchronized Session getSessionFor(String userName) throws RepositoryException {
        return repository.login(new SimpleCredentials(userName,
                "password".toCharArray()));
    }

    public static synchronized void shutdown() {
        try {
            RulesRepositoryConfigurator.getInstance(null).shutdown();
        } catch (Exception e) {
            log.error("Could not shut down repository.", e);
        }
        repository = null;
    }

}
