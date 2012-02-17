package org.drools.guvnor.server.sso;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author esteban
 */
public interface HttpAuthenticationProvider {

    void configure(String securityCallbackURL, Map<String, String> configurationParameters);

    void doLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException;
    

    public String processSecurityCallback(HttpServletRequest request, HttpServletResponse response, InternalAuthenticator internalAuthenticator)  throws AuthenticationFailedException;
    
}
