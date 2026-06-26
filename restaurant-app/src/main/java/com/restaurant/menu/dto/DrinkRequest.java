package com.restaurant.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class DrinkRequest {
    @NotBlank  private String name;
    @NotBlank  private String code;
    @NotNull @PositiveOrZero private BigDecimal price;
    @NotNull @PositiveOrZero private Integer balance;
    private String specialRequestOptions;
}
