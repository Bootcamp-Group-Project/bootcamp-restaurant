package com.restaurant.menu.dto;

import com.restaurant.menu.entity.Status;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class DrinkResponse {
    private Long id;
    private String name;
    private String code;
    private BigDecimal price;
    private String imageUrl;
    private String specialRequestOptions;
    private Integer balance;
    private Status status;
}
