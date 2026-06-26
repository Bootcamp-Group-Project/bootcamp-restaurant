package com.example.canteen.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.canteen.dto.OrderItemRequest;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Drink;
import com.example.canteen.entity.Status;
import com.example.canteen.exception.ResourceNotFoundException;
import com.example.canteen.repository.DishRepository;
import com.example.canteen.repository.DrinkRepository;
import com.example.canteen.util.StockStatusUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderValidationService {

    private final DishRepository dishRepository;
    private final DrinkRepository drinkRepository;

    // 檢查單一 Dish 是否可下單
    public void validateDishAvailable(Dish dish, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Dish quantity must be greater than 0");
        }
        Status currentStatus = StockStatusUtil.syncStatus(dish.getBalance());
        if (currentStatus == Status.SOLD_OUT || dish.getBalance() == null || dish.getBalance() < quantity) {
            throw new IllegalArgumentException("菜品 " + dish.getName() + " 已售完（SOLD_OUT）或庫存不足，無法下單");
        }
    }

    // 檢查單一 Drink 是否可下單
    public void validateDrinkAvailable(Drink drink, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Drink quantity must be greater than 0");
        }
        Status currentStatus = StockStatusUtil.syncStatus(drink.getBalance());
        if (currentStatus == Status.SOLD_OUT || drink.getBalance() == null || drink.getBalance() < quantity) {
            throw new IllegalArgumentException("飲品 " + drink.getName() + " 已售完（SOLD_OUT）或庫存不足，無法下單");
        }
    }

    // 檢查整個訂單
    public void validateOrderItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("訂單內容不可為空");
        }

        for (OrderItemRequest item : items) {
            if (item.getDishId() != null && item.getDrinkId() != null) {
                throw new IllegalArgumentException("每個品項只能是 Dish 或 Drink 其中之一");
            }
            if (item.getDishId() == null && item.getDrinkId() == null) {
                throw new IllegalArgumentException("每個品項必須指定 dishId 或 drinkId");
            }

            if (item.getDishId() != null) {
                Dish dish = dishRepository.findById(item.getDishId())
                        .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + item.getDishId()));
                validateDishAvailable(dish, item.getQuantity());
            }

            if (item.getDrinkId() != null) {
                Drink drink = drinkRepository.findById(item.getDrinkId())
                        .orElseThrow(() -> new ResourceNotFoundException("Drink not found: " + item.getDrinkId()));
                validateDrinkAvailable(drink, item.getQuantity());
            }
        }
    }
}
