package com.lambrk.controller;

import com.lambrk.dto.CategoryCreateRequest;
import com.lambrk.dto.CategoryResponse;
import com.lambrk.service.CategoryService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @NewSpan("create-category")
    @Counted(value = "categories.created")
    @Timed(value = "categories.create.duration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/{categoryId}")
    @NewSpan("get-category")
    @Timed(value = "categories.get.duration")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable @SpanTag UUID categoryId) {
        return ResponseEntity.ok(categoryService.getCategory(categoryId));
    }

    @GetMapping("/slug/{slug}")
    @NewSpan("get-category-by-slug")
    @Timed(value = "categories.getBySlug.duration")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @PathVariable @SpanTag String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @GetMapping
    @NewSpan("get-all-categories")
    @Timed(value = "categories.all.duration")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    @PutMapping("/{categoryId}")
    @NewSpan("update-category")
    @Counted(value = "categories.updated")
    @Timed(value = "categories.update.duration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable @SpanTag UUID categoryId,
            @Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    @NewSpan("delete-category")
    @Counted(value = "categories.deleted")
    @Timed(value = "categories.delete.duration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable @SpanTag UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
