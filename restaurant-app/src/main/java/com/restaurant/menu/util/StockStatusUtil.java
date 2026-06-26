package com.restaurant.menu.util;

import com.restaurant.menu.entity.Status;

public final class StockStatusUtil {

    private StockStatusUtil() {}

    public static Status resolveStatus(Integer balance) {
        return balance != null && balance > 0 ? Status.ON_LIST : Status.SOLD_OUT;
    }

    public static Status syncStatus(Integer balance) {
        return resolveStatus(balance);
    }
}
