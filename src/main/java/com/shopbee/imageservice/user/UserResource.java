package com.shopbee.imageservice.user;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("customers")
@RegisterRestClient(configKey = "customer-api")
public interface UserResource {

    @GET
    @Path("{username}")
    User getByUsername(@PathParam("username") String username, @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader);

}
