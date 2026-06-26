package com.restaurant.menu.dto;

import com.restaurant.menu.entity.Status;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Builder
public class DishResponse {
    private Long id;
    private String name;
    private String code;
    private BigDecimal price;
    private String imageUrl;
    private String specialRequestOptions;
    private Integer initialStock;
    private Integer balance;
    private Integer preparationTime;
    private LocalDate menuDate;
    private String category;
    private boolean published;
    private Status status;
}
