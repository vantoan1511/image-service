package com.shopbee.imageservice.image;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetAvatarRequest {

    @NotNull(message = "UserId must not be null")
    private Long userId;

    @NotNull(message = "ImageId must not be null")
    private Long imageId;
}
