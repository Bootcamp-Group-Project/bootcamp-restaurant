package com.example.canteen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.canteen.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
    private Status status;
}
