package com.restaurant.menu.scheduler;

import com.restaurant.menu.entity.Dish;
import com.restaurant.menu.entity.Menu;
import com.restaurant.menu.repository.DishRepository;
import com.restaurant.menu.repository.MenuRepository;
import com.restaurant.menu.util.StockStatusUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStockResetScheduler {

    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;

    @Scheduled(cron = "${canteen.scheduler.daily-reset-cron:0 0 0 * * *}",
               zone = "${canteen.order.zone:Asia/Hong_Kong}")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "menus-today",  allEntries = true),
        @CacheEvict(value = "dishes-today", allEntries = true),
        @CacheEvict(value = "drinks-today", allEntries = true),
        @CacheEvict(value = "menu-by-id",   allEntries = true),
        @CacheEvict(value = "dish-by-id",   allEntries = true),
        @CacheEvict(value = "drink-by-id",  allEntries = true)
    })
    public void resetDailyStock() {
        LocalDate today = LocalDate.now();

        List<Menu> menus = menuRepository.findByMenuDate(today);
        for (Menu menu : menus) {
            menu.setBalance(menu.getInitialStock());
            menu.setStatus(StockStatusUtil.syncStatus(menu.getBalance()));
        }

        List<Dish> dishes = dishRepository.findByMenuDate(today);
        for (Dish dish : dishes) {
            dish.setBalance(dish.getInitialStock());
            dish.setStatus(StockStatusUtil.syncStatus(dish.getBalance()));
        }

        String chineseDay = toChinese(today.getDayOfWeek());
        List<Dish> weeklyDishes = dishRepository.findByCategory(chineseDay);
        for (Dish dish : weeklyDishes) {
            dish.setBalance(dish.getInitialStock());
            dish.setStatus(StockStatusUtil.syncStatus(dish.getBalance()));
        }

        log.info("Daily stock reset completed for {} ({}), weekly dishes: {}",
                today, chineseDay, weeklyDishes.size());
    }

    private static String toChinese(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY    -> "週一";
            case TUESDAY   -> "週二";
            case WEDNESDAY -> "週三";
            case THURSDAY  -> "週四";
            case FRIDAY    -> "週五";
            case SATURDAY  -> "週六";
            default        -> "";
        };
    }
}
