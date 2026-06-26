package com.restaurant.menu.dto;

import java.util.List;

public record OrderWindowConfigResp(
        List<SlotResp> slots,
        String zone,
        boolean enforced
) {
    public record SlotResp(String start, String end) {}
}
