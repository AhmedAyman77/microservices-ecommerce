package com.example.ecommerce.abstracts;

import java.util.List;
import java.util.UUID;

import com.example.ecommerce.models.Categories;
import com.example.ecommerce.dtos.CreateCategory;
import com.example.ecommerce.dtos.UpdateCategory;

public interface CategoryService {
    Categories createCategory(CreateCategory category);
    List<Categories> getAllCategories();
    Categories updateCategory(UUID categoryId, UpdateCategory category);
    void deleteCategory(UUID categoryId);
}
