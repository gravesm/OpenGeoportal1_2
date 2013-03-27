package org.OpenGeoPortal.Security;

import java.util.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Provides user details for a preauthenticated user.
 * 
 * @author Mike Graves
 */
public class ShibUserDetails implements
        AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    /**
     * Creates the UserDetails object. Preauthenticated users are given the role
     * ROLE_USER.
     * 
     * @param token preauthenticated token
     * @return      the created UserDetails object
     * @throws AuthenticationException
     */
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
            throws AuthenticationException {

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        UserDetails userdetails = new User(token.getName(), "N/A", authorities);
        
        return userdetails;
    
    }
}
