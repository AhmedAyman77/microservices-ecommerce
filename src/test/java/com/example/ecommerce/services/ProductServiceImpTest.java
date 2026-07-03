package com.example.ecommerce.services;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecommerce.dtos.CreateProduct;
import com.example.ecommerce.dtos.UpdateProduct;
import com.example.ecommerce.models.Categories;
import com.example.ecommerce.models.Products;
import com.example.ecommerce.repository.CategoriesRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.share.CustomException;

@ExtendWith(MockitoExtension.class)
class ProductServiceImpTest {

    @Mock private ProductRepository productRepo;
    @Mock private CategoriesRepository categoriesRepo;
    @Mock private StorageService storageService;

    @InjectMocks private ProductServiceImp productService;

    private UUID categoryId;
    private UUID productId;
    private Categories category;
    private Products existingProduct;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        productId  = UUID.randomUUID();

        category = new Categories();
        category.setId(categoryId);
        category.setName("Electronics");

        existingProduct = new Products();
        existingProduct.setId(productId);
        existingProduct.setName("Laptop");
        existingProduct.setQuantity(10);
        existingProduct.setPrice(new BigDecimal("999.99"));
        existingProduct.setCategory(category);
    }

    @Test
    void createProduct_validInput_savesAndReturnsProduct() {
        CreateProduct dto = new CreateProduct("Laptop", 10, new BigDecimal("999.99"), categoryId);

        when(categoriesRepo.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepo.save(any(Products.class))).thenReturn(existingProduct);

        Products result = productService.createProduct(dto);

        ArgumentCaptor<Products> captor = ArgumentCaptor.forClass(Products.class);
        verify(productRepo).save(captor.capture());

        assertEquals("Laptop", captor.getValue().getName());
        assertEquals(10, captor.getValue().getQuantity());
        assertEquals(new BigDecimal("999.99"), captor.getValue().getPrice());
        assertEquals(category, captor.getValue().getCategory());
        assertNotNull(result);
    }

    @Test
    void createProduct_categoryNotFound_throwsNotFound() {
        CreateProduct dto = new CreateProduct("Laptop", 10, new BigDecimal("999.99"), categoryId);
        when(categoriesRepo.findById(categoryId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> productService.createProduct(dto));

        assertEquals(404, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Category not found"));
        verify(productRepo, never()).save(any());
    }

    @Test
    void createProduct_negativeQuantity_throwsBadRequest() {
        CreateProduct dto = new CreateProduct("Laptop", -1, new BigDecimal("999.99"), categoryId);
        when(categoriesRepo.findById(categoryId)).thenReturn(Optional.of(category));

        CustomException ex = assertThrows(CustomException.class,
            () -> productService.createProduct(dto));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Product quantity cannot be negative", ex.getMessage());
        verify(productRepo, never()).save(any());
    }

    @Test
    void createProduct_negativePrice_throwsBadRequest() {
        CreateProduct dto = new CreateProduct("Laptop", 10, new BigDecimal("-5.00"), categoryId);
        when(categoriesRepo.findById(categoryId)).thenReturn(Optional.of(category));

        CustomException ex = assertThrows(CustomException.class,
            () -> productService.createProduct(dto));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Product price cannot be negative", ex.getMessage());
        verify(productRepo, never()).save(any());
    }

    @Test
    void createProduct_zeroPriceAndQuantity_isAllowed() {
        CreateProduct dto = new CreateProduct("FreeItem", 0, BigDecimal.ZERO, categoryId);
        when(categoriesRepo.findById(categoryId)).thenReturn(Optional.of(category));

        Products freeProduct = new Products();
        freeProduct.setId(UUID.randomUUID());
        freeProduct.setName("FreeItem");
        freeProduct.setQuantity(0);
        freeProduct.setPrice(BigDecimal.ZERO);
        freeProduct.setCategory(category);

        when(productRepo.save(any(Products.class))).thenReturn(freeProduct);

        assertDoesNotThrow(() -> productService.createProduct(dto));
        verify(productRepo).save(any());
    }

    @Test
    void updateProduct_updateName_onlyNameChanges() {
        UpdateProduct dto = new UpdateProduct("Gaming Laptop", null, null, null);
        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(any(Products.class))).thenReturn(existingProduct);

        Products result = productService.updateProduct(productId, dto);

        ArgumentCaptor<Products> captor = ArgumentCaptor.forClass(Products.class);
        verify(productRepo).save(captor.capture());
        assertEquals("Gaming Laptop", captor.getValue().getName());
        assertEquals(10, captor.getValue().getQuantity());
        assertEquals(new BigDecimal("999.99"), captor.getValue().getPrice());
    }

    @Test
    void updateProduct_updatePrice_onlyPriceChanges() {
        UpdateProduct dto = new UpdateProduct(null, null, new BigDecimal("1299.99"), null);
        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(any(Products.class))).thenReturn(existingProduct);

        productService.updateProduct(productId, dto);

        ArgumentCaptor<Products> captor = ArgumentCaptor.forClass(Products.class);
        verify(productRepo).save(captor.capture());
        assertEquals(new BigDecimal("1299.99"), captor.getValue().getPrice());
        assertEquals("Laptop", captor.getValue().getName());
    }

    @Test
    void updateProduct_updateCategory_resolvesNewCategory() {
        UUID newCategoryId = UUID.randomUUID();
        Categories newCategory = new Categories();
        newCategory.setId(newCategoryId);
        newCategory.setName("Computers");

        UpdateProduct dto = new UpdateProduct(null, null, null, newCategoryId);
        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoriesRepo.findById(newCategoryId)).thenReturn(Optional.of(newCategory));
        when(productRepo.save(any(Products.class))).thenReturn(existingProduct);

        productService.updateProduct(productId, dto);

        ArgumentCaptor<Products> captor = ArgumentCaptor.forClass(Products.class);
        verify(productRepo).save(captor.capture());
        assertEquals(newCategory, captor.getValue().getCategory());
    }

    @Test
    void updateProduct_allFieldsNull_noFieldsChange() {
        UpdateProduct dto = new UpdateProduct(null, null, null, null);
        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(any(Products.class))).thenReturn(existingProduct);

        productService.updateProduct(productId, dto);

        ArgumentCaptor<Products> captor = ArgumentCaptor.forClass(Products.class);
        verify(productRepo).save(captor.capture());
        assertEquals("Laptop", captor.getValue().getName());
        assertEquals(10, captor.getValue().getQuantity());
        assertEquals(new BigDecimal("999.99"), captor.getValue().getPrice());
    }

    @Test
    void updateProduct_productNotFound_throwsNotFound() {
        UpdateProduct dto = new UpdateProduct("New Name", null, null, null);
        when(productRepo.findById(productId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> productService.updateProduct(productId, dto));

        assertEquals(404, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Product not found"));
        verify(productRepo, never()).save(any());
    }

    @Test
    void updateProduct_negativeQuantity_throwsBadRequest() {
        UpdateProduct dto = new UpdateProduct(null, -5, null, null);
        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));

        CustomException ex = assertThrows(CustomException.class,
            () -> productService.updateProduct(productId, dto));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Product quantity cannot be negative", ex.getMessage());
        verify(productRepo, never()).save(any());
    }

    @Test
    void updateProduct_negativePrice_throwsBadRequest() {
        UpdateProduct dto = new UpdateProduct(null, null, new BigDecimal("-1.00"), null);
        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));

        CustomException ex = assertThrows(CustomException.class,
            () -> productService.updateProduct(productId, dto));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Product price cannot be negative", ex.getMessage());
        verify(productRepo, never()).save(any());
    }

    @Test
    void updateProduct_categoryNotFound_throwsNotFound() {
        UUID badCategoryId = UUID.randomUUID();
        UpdateProduct dto = new UpdateProduct(null, null, null, badCategoryId);
        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoriesRepo.findById(badCategoryId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> productService.updateProduct(productId, dto));

        assertEquals(404, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Category not found"));
        verify(productRepo, never()).save(any());
    }
}
