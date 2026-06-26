package com.example.canteen.dto;

import com.example.canteen.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DishBalanceResponse {
    private Long dishId;
    private Integer balance;
    private Status status;
}
