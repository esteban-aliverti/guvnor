package org.drools.guvnor.server.sso;

import java.io.IOException;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.AuthenticationFailedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.drools.guvnor.server.security.SSOSecurityStore;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

/**
 *
 * @author esteban
 */
@ApplicationScoped
public class InternalAuthenticator {

    @Inject
    private SSOSecurityStore sSOSecurityStore;
    
    @Inject
    private Credentials credentials;
    
    @Inject
    private Identity identity;
    
    public InternalAuthenticator() {
        
    }
    
    public boolean isSecureRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return identity.isLoggedIn();
    }
    
    public void authenticate(String user, String authenticationToken, HttpServletRequest request, HttpServletResponse response) throws AuthenticationFailedException{
        credentials.setUsername(user);
        credentials.setCredential(new org.picketlink.idm.impl.api.PasswordCredential(user));
        identity.login();
        
        this.sSOSecurityStore.setSecurityToken(authenticationToken);
    }
    
    public void logout(HttpServletRequest request){
        
    }
}
