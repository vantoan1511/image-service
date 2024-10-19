package com.shopbee.imageservice.image;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@ApplicationScoped
public class ImageResizingService {

    public byte[] resize(byte[] originalImage, int targetWidth, int targetHeight) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(originalImage));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(bufferedImage)
                    .size(targetWidth, targetHeight)
                    .outputFormat("JPEG")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageException("Failed to resize image", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] compress(byte[] originalImage) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(originalImage));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(bufferedImage)
                    .scale(1)
                    .outputFormat("JPEG")
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageException("Failed to compress image", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
