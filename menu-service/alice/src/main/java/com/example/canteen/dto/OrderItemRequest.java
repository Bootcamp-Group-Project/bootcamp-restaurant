package com.example.canteen.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    private Long dishId;
    private Long drinkId;

    @NotNull
    @Positive
    private Integer quantity;
}
