package com.restaurant.menu.controller;

import com.restaurant.menu.dto.BalanceUpdateRequest;
import com.restaurant.menu.dto.MenuRequest;
import com.restaurant.menu.dto.MenuResponse;
import com.restaurant.menu.service.MenuService;
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
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse create(
            @Valid @RequestPart("data") MenuRequest request,
            @RequestPart("image") MultipartFile image) throws Exception {
        return menuService.create(request, image);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse update(
            @PathVariable Long id,
            @Valid @RequestPart("data") MenuRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        return menuService.update(id, request, image);
    }

    @PatchMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse updateStock(@PathVariable Long id,
                                    @Valid @RequestBody BalanceUpdateRequest request) {
        return menuService.updateStock(id, request);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse uploadImage(@PathVariable Long id,
                                    @RequestPart("image") MultipartFile image) throws Exception {
        return menuService.uploadImage(id, image);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        menuService.delete(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse getById(@PathVariable Long id) {
        return menuService.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public List<MenuResponse> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return menuService.listByDate(date);
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN_USER', 'KITCHEN_USER')")
    public List<MenuResponse> getTodayForClient() {
        return menuService.getTodayForClient();
    }
}
