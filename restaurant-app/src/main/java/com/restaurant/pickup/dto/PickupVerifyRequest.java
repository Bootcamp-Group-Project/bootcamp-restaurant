package com.restaurant.pickup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PickupVerifyRequest {

    @NotNull(message = "item_id 唔可以空")
    private Long itemId;

    @NotNull(message = "admin_id 唔可以空")
    private Long adminId;

    @NotBlank(message = "核銷碼唔可以空")
    @Pattern(regexp = "^\\d{4}-\\d{3}$",
             message = "核銷碼格式錯誤，應為「手機後4碼-訂單末3碼」，例如：5678-023")
    private String method;
}
