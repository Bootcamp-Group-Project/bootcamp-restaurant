package com.restaurant.menu.controller;

import com.restaurant.menu.dto.DrinkRequest;
import com.restaurant.menu.dto.DrinkResponse;
import com.restaurant.menu.service.DrinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/drinks")
@RequiredArgsConstructor
public class DrinkController {

    private final DrinkService drinkService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DrinkResponse create(
            @Valid @RequestPart("data") DrinkRequest request,
            @RequestPart("image") MultipartFile image) throws Exception {
        return drinkService.create(request, image);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DrinkResponse update(
            @PathVariable Long id,
            @Valid @RequestPart("data") DrinkRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        return drinkService.update(id, request, image);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DrinkResponse uploadImage(@PathVariable Long id,
                                     @RequestPart("image") MultipartFile image) throws Exception {
        return drinkService.uploadImage(id, image);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        drinkService.delete(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DrinkResponse getById(@PathVariable Long id) {
        return drinkService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public List<DrinkResponse> listAll() {
        return drinkService.listAll();
    }

    @GetMapping("/today")
    public List<DrinkResponse> getTodayForClient() {
        return drinkService.getTodayForClient();
    }
}
