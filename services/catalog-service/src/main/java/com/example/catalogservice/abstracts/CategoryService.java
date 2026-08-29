package com.example.catalogservice.abstracts;

import com.example.catalogservice.dtos.CreateCategory;
import com.example.catalogservice.dtos.UpdateCategory;
import com.example.catalogservice.models.Categories;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Categories createCategory(CreateCategory category);
    List<Categories> getAllCategories();
    Categories updateCategory(UUID categoryId, UpdateCategory category);
    void deleteCategory(UUID categoryId);
}
