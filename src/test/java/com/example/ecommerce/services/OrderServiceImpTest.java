package com.example.ecommerce.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.models.CartItems;
import com.example.ecommerce.models.Carts;
import com.example.ecommerce.models.OrderItems;
import com.example.ecommerce.models.Orders;
import com.example.ecommerce.models.Products;
import com.example.ecommerce.models.Users;
import com.example.ecommerce.repository.CartItemsRepository;
import com.example.ecommerce.repository.CartsRepository;
import com.example.ecommerce.repository.OrderItemsRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.share.CustomException;

@ExtendWith(MockitoExtension.class)
class OrderServiceImpTest {

    @Mock private UserRepository userRepository;
    @Mock private CartsRepository cartsRepository;
    @Mock private CartItemsRepository cartItemsRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemsRepository orderItemsRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private OrderServiceImp orderService;

    private UUID userId;
    private UUID cartId;
    private UUID productId;
    private Users user;
    private Products product;
    private Carts cart;
    private CartItems cartItem;
    private Orders savedOrder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        productId = UUID.randomUUID();

        user = new Users();
        user.setId(userId);
        user.setUsername("ahmed");

        product = new Products();
        product.setId(productId);
        product.setName("Laptop");
        product.setPrice(new BigDecimal("1000.00"));
        product.setQuantity(10);

        cart = new Carts();
        cart.setId(cartId);
        cart.setUserId(user);

        cartItem = new CartItems();
        cartItem.setId(UUID.randomUUID());
        cartItem.setCartId(cart);
        cartItem.setProductId(product);
        cartItem.setQuantity(2);

        savedOrder = new Orders();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setUserId(user);
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setTotalPrice(BigDecimal.ZERO);
    }

    @Test
    void checkout_happyPath_createsOrderAndClearsCart() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_Id(cartId)).thenReturn(Optional.of(List.of(cartItem)));
        when(orderRepository.save(any(Orders.class))).thenReturn(savedOrder);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Orders result = orderService.checkout(userId);

        verify(orderRepository, times(2)).save(any(Orders.class));

        verify(orderItemsRepository, times(1)).save(any(OrderItems.class));

        assertEquals(8, product.getQuantity());

        verify(cartItemsRepository).deleteAll(List.of(cartItem));

        assertNotNull(result);
    }

    @Test
    void checkout_verifiesOrderItemTotal() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_Id(cartId)).thenReturn(Optional.of(List.of(cartItem)));
        when(orderRepository.save(any(Orders.class))).thenReturn(savedOrder);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        orderService.checkout(userId);

        ArgumentCaptor<OrderItems> itemCaptor = ArgumentCaptor.forClass(OrderItems.class);
        verify(orderItemsRepository).save(itemCaptor.capture());
        assertEquals(new BigDecimal("2000.00"), itemCaptor.getValue().getTotalPrice());
    }

    @Test
    void checkout_multipleItems_sumsTotal() {
        UUID productId2 = UUID.randomUUID();
        Products product2 = new Products();
        product2.setId(productId2);
        product2.setName("Mouse");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setQuantity(5);

        CartItems cartItem2 = new CartItems();
        cartItem2.setId(UUID.randomUUID());
        cartItem2.setCartId(cart);
        cartItem2.setProductId(product2);
        cartItem2.setQuantity(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_Id(cartId))
            .thenReturn(Optional.of(List.of(cartItem, cartItem2)));
        when(orderRepository.save(any(Orders.class))).thenReturn(savedOrder);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findById(productId2)).thenReturn(Optional.of(product2));

        orderService.checkout(userId);

        ArgumentCaptor<Orders> orderCaptor = ArgumentCaptor.forClass(Orders.class);
        verify(orderRepository, times(2)).save(orderCaptor.capture());
        Orders finalOrder = orderCaptor.getAllValues().get(1);
        assertEquals(new BigDecimal("2050.00"), finalOrder.getTotalPrice());
    }

    @Test
    void checkout_insufficientStock_throwsBadRequest() {
        product.setQuantity(1);
        cartItem.setQuantity(5);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_Id(cartId)).thenReturn(Optional.of(List.of(cartItem)));
        when(orderRepository.save(any(Orders.class))).thenReturn(savedOrder);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CustomException ex = assertThrows(CustomException.class,
            () -> orderService.checkout(userId));

        assertEquals(400, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void checkout_userNotFound_throwsNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> orderService.checkout(userId));

        assertEquals(404, ex.getStatusCode());
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void checkout_cartNotFound_throwsNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> orderService.checkout(userId));

        assertEquals(404, ex.getStatusCode());
        assertEquals("Cart not found", ex.getMessage());
    }

    @Test
    void checkout_emptyCart_throwsBadRequest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartsRepository.findByUserId_Id(userId)).thenReturn(Optional.of(cart));
        when(cartItemsRepository.findByCartId_Id(cartId)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
            () -> orderService.checkout(userId));

        assertEquals(400, ex.getStatusCode());
        assertEquals("Cart is empty", ex.getMessage());
    }
}
