package org.drools.guvnor.server.sso;

import java.io.IOException;
import java.util.UUID;
import javax.mail.AuthenticationFailedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.drools.guvnor.client.configurations.SecurityPreferences;

/**
 *
 * @author esteban
 */
public class InternalAuthenticator {
    
    public boolean isSecureRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return request.getSession(true).getAttribute("securityToken") != null;
    }
    
    public void authenticate(String user, String authenticationToken, HttpServletRequest request, HttpServletResponse response) throws AuthenticationFailedException{
        SecurityPreferences.getInstance().setSecurityToken(authenticationToken);
        request.getSession(true).setAttribute("securityToken", UUID.randomUUID().toString());
    }
    
    public void logout(HttpServletRequest request){
        
    }
}
