package com.lambrk.service;

import com.lambrk.domain.Category;
import com.lambrk.dto.CategoryCreateRequest;
import com.lambrk.dto.CategoryResponse;
import com.lambrk.exception.DuplicateResourceException;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.repository.CategoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CustomMetrics customMetrics;

    public CategoryService(CategoryRepository categoryRepository, CustomMetrics customMetrics) {
        this.categoryRepository = categoryRepository;
        this.customMetrics = customMetrics;
    }

    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("Category", "slug", request.slug());
        }
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Category", "name", request.name());
        }

        Category category = new Category(
            null, request.name(), request.description(), request.iconUrl(),
            request.imageUrl(), request.color(), request.slug(), request.sortOrder(),
            null, null, null
        );

        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }

    @Cacheable(value = "categories", key = "#categoryId")
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return CategoryResponse.from(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return CategoryResponse.from(category);
    }

    @Cacheable(value = "categories", key = "'all-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAllOrdered(pageable)
            .map(CategoryResponse::from);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(UUID categoryId, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        Category updated = new Category(
            category.id(), request.name(), request.description(), request.iconUrl(),
            request.imageUrl(), request.color(), request.slug(), request.sortOrder(),
            category.communities(), category.createdAt(), null
        );

        Category saved = categoryRepository.save(updated);
        return CategoryResponse.from(saved);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        categoryRepository.delete(category);
    }
}
