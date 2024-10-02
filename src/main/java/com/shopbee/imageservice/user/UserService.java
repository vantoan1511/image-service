package com.shopbee.imageservice.user;

import com.shopbee.imageservice.image.ImageException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@ApplicationScoped
public class UserService {

    private static final String BEARER = "Bearer ";

    @RestClient
    UserResource userResource;

    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    SecurityIdentity securityIdentity;

    public User getByUsername(String username) {
        try {
            return userResource.getByUsername(username, BEARER + accessToken());
        } catch (ClientWebApplicationException e) {
            throw new ImageException("User not found: " + username, Response.Status.NOT_FOUND);
        }
    }

    private String accessToken() {
        return jsonWebToken.getRawToken();
    }
}
