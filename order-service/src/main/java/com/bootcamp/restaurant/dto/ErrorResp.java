package com.bootcamp.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResp {
    private int status;
    private String message;
    private LocalDateTime timestamp;
}
