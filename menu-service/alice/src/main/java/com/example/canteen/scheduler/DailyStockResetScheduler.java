package com.example.canteen.scheduler;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Menu;
import com.example.canteen.repository.DishRepository;
import com.example.canteen.repository.MenuRepository;
import com.example.canteen.util.StockStatusUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStockResetScheduler {

    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;

    @Scheduled(cron = "${canteen.scheduler.daily-reset-cron:0 0 0 * * *}", zone = "${canteen.order.zone:Asia/Hong_Kong}")
    @Transactional
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
        log.info("Daily stock reset completed for {}", today);
    }
}
