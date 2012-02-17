package org.drools.guvnor.server.sso.wso2;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;

/**
 * This class is used to generate the Logout Requests.
 */
public class LogoutRequestBuilder {

    /**
     * Build the logout request
     * @param subject name of the user
     * @param reason reason for generating logout request.
     * @return LogoutRequest object
     */
    public LogoutRequest buildLogoutRequest(String subject, String reason) {
        Saml2Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Saml2Util.createID());

        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(Saml2Util.getProperty(SSOConstants.ISSUER_ID));
        logoutReq.setIssuer(issuer);

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat(SSOConstants.SAML2_NAME_ID_POLICY);
        nameId.setValue(subject);
        logoutReq.setNameID(nameId);

        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(Saml2Util.createID());
        logoutReq.getSessionIndexes().add(sessionIndex);

        logoutReq.setReason(reason);

        return logoutReq;
    }
}
