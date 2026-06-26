package com.example.canteen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrinkRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String code;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    private Integer balance;

    private String specialRequestOptions;
}
