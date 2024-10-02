package com.shopbee.imageservice.image;

import com.shopbee.imageservice.AuthenticationService;
import com.shopbee.imageservice.user.User;
import com.shopbee.imageservice.user.UserResource;
import com.shopbee.imageservice.user.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public void delete(List<Long> ids) {
        ids.forEach(this::delete);
    }

    public void delete(Long id) {
        User user = authenticationService.getPrincipal();
        Image image = getMetadataByIdAndUserId(id, user.getId());
        imageRepository.delete(image);
    }

    public void setAvatar(Long id) {
        removeCurrentAvatars();
        Image image = getMetadataById(id);
        image.setAvatar(true);
    }

    public Image upload(String altText, FileUpload imageFile) {
        try {
            User user = authenticationService.getPrincipal();
            if (Optional.ofNullable(user).map(User::getId).isEmpty()) {
                throw new ImageException("Unauthorized", Response.Status.UNAUTHORIZED);
            }

            validateUploadedImage(imageFile);

            if (StringUtils.isBlank(altText)) {
                altText = imageFile.name();
            }

            Image image = new Image();
            image.setAltText(altText);
            image.setContent(Files.readAllBytes(imageFile.uploadedFile()));
            image.setUserId(user.getId());
            imageRepository.persist(image);
            return image;
        } catch (IOException e) {
            throw new ImageException("Failed to upload image", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Image> getUploadedWithFilter() {
        User user = authenticationService.getPrincipal();
        return imageRepository.find("userId", user.getId()).list();
    }

//    public byte[] getAvatar(String username) {
//        User user = userService.getByUsername(username);
//        return getAvatarByUserId(user.getId()).getContent();
//    }

    public Image getAvatarByUserId(Long userId) {
        return imageRepository.find("userId = ?1 AND avatar = ?2", userId, true)
                .firstResultOptional()
                .orElseThrow(() -> new ImageException("Avatar not found", Response.Status.NOT_FOUND));
    }

    public Image getById(Long id) {
        User user = authenticationService.getPrincipal();

        return getMetadataByIdAndUserId(id, user.getId());
    }

    public Image getMetadataById(Long id) {
        return imageRepository.findByIdOptional(id)
                .orElseThrow(() -> new ImageException("Image not found", Response.Status.NOT_FOUND));
    }

    public Image getMetadataByIdAndUserId(Long id, Long userId) {
        return imageRepository.find("id = ?1 AND userId = ?2", id, userId)
                .firstResultOptional()
                .orElseThrow(() -> new ImageException("Image not found", Response.Status.NOT_FOUND));
    }

    private void removeCurrentAvatars() {
        List<Image> avatars = imageRepository.find("avatar", true).list();
        avatars.forEach(avatar -> avatar.setAvatar(false));
    }

    private void validateUploadedImage(FileUpload imageFile) {
        String mimeType = imageFile.contentType();
        if (mimeType == null || !mimeType.startsWith("image/")) {
            throw new ImageException("Uploaded file is not image", Response.Status.BAD_REQUEST);
        }

        if (imageFile.size() > MAX_FILE_SIZE) {
            throw new ImageException("File exceeds limit of 1MB", Response.Status.BAD_REQUEST);
        }
    }
}
