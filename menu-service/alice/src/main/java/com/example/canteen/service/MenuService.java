package com.example.canteen.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.canteen.dto.DishResponse;
import com.example.canteen.dto.DrinkResponse;
import com.example.canteen.dto.BalanceUpdateRequest;
import com.example.canteen.dto.MenuRequest;
import com.example.canteen.dto.MenuResponse;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Drink;
import com.example.canteen.entity.Menu;
import com.example.canteen.exception.ResourceNotFoundException;
import com.example.canteen.repository.DishRepository;
import com.example.canteen.repository.DrinkRepository;
import com.example.canteen.repository.MenuRepository;
import com.example.canteen.util.StockStatusUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private static final LocalTime ORDER_START = LocalTime.of(11, 0);
    private static final LocalTime ORDER_END = LocalTime.of(14, 30);

    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final DrinkRepository drinkRepository;
    private final ImageService imageService;
    private final OrderWindowService orderWindowService;

    public MenuResponse create(MenuRequest request, MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Menu image is required");
        }
        Menu menu = new Menu();
        applyRequest(menu, request);
        menu.setImageUrl(imageService.uploadImage(image, "menu"));
        menu.setStatus(StockStatusUtil.syncStatus(menu.getBalance()));
        return toResponse(menuRepository.save(menu));
    }

    public MenuResponse update(Long id, MenuRequest request, MultipartFile image) throws Exception {
        Menu menu = findEntity(id);
        applyRequest(menu, request);
        if (image != null && !image.isEmpty()) {
            menu.setImageUrl(imageService.uploadImage(image, "menu"));
        }
        menu.setStatus(StockStatusUtil.syncStatus(menu.getBalance()));
        return toResponse(menuRepository.save(menu));
    }

    public MenuResponse updateStock(Long id, BalanceUpdateRequest request) {
        Menu menu = findEntity(id);
        menu.setBalance(request.getBalance());
        menu.setStatus(StockStatusUtil.syncStatus(menu.getBalance()));
        return toResponse(menuRepository.save(menu));
    }

    public MenuResponse uploadImage(Long id, MultipartFile image) throws Exception {
        Menu menu = findEntity(id);
        menu.setImageUrl(imageService.uploadImage(image, "menu"));
        return toResponse(menuRepository.save(menu));
    }

    public void delete(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu not found: " + id);
        }
        menuRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MenuResponse getById(Long id) {
        Menu menu = menuRepository
                .findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + id));
        return toResponse(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> listByDate(LocalDate date) {
        return menuRepository.findByMenuDate(date).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getTodayForClient() {
        orderWindowService.assertOpen();
        LocalDate today = LocalDate.now(orderWindowService.getZoneId());
        return menuRepository.findTodayAvailableWithItems(today).stream()
                .map(this::toResponse)
                .toList();
    }

    public void decreaseStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Menu menu = findEntity(id);
        if (menu.getBalance() < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        menu.setBalance(menu.getBalance() - quantity);
        menu.setStatus(StockStatusUtil.syncStatus(menu.getBalance()));
        menuRepository.save(menu);
    }

    private Menu findEntity(Long id) {
        return menuRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + id));
    }

    private void applyRequest(Menu menu, MenuRequest request) {
        menu.setName(request.getName());
        menu.setMenuDate(request.getMenuDate() != null ? request.getMenuDate() : LocalDate.now());
        menu.setStartTime(ORDER_START);
        menu.setEndTime(ORDER_END);
        menu.setInitialStock(request.getInitialStock());
        if (menu.getBalance() == null) {
            menu.setBalance(request.getInitialStock());
        }
        Set<Dish> dishes = resolveDishes(request.getDishIds());
        Set<Drink> drinks = resolveDrinks(request.getDrinkIds());
        if (dishes.isEmpty() && drinks.isEmpty()) {
            throw new IllegalArgumentException("At least one dish or drink is required");
        }
        menu.setDishes(dishes);
        menu.setDrinks(drinks);
    }

    private Set<Dish> resolveDishes(Set<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Dish> dishes = dishRepository.findAllById(dishIds);
        if (dishes.size() != dishIds.size()) {
            throw new ResourceNotFoundException("One or more dishes not found");
        }
        return new HashSet<>(dishes);
    }

    private Set<Drink> resolveDrinks(Set<Long> drinkIds) {
        if (drinkIds == null || drinkIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Drink> drinks = drinkRepository.findAllById(drinkIds);
        if (drinks.size() != drinkIds.size()) {
            throw new ResourceNotFoundException("One or more drinks not found");
        }
        return new HashSet<>(drinks);
    }

    private MenuResponse toResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .menuDate(menu.getMenuDate())
                .startTime(menu.getStartTime())
                .endTime(menu.getEndTime())
                .initialStock(menu.getInitialStock())
                .balance(menu.getBalance())
                .imageUrl(menu.getImageUrl())
                .status(menu.getStatus())
                .dishes(menu.getDishes().stream().map(DishService::toResponse).toList())
                .drinks(menu.getDrinks().stream().map(DrinkService::toResponse).toList())
                .build();
    }
}
