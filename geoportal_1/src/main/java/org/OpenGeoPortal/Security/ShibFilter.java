package org.OpenGeoPortal.Security;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * Filter for processing requests in a preauthenticated scenario such as
 * Shibboleth.
 * 
 * @author Mike Graves
 */
public class ShibFilter extends AbstractPreAuthenticatedProcessingFilter {
    
    /**
     * Extracts the user principal from the HTTP request.
     * 
     * @param request   HTTP request
     * @return          principal name
     */
    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        
        Principal principal = request.getUserPrincipal();
        
        if (principal == null) {
            return null;
        }
        
        String username = principal.getName();
                
        return username;
    }

    /**
     * Credentials are not used in a preauthenticated scenario, so this just
     * returns "N/A".
     * 
     * @param request
     * @return
     */
    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }
}