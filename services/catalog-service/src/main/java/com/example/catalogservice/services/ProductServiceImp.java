package com.example.catalogservice.services;

import com.example.catalogservice.abstracts.ProductService;
import com.example.catalogservice.dtos.CreateProduct;
import com.example.catalogservice.dtos.UpdateProduct;
import com.example.catalogservice.models.Categories;
import com.example.catalogservice.models.Products;
import com.example.catalogservice.repository.CategoriesRepository;
import com.example.catalogservice.repository.ProductRepository;
import com.example.catalogservice.share.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProductServiceImp implements ProductService {
    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private CategoriesRepository categoriesRepo;

    @Autowired
    private StorageService storageService;

    @Override
    public Products createProduct(CreateProduct product) {
        Categories category = categoriesRepo.findById(product.categoryId())
        .orElseThrow(() -> CustomException.resourceNotFound("Category not found with id: " + product.categoryId()));
        
        if(product.quantity() < 0) {
            throw CustomException.badRequest("Product quantity cannot be negative");
        }

        if(product.price().compareTo(BigDecimal.ZERO) < 0) {
            throw CustomException.badRequest("Product price cannot be negative");
        }

        Products newProduct = new Products();
        newProduct.setName(product.name());
        newProduct.setQuantity(product.quantity());
        newProduct.setPrice(product.price());
        newProduct.setCategory(category);
        
        productRepo.save(newProduct);
        return newProduct;
    }
    
    @Override
    public Page<Products> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepo.findAll(pageable);
    }
    
    @Override
    public Products getProductsById(UUID productId) {
        Products product = productRepo.findById(productId)
        .orElseThrow(() -> CustomException.resourceNotFound("Product not found with id: " + productId));
        
        return product;
    }

    @Override
    public Products updateProduct(UUID productId, UpdateProduct product) {
        Products currProduct = productRepo.findById(productId)
        .orElseThrow(() -> CustomException.resourceNotFound("Product not found with id: " + productId));
        
        if(product.name() != null) {
            currProduct.setName(product.name());
        }
        if(product.quantity() != null) {
            if(product.quantity() < 0) {
                throw CustomException.badRequest("Product quantity cannot be negative");
            }

            currProduct.setQuantity(product.quantity());
        }
        if(product.price() != null) {
            if(product.price().compareTo(BigDecimal.valueOf(0)) < 0) {
                throw CustomException.badRequest("Product price cannot be negative");
            }

            currProduct.setPrice(product.price());
        }
        if(product.category() != null) {
            Categories category = categoriesRepo.findById(product.category())
            .orElseThrow(() -> CustomException.resourceNotFound("Category not found with id: " + product.category()));
            
            currProduct.setCategory(category);
        }

        return productRepo.save(currProduct);
    }

    @Override
    public void deleteProduct(UUID productId) {
        Products product = productRepo.findById(productId)
        .orElseThrow(() -> CustomException.resourceNotFound("Product not found with id: " + productId));
    
        productRepo.delete(product);
    }

    @Override
    public String uploadImage(UUID productId, MultipartFile image) {
        Products product = productRepo.findById(productId).orElseThrow(
            () -> CustomException.resourceNotFound("Product not found with id: " + productId)
        );

        String imagePath = storageService.uploadImage(image);
        product.setImagePath(imagePath);
        productRepo.save(product);

        return imagePath;
    }
}
