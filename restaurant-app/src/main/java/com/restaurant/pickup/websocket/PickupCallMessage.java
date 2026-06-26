package com.restaurant.pickup.websocket;

public record PickupCallMessage(String type, Long pickupId, String method, String message) {

    public static PickupCallMessage of(Long pickupId, String method) {
        return new PickupCallMessage("PICKUP_CALL", pickupId, method, "請 " + method + " 號取餐");
    }
}
