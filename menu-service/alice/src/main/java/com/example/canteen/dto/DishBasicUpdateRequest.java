package com.example.canteen.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishBasicUpdateRequest {

    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;
}
