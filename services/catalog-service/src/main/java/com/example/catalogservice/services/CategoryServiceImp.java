package com.example.catalogservice.services;

import com.example.catalogservice.abstracts.CategoryService;
import com.example.catalogservice.dtos.CreateCategory;
import com.example.catalogservice.dtos.UpdateCategory;
import com.example.catalogservice.models.Categories;
import com.example.catalogservice.repository.CategoriesRepository;
import com.example.catalogservice.repository.ProductRepository;
import com.example.catalogservice.share.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImp implements CategoryService {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Categories createCategory(CreateCategory category) {
        if(categoriesRepository.existsByName(category.name())) {
            throw CustomException.conflict("Category already exists");
        }

        Categories newCategory = new Categories();
        newCategory.setName(category.name());

        return categoriesRepository.save(newCategory);
    }

    @Override
    public List<Categories> getAllCategories() {
        return categoriesRepository.findAll();
    }

    @Override
    public Categories updateCategory(UUID categoryId, UpdateCategory category) {
        Categories currCategory = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> CustomException.resourceNotFound("Category not found with id: " + categoryId));

        if(category.name() != null) {
            if(!category.name().equals(currCategory.getName()) && categoriesRepository.existsByName(category.name())) {
                throw CustomException.conflict("Category already exists");
            }

            currCategory.setName(category.name());
        }

        return categoriesRepository.save(currCategory);
    }

    @Override
    public void deleteCategory(UUID categoryId) {
        Categories category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> CustomException.resourceNotFound("Category not found with id: " + categoryId));

        if(productRepository.existsByCategory_Id(categoryId)) {
            throw CustomException.conflict("Cannot delete category that still has products assigned to it");
        }

        categoriesRepository.delete(category);
    }
}