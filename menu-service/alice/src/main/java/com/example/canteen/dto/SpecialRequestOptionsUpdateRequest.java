package com.example.canteen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialRequestOptionsUpdateRequest {

    /**
     * Comma-separated options, e.g. "Less Rice,No Onion,Extra Spicy"
     */
    @NotBlank
    private String specialRequestOptions;
}
