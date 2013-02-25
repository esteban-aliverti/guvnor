package org.drools.guvnor.server.sso;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.drools.guvnor.server.sso.wso2.WSO2AuthenticationProvider;

/**
 *
 */
public class SecurityFilter implements Filter {

    private static final boolean debug = false;
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;
    
    private String securityCallbackURL = null;
    
    private boolean enabled = true;
    
    @Inject
    private InternalAuthenticator internalAuthenticator;
    
    private HttpAuthenticationProvider authenticationProvider;
    
    public SecurityFilter() {
    }

    private boolean doBeforeProcessing(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("SecurityFilter:DoBeforeProcessing");
        }
        
        if (!enabled){
            return true;
        }
        
        //skip this for webdav
        if (request.getServletPath().equals("/org.drools.guvnor.GuvnorDrools/webdav") || request.getServletPath().equals("/org.drools.guvnor.Guvnor/webdav")){
            return true;
        }
//        
//        //skip this for feed
//        if (request.getServletPath().equals("/org.drools.guvnor.GuvnorDrools/feed") || request.getServletPath().equals("/org.drools.guvnor.Guvnor/feed")){
//            return true;
//        }
        
        
        
        //is security callback -> process it
        if (request.getServletPath().endsWith(this.securityCallbackURL)){
            try {
                authenticationProvider.processSecurityCallback(request, response, internalAuthenticator);
                response.sendRedirect(request.getContextPath());
                return false;
            } catch (AuthenticationFailedException ex) {

                request.setAttribute("errorMessage", ex.getMessage());
                
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("/error.jsp");
                requestDispatcher.forward(request, response);
                return true;
            }
        }
        
        //is this secured?
        if (!internalAuthenticator.isSecureRequest(request, response)){
            //is this a service call?
            if (request.getHeader("SAMLResponse") != null || request.getParameter("SAMLResponse") != null){
                try {
                    authenticationProvider.processSecurityCallback(request, response, internalAuthenticator);
                    return true;
                } catch (AuthenticationFailedException ex) {
                    //do nothing. The user will be redirected to login page.
                }
            }
            //is Authentication header attribute present? The concrete authenticator will handle this later.
            if (request.getHeader("Authorization") != null){
                return true;
            }
            
            //invalid? -> do login please
            authenticationProvider.doLoginRedirect(request, response);
            return false;
        }
        
        return true;
    }

    private void doAfterProcessing(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("SecurityFilter:DoAfterProcessing");
        }


    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (debug) {
            log("SecurityFilter:doFilter()");
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        boolean continueProcessing = doBeforeProcessing(httpRequest, httpResponse);

        Throwable problem = null;

        if (continueProcessing){
            try {
                chain.doFilter(httpRequest, httpResponse);
            } catch (Throwable t) {
                // If an exception is thrown somewhere down the filter chain,
                // we still want to execute our after processing, and then
                // rethrow the problem after that.
                problem = t;
                t.printStackTrace();
            }

            doAfterProcessing(httpRequest, httpResponse);

            // If there was a problem, we want to rethrow it if it is
            // a known type, otherwise log it.
            if (problem != null) {
                if (problem instanceof ServletException) {
                    throw (ServletException) problem;
                }
                if (problem instanceof IOException) {
                    throw (IOException) problem;
                }
                sendProcessingError(problem, response);
            }
        }
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter 
     */
    public void destroy() {
    }

    /**
     * Init method for this filter 
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("SecurityFilter: Initializing filter");
            }

            this.securityCallbackURL = filterConfig.getInitParameter("securityCallbackURL");
            
            this.enabled = filterConfig.getInitParameter("enabled")==null?true:Boolean.getBoolean(filterConfig.getInitParameter("enabled"));
        }

        if (this.securityCallbackURL == null) {
            throw new IllegalStateException("securityCallbackURL was not specified!");
        }

        authenticationProvider = new WSO2AuthenticationProvider();
        
        authenticationProvider.configure(this.securityCallbackURL, this.getConfigurationParameters(filterConfig));
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("SecurityFilter()");
        }
        StringBuffer sb = new StringBuffer("SecurityFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());

    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }
    
    private Map<String,String> getConfigurationParameters(FilterConfig filterConfig){
        Map config = new HashMap();
        if (filterConfig != null){
            Enumeration initParameterNames = filterConfig.getInitParameterNames();
            while (initParameterNames.hasMoreElements()) {
                String name = (String) initParameterNames.nextElement();
                String value = filterConfig.getInitParameter(name);
                
                config.put(name, value);
            }
        }
        return config;
    }

}
