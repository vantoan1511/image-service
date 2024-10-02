package com.shopbee.imageservice;

import com.shopbee.imageservice.image.ImageException;
import com.shopbee.imageservice.user.User;
import com.shopbee.imageservice.user.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthenticationService {

    @Inject
    UserService userService;

    @Inject
    SecurityIdentity securityIdentity;

    public User getPrincipal() {
        try {
            String username = securityIdentity.getPrincipal().getName();
            return userService.getByUsername(username);
        } catch (ImageException e) {
            throw new ImageException("Unauthorized", Response.Status.UNAUTHORIZED);
        }
    }

}
