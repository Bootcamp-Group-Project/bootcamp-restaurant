package com.restaurant.menu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BalanceUpdateRequest {
    @NotNull @PositiveOrZero
    private Integer balance;
}
