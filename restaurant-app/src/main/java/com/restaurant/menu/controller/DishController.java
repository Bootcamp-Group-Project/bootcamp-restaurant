package com.restaurant.menu.controller;

import com.restaurant.menu.dto.*;
import com.restaurant.menu.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DishResponse create(
            @Valid @RequestPart("data") DishRequest request,
            @RequestPart("image") MultipartFile image) throws Exception {
        return dishService.create(request, image);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DishResponse update(
            @PathVariable Long id,
            @Valid @RequestPart("data") DishRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        return dishService.update(id, request, image);
    }

    @PatchMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DishResponse updateBalance(@PathVariable Long id,
                                      @Valid @RequestBody BalanceUpdateRequest request) {
        return dishService.updateBalance(id, request);
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DishBalanceResponse getBalance(@PathVariable Long id) {
        return dishService.getBalance(id);
    }

    @PatchMapping("/{id}/basic-info")
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DishResponse updateBasicInfo(@PathVariable Long id,
                                        @Valid @RequestBody DishBasicUpdateRequest request) {
        return dishService.updateBasicInfo(id, request);
    }

    @PatchMapping("/{id}/special-request-options")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DishResponse updateSpecialRequestOptions(
            @PathVariable Long id,
            @Valid @RequestBody SpecialRequestOptionsUpdateRequest request) {
        return dishService.updateSpecialRequestOptions(id, request);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DishResponse uploadImage(@PathVariable Long id,
                                    @RequestPart("image") MultipartFile image) throws Exception {
        return dishService.uploadImage(id, image);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dishService.delete(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DishResponse getById(@PathVariable Long id) {
        return dishService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public List<DishResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return date != null ? dishService.listByDate(date) : dishService.listAll();
    }

    @GetMapping("/today")
    public List<DishResponse> getTodayForClient() {
        return dishService.getTodayForClient();
    }
}
