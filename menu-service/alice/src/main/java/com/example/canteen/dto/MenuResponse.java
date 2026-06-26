package com.example.canteen.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.example.canteen.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuResponse {

    private Long id;
    private String name;
    private LocalDate menuDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer initialStock;
    private Integer balance;
    private String imageUrl;
    private Status status;
    private List<DishResponse> dishes;
    private List<DrinkResponse> drinks;
}
