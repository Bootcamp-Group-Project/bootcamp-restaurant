package com.restaurant.menu.service;

import com.restaurant.menu.dto.DrinkRequest;
import com.restaurant.menu.dto.DrinkResponse;
import com.restaurant.menu.entity.Drink;
import com.restaurant.menu.repository.DrinkRepository;
import com.restaurant.menu.util.StockStatusUtil;
import com.restaurant.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final ImageService imageService;
    private final OrderWindowService orderWindowService;

    @Caching(evict = {
        @CacheEvict(value = "drinks-today", allEntries = true),
        @CacheEvict(value = "menus-today",  allEntries = true)
    })
    public DrinkResponse create(DrinkRequest request, MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("Drink image is required");
        Drink drink = new Drink();
        applyRequest(drink, request);
        drink.setImageUrl(imageService.uploadImage(image, "drink"));
        return toResponse(drinkRepository.save(drink));
    }

    @Caching(evict = {
        @CacheEvict(value = "drink-by-id",  key = "#id"),
        @CacheEvict(value = "drinks-today", allEntries = true),
        @CacheEvict(value = "menus-today",  allEntries = true)
    })
    public DrinkResponse update(Long id, DrinkRequest request, MultipartFile image) throws Exception {
        Drink drink = findEntity(id);
        applyRequest(drink, request);
        if (image != null && !image.isEmpty()) {
            drink.setImageUrl(imageService.uploadImage(image, "drink"));
        }
        return toResponse(drinkRepository.save(drink));
    }

    @Caching(evict = {
        @CacheEvict(value = "drink-by-id",  key = "#id"),
        @CacheEvict(value = "drinks-today", allEntries = true),
        @CacheEvict(value = "menus-today",  allEntries = true)
    })
    public DrinkResponse uploadImage(Long id, MultipartFile image) throws Exception {
        Drink drink = findEntity(id);
        drink.setImageUrl(imageService.uploadImage(image, "drink"));
        return toResponse(drinkRepository.save(drink));
    }

    @Caching(evict = {
        @CacheEvict(value = "drink-by-id",  key = "#id"),
        @CacheEvict(value = "drinks-today", allEntries = true),
        @CacheEvict(value = "menus-today",  allEntries = true)
    })
    public void delete(Long id) {
        if (!drinkRepository.existsById(id)) throw new ResourceNotFoundException("Drink not found: " + id);
        drinkRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "drink-by-id", key = "#id")
    public DrinkResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<DrinkResponse> listAll() {
        return drinkRepository.findAll().stream().map(DrinkService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable("drinks-today")
    public List<DrinkResponse> getTodayForClient() {
        orderWindowService.assertOpen();
        return drinkRepository.findAllAvailable().stream().map(DrinkService::toResponse).toList();
    }

    private Drink findEntity(Long id) {
        return drinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drink not found: " + id));
    }

    private void applyRequest(Drink drink, DrinkRequest request) {
        drink.setName(request.getName());
        drink.setCode(request.getCode());
        drink.setPrice(request.getPrice());
        drink.setBalance(request.getBalance());
        drink.setStatus(StockStatusUtil.syncStatus(request.getBalance()));
        drink.setSpecialRequestOptions(request.getSpecialRequestOptions());
    }

    static DrinkResponse toResponse(Drink drink) {
        return DrinkResponse.builder()
                .id(drink.getId())
                .name(drink.getName())
                .code(drink.getCode())
                .price(drink.getPrice())
                .imageUrl(drink.getImageUrl())
                .specialRequestOptions(drink.getSpecialRequestOptions())
                .balance(drink.getBalance())
                .status(drink.getStatus())
                .build();
    }
}
