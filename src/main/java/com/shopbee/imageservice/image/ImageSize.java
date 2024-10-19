package com.shopbee.imageservice.image;

import lombok.Getter;

@Getter
public enum ImageSize {

    SMALL(48, 48),
    MEDIUM(128, 128),
    LARGE(256, 256);

    final int width;
    final int height;

    ImageSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

}
