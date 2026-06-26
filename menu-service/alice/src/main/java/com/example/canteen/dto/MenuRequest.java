package com.example.canteen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuRequest {

    @NotBlank
    private String name;

    private LocalDate menuDate;

    @NotNull
    @PositiveOrZero
    private Integer initialStock;

    private Set<Long> dishIds = new HashSet<>();

    private Set<Long> drinkIds = new HashSet<>();
}
