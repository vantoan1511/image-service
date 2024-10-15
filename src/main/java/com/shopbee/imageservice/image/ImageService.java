package com.shopbee.imageservice.image;

import com.shopbee.imageservice.AuthenticationService;
import com.shopbee.imageservice.page.PageRequest;
import com.shopbee.imageservice.page.PagedResponse;
import com.shopbee.imageservice.user.User;
import com.shopbee.imageservice.user.UserResource;
import com.shopbee.imageservice.user.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
@Transactional
public class ImageService {

    private static final long MAX_FILE_SIZE = 1024 * 1024;

    ImageRepository imageRepository;

    UserService userService;

    AuthenticationService authenticationService;

    @RestClient
    UserResource userResource;

    public ImageService(ImageRepository imageRepository,
                        UserService userService,
                        AuthenticationService authenticationService) {
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    public PagedResponse<Image> getUploaded(@Valid PageRequest pageRequest) {
        Long userId = authenticationService.getPrincipal().getId();
        List<Image> images = imageRepository.find(userId, pageRequest);
        long totalImages = imageRepository.count(userId);
        return PagedResponse.of((int) totalImages, pageRequest, images);
    }

    public Image getAvatarByUserId(Long userId) {
        return imageRepository.findAvatarByUserId(userId)
                .orElseThrow(() -> new ImageException("Image not found", Response.Status.NOT_FOUND));
    }

    public Image upload(String altText, FileUpload imageFile) {
        try {
            User user = authenticationService.getPrincipal();
            if (Optional.ofNullable(user).map(User::getId).isEmpty()) {
                throw new ImageException("Unauthorized", Response.Status.UNAUTHORIZED);
            }

            validateUploadedImage(imageFile);

            Image image = new Image();

            if (altText == null) {
                image.setAltText(imageFile.name());
            } else {
                image.setAltText(altText);
            }

            image.setContent(Files.readAllBytes(imageFile.uploadedFile()));
            image.setUserId(user.getId());
            imageRepository.persist(image);
            return image;
        } catch (IOException e) {
            throw new ImageException("Failed to upload image", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public void delete(Long id) {
        User user = authenticationService.getPrincipal();
        Image image = getById(id);

        if (!user.getId().equals(image.getUserId())) {
            throw new ImageException("Not permitted", Response.Status.FORBIDDEN);
        }

        imageRepository.delete(image);
    }

    public void setAvatar(Long id) {
        removeCurrentAvatars();
        Image image = getById(id);
        image.setAvatar(true);
    }

    public Image getById(Long id) {
        return imageRepository.findByIdOptional(id)
                .orElseThrow(() -> new ImageException("Image not found", Response.Status.NOT_FOUND));
    }

    private void removeCurrentAvatars() {
        List<Image> avatars = imageRepository.find("avatar", true).list();
        avatars.forEach(avatar -> avatar.setAvatar(false));
    }

    private void validateUploadedImage(FileUpload imageFile) {
        if (imageFile == null) {
            throw new ImageException("File empty", Response.Status.BAD_REQUEST);
        }

        String mimeType = imageFile.contentType();
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new ImageException("File type not supported", Response.Status.BAD_REQUEST);
        }

        if (imageFile.size() > MAX_FILE_SIZE) {
            throw new ImageException("File exceeds limit of 1MB", Response.Status.BAD_REQUEST);
        }
    }
}
