package com.example.catalogservice.controllers;

import com.example.catalogservice.abstracts.ProductService;
import com.example.catalogservice.dtos.CreateProduct;
import com.example.catalogservice.dtos.PaginatedResponse;
import com.example.catalogservice.dtos.UpdateProduct;
import com.example.catalogservice.models.Products;
import com.example.catalogservice.share.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/products")
public class ProductsController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<GlobalResponse<Products>> createProduct(@Valid @RequestBody CreateProduct product) {
        Products newProduct = productService.createProduct(product);
        return ResponseEntity.status(201)
        .body(
            new GlobalResponse<Products>(newProduct)
        );
    }
    
    @GetMapping
    public ResponseEntity<GlobalResponse<PaginatedResponse<Products>>> getAllProducts(
        @RequestParam(defaultValue="1") int page,
        @RequestParam(defaultValue="3") int size,
        HttpServletRequest request
    ) {
        Page<Products> productsPage = productService.getAllProducts(page-1, size);
        String baseUrl = request.getRequestURL().toString();
        String nextUrl = productsPage.hasNext() ? String.format("%s?page=%d&size=%d", baseUrl, page + 1, size) : null;
        String prevUrl = productsPage.hasPrevious() ? String.format("%s?page=%d&size=%d", baseUrl, page - 1, size) : null;

        var pagenatedResponse = new PaginatedResponse<Products> (
            productsPage.getContent(),
            productsPage.getNumber(),
            productsPage.getTotalPages(),
            productsPage.getTotalElements(),
            productsPage.hasNext(),
            productsPage.hasPrevious(),
            nextUrl,
            prevUrl
        );

        return ResponseEntity.ok(
            new GlobalResponse<PaginatedResponse<Products>>(pagenatedResponse)
        );
    }

    @GetMapping("/{productID}")
    public ResponseEntity<GlobalResponse<Products>> getProductsById(@PathVariable UUID productID) {
        Products product = productService.getProductsById(productID);
        return ResponseEntity.ok(
            new GlobalResponse<Products>(product)
        );
    }
    
    @PutMapping("/{productID}")
    public ResponseEntity<GlobalResponse<Products>> updateProduct(
        @PathVariable UUID productID,
        @Valid @RequestBody UpdateProduct product
    ) {
        Products updatedProduct = productService.updateProduct(productID, product);
        
        return ResponseEntity.ok(
            new GlobalResponse<Products>(updatedProduct)
        );
    }

    @DeleteMapping("/{productID}")
    public ResponseEntity<GlobalResponse<String>> deleteProduct(@PathVariable UUID productID) {
        productService.deleteProduct(productID);

        return ResponseEntity.status(204)
        .body(
            new GlobalResponse<String>("Product deleted successfully")
        );
    }

    @PostMapping("/{productId}/upload-image")
    public ResponseEntity<GlobalResponse<String>> uploadImage(
        @PathVariable UUID productId,
        @RequestParam MultipartFile image
    ) {
        String imagePath = productService.uploadImage(productId, image);
        return ResponseEntity.ok(
            new GlobalResponse<String>(imagePath)
        );
    }
}
