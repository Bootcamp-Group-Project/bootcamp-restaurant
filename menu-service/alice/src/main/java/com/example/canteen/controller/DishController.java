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
import com.example.canteen.dto.BalanceUpdateRequest;
import com.example.canteen.dto.DishBalanceResponse;
import com.example.canteen.dto.DishBasicUpdateRequest;
import com.example.canteen.dto.DishRequest;
import com.example.canteen.dto.DishResponse;
import com.example.canteen.dto.SpecialRequestOptionsUpdateRequest;
import com.example.canteen.service.DishService;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DishResponse create(
            @Valid @RequestPart("data") DishRequest request, @RequestPart("image") MultipartFile image)
            throws Exception {
        return dishService.create(request, image);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DishResponse update(
            @PathVariable Long id,
            @Valid @RequestPart("data") DishRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image)
            throws Exception {
        return dishService.update(id, request, image);
    }

    @PatchMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DishResponse updateBalance(@PathVariable Long id, @Valid @RequestBody BalanceUpdateRequest request) {
        return dishService.updateBalance(id, request);
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN_USER', 'KITCHEN_USER')")
    public DishBalanceResponse getBalance(@PathVariable Long id) {
        return dishService.getBalance(id);
    }

    @PatchMapping("/{id}/basic-info")
    @PreAuthorize("hasRole('ADMIN_USER')")
    public DishResponse updateBasicInfo(@PathVariable Long id, @Valid @RequestBody DishBasicUpdateRequest request) {
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
    public DishResponse uploadImage(@PathVariable Long id, @RequestPart("image") MultipartFile image) throws Exception {
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
    public List<DishResponse> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dishService.listByDate(date);
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN_USER', 'KITCHEN_USER')")
    public List<DishResponse> getTodayForClient() {
        return dishService.getTodayForClient();
    }
}
