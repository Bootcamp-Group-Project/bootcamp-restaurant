package com.example.canteen.dto;

import java.math.BigDecimal;
import com.example.canteen.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
