/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.server.security;

import javax.servlet.http.HttpServletRequest;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;
import org.jboss.seam.web.ServletContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Name("WSO2SSOAuthenticator")
public class WSO2SSOAuthenticator {

    //TODO: change this to a configuration parameter
    public static String SSO_COOKIE = "ssoTokenId";
    private static final Logger log = LoggerFactory.getLogger(WSO2SSOAuthenticator.class);
    
    @In
    private Identity identity;
    
    public boolean authenticate() {
        
        HttpServletRequest request = ServletContexts.getInstance().getRequest();
        
        //TODO: hack to bypass Web SSO... We need to come up with a better solution :P
        //This is duplicated in WSO2SSOFilter. Please fix both classes!
        if(request.getHeader("authorization")!= null){
            log.info("User logged in via LAME authentication module :(! Please fix me!");
            return true; //:(
        }
        
        boolean validSession = request.getSession(true).getAttribute(SSO_COOKIE) != null;

        if (validSession){
            log.info("User logged in via SSO authentication module.");
        }
        
        return validSession;

    }
}
