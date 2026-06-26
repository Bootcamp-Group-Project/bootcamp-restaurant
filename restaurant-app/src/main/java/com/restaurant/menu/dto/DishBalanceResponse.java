package com.restaurant.menu.dto;

import com.restaurant.menu.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class DishBalanceResponse {
    private Long dishId;
    private Integer balance;
    private Status status;
}
