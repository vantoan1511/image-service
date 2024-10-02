package com.shopbee.imageservice;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("api/health-check")
public class HealthResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String check() {
        return "Service is running...";
    }
}
