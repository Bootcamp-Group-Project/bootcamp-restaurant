package com.example.canteen.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.canteen.dto.DrinkRequest;
import com.example.canteen.dto.DrinkResponse;
import com.example.canteen.entity.Drink;
import com.example.canteen.util.StockStatusUtil;
import com.example.canteen.exception.ResourceNotFoundException;
import com.example.canteen.repository.DrinkRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class DrinkService {

    private final DrinkRepository drinkRepository;
    private final ImageService imageService;
    private final OrderWindowService orderWindowService;

    public DrinkResponse create(DrinkRequest request, MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Drink image is required");
        }
        Drink drink = new Drink();
        applyRequest(drink, request);
        drink.setImageUrl(imageService.uploadImage(image, "drink"));
        return toResponse(drinkRepository.save(drink));
    }

    public DrinkResponse update(Long id, DrinkRequest request, MultipartFile image) throws Exception {
        Drink drink = findEntity(id);
        applyRequest(drink, request);
        if (image != null && !image.isEmpty()) {
            drink.setImageUrl(imageService.uploadImage(image, "drink"));
        }
        return toResponse(drinkRepository.save(drink));
    }

    public DrinkResponse uploadImage(Long id, MultipartFile image) throws Exception {
        Drink drink = findEntity(id);
        drink.setImageUrl(imageService.uploadImage(image, "drink"));
        return toResponse(drinkRepository.save(drink));
    }

    public void delete(Long id) {
        if (!drinkRepository.existsById(id)) {
            throw new ResourceNotFoundException("Drink not found: " + id);
        }
        drinkRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public DrinkResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<DrinkResponse> listAll() {
        return drinkRepository.findAll().stream().map(DrinkService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DrinkResponse> getTodayForClient() {
        orderWindowService.assertOpen();
        return drinkRepository.findAllAvailable().stream().map(DrinkService::toResponse).toList();
    }

    private Drink findEntity(Long id) {
        return drinkRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Drink not found: " + id));
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
