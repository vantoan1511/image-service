package com.shopbee.imageservice;

import com.shopbee.imageservice.image.ImageException;
import com.shopbee.imageservice.user.User;
import com.shopbee.imageservice.user.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthenticationService {

    UserService userService;

    SecurityIdentity securityIdentity;

    public AuthenticationService(UserService userService,
                                 SecurityIdentity securityIdentity) {
        this.userService = userService;
        this.securityIdentity = securityIdentity;
    }

    public User getPrincipal() {
        try {
            String username = securityIdentity.getPrincipal().getName();
            return userService.getByUsername(username);
        } catch (ImageException e) {
            throw new ImageException("Unauthorized", Response.Status.UNAUTHORIZED);
        }
    }

}
