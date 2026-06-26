package com.restaurant.menu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SpecialRequestOptionsUpdateRequest {
    @NotBlank
    private String specialRequestOptions;
}
