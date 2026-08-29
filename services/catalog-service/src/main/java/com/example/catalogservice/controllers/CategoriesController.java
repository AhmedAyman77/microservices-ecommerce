package com.example.catalogservice.controllers;

import com.example.catalogservice.abstracts.CategoryService;
import com.example.catalogservice.dtos.CreateCategory;
import com.example.catalogservice.dtos.UpdateCategory;
import com.example.catalogservice.models.Categories;
import com.example.catalogservice.share.GlobalResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/categories")
public class CategoriesController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<GlobalResponse<Categories>> createCategory(@Valid @RequestBody CreateCategory category) {
        Categories newCategory = categoryService.createCategory(category);

        return  ResponseEntity.status(201)
        .body(
            new GlobalResponse<Categories>(newCategory)
        );
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<Categories>>> getAllCategories() {
        List<Categories> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new GlobalResponse<List<Categories>>(categories));
    }


    @PutMapping("/{categoryId}")
    public ResponseEntity<GlobalResponse<Categories>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategory category
    ) {
        Categories updatedCategory = categoryService.updateCategory(categoryId, category);
        return ResponseEntity.ok(new GlobalResponse<Categories>(updatedCategory));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<GlobalResponse<String>> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(new GlobalResponse<String>("Category deleted successfully"));
    }
}
