package com.example.ecommerce.controllers;

import com.example.ecommerce.abstracts.ProductService;
import com.example.ecommerce.dtos.CreateProduct;
import com.example.ecommerce.dtos.UpdateProduct;
import com.example.ecommerce.models.Categories;
import com.example.ecommerce.models.Products;
import com.example.ecommerce.share.CustomException;
import com.example.ecommerce.share.GlobalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductsController productsController;

    private UUID productId;
    private UUID categoryId;
    private Products product;

    @BeforeEach
    void setUp() {
        productId  = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        Categories category = new Categories();
        category.setId(categoryId);
        category.setName("Electronics");

        product = new Products();
        product.setId(productId);
        product.setName("Laptop");
        product.setQuantity(10);
        product.setPrice(new BigDecimal("999.99"));
        product.setCategory(category);
    }

    @Test
    void createProduct_success_returns201() {
        CreateProduct dto = new CreateProduct("Laptop", 10, new BigDecimal("999.99"), categoryId);
        when(productService.createProduct(any(CreateProduct.class))).thenReturn(product);

        ResponseEntity<GlobalResponse<Products>> response = productsController.createProduct(dto);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("success", response.getBody().getStatus());
        assertEquals("Laptop", response.getBody().getData().getName());
        assertEquals(10, response.getBody().getData().getQuantity());
        assertEquals(new BigDecimal("999.99"), response.getBody().getData().getPrice());
    }

    @Test
    void createProduct_categoryNotFound_throwsException() {
        CreateProduct dto = new CreateProduct("Laptop", 10, new BigDecimal("999.99"), categoryId);
        when(productService.createProduct(any()))
            .thenThrow(CustomException.resourceNotFound("Category not found with id: " + categoryId));

        CustomException ex = assertThrows(CustomException.class,
            () -> productsController.createProduct(dto));

        assertEquals(404, ex.getStatusCode());
    }

    @Test
    void createProduct_negativeQuantity_throwsException() {
        CreateProduct dto = new CreateProduct("Laptop", -1, new BigDecimal("999.99"), categoryId);
        when(productService.createProduct(any()))
            .thenThrow(CustomException.badRequest("Product quantity cannot be negative"));

        CustomException ex = assertThrows(CustomException.class,
            () -> productsController.createProduct(dto));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Product quantity cannot be negative", ex.getMessage());
    }

    @Test
    void updateProduct_success_returns200() {
        UpdateProduct dto = new UpdateProduct("Gaming Laptop", 5, new BigDecimal("1299.99"), null);
        product.setName("Gaming Laptop");
        when(productService.updateProduct(eq(productId), any(UpdateProduct.class))).thenReturn(product);

        ResponseEntity<GlobalResponse<Products>> response = productsController.updateProduct(productId, dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Gaming Laptop", response.getBody().getData().getName());
    }

    @Test
    void updateProduct_productNotFound_throwsException() {
        UpdateProduct dto = new UpdateProduct("New Name", null, null, null);
        when(productService.updateProduct(eq(productId), any()))
            .thenThrow(CustomException.resourceNotFound("Product not found with id: " + productId));

        CustomException ex = assertThrows(CustomException.class,
            () -> productsController.updateProduct(productId, dto));

        assertEquals(404, ex.getStatusCode());
    }

    @Test
    void updateProduct_negativePrice_throwsException() {
        UpdateProduct dto = new UpdateProduct(null, null, new BigDecimal("-10.00"), null);
        when(productService.updateProduct(eq(productId), any()))
            .thenThrow(CustomException.badRequest("Product price cannot be negative"));

        CustomException ex = assertThrows(CustomException.class,
            () -> productsController.updateProduct(productId, dto));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Product price cannot be negative", ex.getMessage());
    }
}
