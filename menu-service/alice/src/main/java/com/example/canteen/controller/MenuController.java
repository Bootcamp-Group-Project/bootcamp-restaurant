package com.example.canteen.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.example.canteen.dto.MenuRequest;
import com.example.canteen.dto.MenuResponse;
import com.example.canteen.dto.BalanceUpdateRequest;
import com.example.canteen.service.MenuService;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // ---------- ADMIN / KITCHEN ----------
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse create(
            @Valid @RequestPart("data") MenuRequest request, @RequestPart("image") MultipartFile image)
            throws Exception {
        return menuService.create(request, image);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse update(
            @PathVariable Long id,
            @Valid @RequestPart("data") MenuRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image)
            throws Exception {
        return menuService.update(id, request, image);
    }

    @PatchMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse updateStock(@PathVariable Long id, @Valid @RequestBody BalanceUpdateRequest request) {
        return menuService.updateStock(id, request);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public MenuResponse uploadImage(@PathVariable Long id, @RequestPart("image") MultipartFile image) throws Exception {
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

    // ---------- CLIENT ----------
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN_USER', 'KITCHEN_USER')")
    public List<MenuResponse> getTodayForClient() {
        return menuService.getTodayForClient();
    }
}
