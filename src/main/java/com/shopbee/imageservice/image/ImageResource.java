package com.shopbee.imageservice.image;

import com.shopbee.imageservice.AuthenticationService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.net.URI;
import java.util.List;

@Path("api/images")
public class ImageResource {

    SecurityIdentity securityIdentity;

    ImageService imageService;

    AuthenticationService authenticationService;

    public ImageResource(SecurityIdentity securityIdentity,
                         ImageService imageService,
                         AuthenticationService authenticationService) {
        this.securityIdentity = securityIdentity;
        this.imageService = imageService;
        this.authenticationService = authenticationService;
    }

    @GET
    @Path("{id}")
    @Authenticated
    public Image getById(@PathParam("id") Long id) {
        return imageService.getById(id);
    }

    @GET
    @Path("avatar")
    @Produces({"image/jpeg"})
    public byte[] getAvatarByUserId(@QueryParam("userId") Long userId) {
        Image avatar = imageService.getAvatarByUserId(userId);
        return avatar.getContent();
    }

//    @GET
//    @Path("avatar/{username}")
//    @Produces({"image/jpeg"})
//    public byte[] getAvatar(@PathParam("username") String username) {
//        return imageService.getAvatar(username);
//    }

    @GET
    @Authenticated
    public List<Image> getUploaded() {
        return imageService.getUploadedWithFilter();
    }

    @POST
    @Authenticated
    public Response upload(
            @RestForm("altText") String altText,
            @RestForm("image") FileUpload imageFile,
            @Context UriInfo uriInfo) {
        Image image = imageService.upload(altText, imageFile);
        URI uri = uriInfo.getAbsolutePathBuilder().build(image.getId());
        return Response.created(uri).entity(image).build();
    }

    @PUT
    @Path("{id}")
    @Authenticated
    public Response setAvatar(@PathParam("id") Long id) {
        imageService.setAvatar(id);
        return Response.ok().build();
    }

    @DELETE
    @Authenticated
    public Response delete(List<Long> ids) {
        imageService.delete(ids);
        return Response.noContent().build();
    }
}
