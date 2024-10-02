package com.shopbee.imageservice.image;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class ImageException extends WebApplicationException {

    public ImageException(String message, Response response) {
        super(message, response);
    }

    public ImageException(String message, Response.Status status) {
        super(message, status);
    }
}
