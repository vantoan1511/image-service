package com.shopbee.imageservice.image;

import com.shopbee.imageservice.AuthenticationService;
import com.shopbee.imageservice.page.PageRequest;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.Objects;

@Path("images")
public class ImageResource {

    SecurityIdentity securityIdentity;
    ImageService imageService;
    AuthenticationService authenticationService;
    ImageResizingService imageResizingService;

    public ImageResource(SecurityIdentity securityIdentity,
                         ImageService imageService,
                         AuthenticationService authenticationService,
                         ImageResizingService imageResizingService) {
        this.securityIdentity = securityIdentity;
        this.imageService = imageService;
        this.authenticationService = authenticationService;
        this.imageResizingService = imageResizingService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response getUploaded(PageRequest pageRequest) {
        return Response.ok(imageService.getUploaded(pageRequest)).build();
    }

    @GET
    @Path("avatar/users/{userId}")
    @Produces({"images/jpeg"})
    public Response getAvatar(@PathParam("userId") Long userId,
                              @QueryParam("size") String size) {
        Image image = imageService.getAvatarByUserId(userId);
        return Response.ok(image.getContent())
                .header("Content-Disposition", "attachment; filename=\"" + image.getAltText() + "\"")
                .build();
    }

    @POST
    @Path("upload")
    @Authenticated
    public Response upload(@RestForm("altText") String altText,
                           @RestForm("image") FileUpload fileUpload) {
        return Response.ok(imageService.upload(altText, fileUpload)).build();
    }

    @GET
    @Path("{id}")
    @Produces({"images/jpeg"})
    public Response getById(@PathParam("id") Long id,
                            @HeaderParam("If-None-Match") String ifNoneMatch,
                            @QueryParam("size") String size) {
        Image image = imageService.getById(id);

        if (Objects.nonNull(size)) {
            ImageSize imageSize = ImageSize.valueOf(size);
            byte[] imageContent = image.getContent();
            image.setContent(imageResizingService.resize(imageContent, imageSize.getWidth(), imageSize.getHeight()));
        }

        String etag = generateETag(image);
        if (etag.equals(ifNoneMatch)) {
            return Response.notModified().build();
        }

        return Response.ok(image.getContent())
                .tag(etag)
                .header("Cache-Control", "public, max-age=86400")
                .header("Content-Disposition", "attachment; filename=\"" + image.getAltText() + "\"")
                .build();
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    public Response delete(@PathParam("id") Long id) {
        imageService.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("{id}/set-avatar")
    @Authenticated
    public Response setAvatar(@PathParam("id") Long id) {
        imageService.setAvatar(id);
        return Response.ok().build();
    }

    private String generateETag(Image image) {
        return image.getId() + "-" + image.getModifiedAt().getTime();
    }
}
