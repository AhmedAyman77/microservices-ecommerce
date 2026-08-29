package com.example.catalogservice.abstracts;

import com.example.catalogservice.dtos.CreateProduct;
import com.example.catalogservice.dtos.UpdateProduct;
import com.example.catalogservice.models.Products;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProductService {
    Products createProduct(CreateProduct product);
    Page<Products> getAllProducts(int page, int size);
    Products getProductsById(UUID productId);
    Products updateProduct(UUID productId, UpdateProduct product);
    void deleteProduct(UUID productId);
    public String uploadImage(UUID productId, MultipartFile image);
}
