package com.example.ecommerce.services;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.example.ecommerce.models.CartItems;
import com.example.ecommerce.models.Carts;
import com.example.ecommerce.models.Products;
import com.example.ecommerce.models.Users;
import com.example.ecommerce.repository.CartItemsRepository;
import com.example.ecommerce.repository.CartsRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.share.CustomException;

@ExtendWith(MockitoExtension.class)
class CartServiceImpTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CartsRepository cartsRepository;

    @Mock
    private CartItemsRepository cartItemsRepository;

    @InjectMocks
    private CartServiceImp cartService;

    private UUID userId;
    private UUID productId;
    private UUID cartId;
    private Users user;
    private Products product;
    private Carts cart;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();

        user = new Users();
        user.setId(userId);
        user.setUsername("ahmed");
        user.setEmail("ahmed@test.com");

        product = new Products();
        product.setId(productId);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setQuantity(10);

        cart = new Carts();
        cart.setId(cartId);
        cart.setUserId(user);
    }

    @Test
    void addProduct_newCart_newItem_createsCartAndSavesItem() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.empty());
        when(cartsRepository.save(any(Carts.class))).thenReturn(cart);
        when(cartItemsRepository.findByCartId_IdAndProductId_Id(cartId, productId)).thenReturn(null);

        cartService.addProduct(userId, productId);

        verify(cartsRepository).save(any(Carts.class));

        ArgumentCaptor<CartItems> captor = ArgumentCaptor.forClass(CartItems.class);
        verify(cartItemsRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getQuantity());
    }

    @Test
    void addProduct_existingCart_existingItem_incrementsQuantity() {
        CartItems existingItem = new CartItems();
        existingItem.setId(UUID.randomUUID());
        existingItem.setCartId(cart);
        existingItem.setProductId(product);
        existingItem.setQuantity(3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_IdAndProductId_Id(cartId, productId)).thenReturn(existingItem);

        cartService.addProduct(userId, productId);

        ArgumentCaptor<CartItems> captor = ArgumentCaptor.forClass(CartItems.class);
        verify(cartItemsRepository).save(captor.capture());
        assertEquals(4, captor.getValue().getQuantity());
    }

    @Test
    void addProduct_existingCart_newItem_savesItemWithQuantityOne() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_IdAndProductId_Id(cartId, productId)).thenReturn(null);

        cartService.addProduct(userId, productId);

        verify(cartsRepository, never()).save(any());
        ArgumentCaptor<CartItems> captor = ArgumentCaptor.forClass(CartItems.class);
        verify(cartItemsRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getQuantity());
    }

    @Test
    void addProduct_userNotFound_throwsInternalServerError() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> cartService.addProduct(userId, productId));

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void addProduct_productNotFound_throwsInternalServerError() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> cartService.addProduct(userId, productId));

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void removeProduct_itemQuantityAboveOne_decrementsQuantity() {
        CartItems cartItem = new CartItems();
        cartItem.setId(UUID.randomUUID());
        cartItem.setCartId(cart);
        cartItem.setProductId(product);
        cartItem.setQuantity(3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_IdAndProductId_Id(cartId, productId)).thenReturn(cartItem);

        cartService.removeProduct(userId, productId);

        ArgumentCaptor<CartItems> captor = ArgumentCaptor.forClass(CartItems.class);
        verify(cartItemsRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getQuantity());
        verify(cartItemsRepository, never()).delete(any());
    }

    @Test
    void removeProduct_itemQuantityIsOne_deletesItem() {
        CartItems cartItem = new CartItems();
        cartItem.setId(UUID.randomUUID());
        cartItem.setCartId(cart);
        cartItem.setProductId(product);
        cartItem.setQuantity(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_IdAndProductId_Id(cartId, productId)).thenReturn(cartItem);

        cartService.removeProduct(userId, productId);

        verify(cartItemsRepository).delete(cartItem);
        verify(cartItemsRepository, never()).save(any());
    }

    @Test
    void removeProduct_cartNotFound_throwsInternalServerError() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> cartService.removeProduct(userId, productId));

        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void removeProduct_itemNotInCart_throwsInternalServerError() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_IdAndProductId_Id(cartId, productId)).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class,
            () -> cartService.removeProduct(userId, productId));

        assertEquals(500, ex.getStatusCode());
    }
}
