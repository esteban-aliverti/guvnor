package org.drools.guvnor.server.sso.wso2;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.drools.guvnor.server.sso.AuthenticationFailedException;
import org.drools.guvnor.server.sso.HttpAuthenticationProvider;
import org.drools.guvnor.server.sso.InternalAuthenticator;
import org.opensaml.xml.XMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;

/**
 *
 * @author esteban
 */
public class WSO2AuthenticationProvider implements HttpAuthenticationProvider {
    
    private String securityCallbackURL;
    private SamlConsumer samlConsumer;

    @Override
    public void configure(String securityCallbackURL, Map<String, String> configurationParameters) {
        this.securityCallbackURL = securityCallbackURL;
        
        Saml2Util.addProperty(SSOConstants.IDP_URL, configurationParameters.get(SSOConstants.IDP_URL));

        if (Saml2Util.getProperty(SSOConstants.IDP_URL) == null){
            throw new IllegalStateException("You need to provide an Identity Provider URL ('"+SSOConstants.IDP_URL+"')");
        }
        
        Saml2Util.addProperty(SSOConstants.ISSUER_ID, configurationParameters.get(SSOConstants.ISSUER_ID));
        if (Saml2Util.getProperty(SSOConstants.ISSUER_ID) == null){
            throw new IllegalStateException("You need to provide a Service Provider Id ('"+SSOConstants.ISSUER_ID+"')");
        }
        
        this.samlConsumer = new SamlConsumer(Saml2Util.getProperty(SSOConstants.IDP_URL));
        
        Saml2Util.addProperty(SSOConstants.KEY_STORE_NAME, configurationParameters.get(SSOConstants.KEY_STORE_NAME));
        Saml2Util.addProperty(SSOConstants.KEY_STORE_PASSWORD, configurationParameters.get(SSOConstants.KEY_STORE_PASSWORD));
        Saml2Util.addProperty(SSOConstants.IDP_ALIAS, configurationParameters.get(SSOConstants.IDP_ALIAS));
        
    }

    @Override
    public void doLoginRedirect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        try {
            String requestMessage = samlConsumer.buildRequestMessage(Saml2Util.getProperty(SSOConstants.ISSUER_ID), request.getRequestURL().toString()+"/"+this.securityCallbackURL);
            System.out.println("----------------------------------------");
            System.out.println(requestMessage);
            System.out.println("----------------------------------------");
            response.sendRedirect(requestMessage);

        } finally {
            out.close();
        }
    }

    @Override
    public String processSecurityCallback(HttpServletRequest request, HttpServletResponse response, InternalAuthenticator internalAuthenticator) throws AuthenticationFailedException {
        try {
            String resp = request.getParameter("SAMLResponse");
            String relayState = request.getParameter("RelayState");
            String respEncoded = request.getParameter("SAMLResponseEncoded");
            
            
            if (resp == null){
                resp = request.getHeader("SAMLResponse");
            }
            if (respEncoded == null){
                respEncoded = request.getHeader("SAMLResponseEncoded");
            }
            
            if (respEncoded != null && respEncoded.equalsIgnoreCase("true")){
                resp = Saml2Util.decode(resp);
            }
            
            System.out.println("\n\n");
            System.out.println(resp);
            System.out.println("\n\n");
            
            XMLObject samlObject = Saml2Util.unmarshall(resp);
            
            // if this is a logout Response, then send the user to logout_complete.jsp
            // after removing the session attributes.
            if (samlObject instanceof LogoutResponse) {
                internalAuthenticator.logout(request);
                return request.getContextPath();
            }
            
            // Assuming it a Response
            Response samlResponse = (Response) samlObject;
            List<Assertion> assertions = samlResponse.getAssertions();
            String username = "anonymous user";
            
            // extract the username
            if(assertions != null && assertions.size() > 0){
                Subject subject = assertions.get(0).getSubject();
                if(subject != null){
                    if(subject.getNameID() != null){
                        username = subject.getNameID().getValue();
                    }
                }
            }

            // validating the signature
            if(!Saml2Util.validateSignature(samlResponse)){
                throw new AuthenticationFailedException("Invalid username/password!");
            }
            
            
            internalAuthenticator.authenticate(username, Saml2Util.encode(resp), request, response);
            
            return request.getContextPath();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AuthenticationFailedException("Invalid username/password!");
        }
    }

}
