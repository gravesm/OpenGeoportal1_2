package org.OpenGeoPortal.Security;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This implements an alternative login controller for use in a preauthenticated
 * scenario such as Shibboleth. Since {@link LoginController} also implements a
 * controller mapped to the same URL, you will need to use Spring's XML config
 * to choose one.
 * 
 * @author Mike Graves
 */
@Controller
public class ShibLoginController {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    LoginService loginService;

    /**
     * Presents user with an iframe whose access is controlled by Shibboleth. IT
     * IS IMPERATIVE THAT THIS URL BE ACCESSIBLE ONLY TO AUTHENTICATED USERS.
     * Accessing this URL will log the user into the system so care must be
     * taken that it is accessible only after the user has already authenticated.
     * 
     */
    @RequestMapping("/login")
    public String login() {

        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        logger.debug("Logged in user: " + user.getUsername());

        return "shiblogin";

    }
    
    @RequestMapping("/shiblogout")
    @ResponseBody
    public LoginStatus logout() {
        
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        return new LoginStatus(false, null, authorities);

    }
}