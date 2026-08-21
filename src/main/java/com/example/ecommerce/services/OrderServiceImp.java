package com.example.ecommerce.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.ecommerce.abstracts.OrderService;
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

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImp implements OrderService {
    @Autowired
    private CartsRepository cartsRepository;

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Orders checkout(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Users user = userRepository.findByUsername(username).orElseThrow(
                () -> CustomException.resourceNotFound("User not found")
        );

        Carts cart = cartsRepository.findByUserId_Id(user.getId())
                .orElseThrow(() -> CustomException.resourceNotFound("Cart not found"));

        List<CartItems> cartItems = cartItemsRepository.findByCartId_Id(cart.getId()).orElseThrow(
                () -> CustomException.badRequest("Cart is empty")
        );

        Orders order = new Orders();
        order.setUserId(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.ZERO);
        Orders savedOrder = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItems cartItem : cartItems) {
            UUID productId = cartItem.getProductId();

            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> CustomException.resourceNotFound("Product not found"));

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw CustomException.badRequest(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            OrderItems orderItem = new OrderItems();
            orderItem.setOrderId(savedOrder);
            orderItem.setProductId(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotalPrice(itemTotal);
            orderItemsRepository.save(orderItem);

            total = total.add(itemTotal);
        }

        savedOrder.setTotalPrice(total);
        orderRepository.save(savedOrder);

        cartItemsRepository.deleteAll(cartItems);

        return savedOrder;
    }

    @Override
    public List<Orders> getOrdersByUserId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Users user = userRepository.findByUsername(username).orElseThrow(
                () -> CustomException.resourceNotFound("User not found")
        );

        if (!userRepository.existsById(user.getId())) {
            throw CustomException.resourceNotFound("User not found");
        }
        return orderRepository.findByUserId_Id(user.getId());
    }

    @Override
    public List<OrderItems> getOrderItemsByOrderId(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw CustomException.resourceNotFound("Order not found");
        }
        return orderItemsRepository.findByOrderId_Id(orderId);
    }

    @Override
    public List<Orders> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Orders updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> CustomException.resourceNotFound("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        boolean isLegalTransition =
                (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.PAID) ||
                        (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CANCELLED);

        if (!isLegalTransition) {
            throw CustomException.badRequest(
                    "Cannot change order status from " + currentStatus + " to " + newStatus
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {
            List<OrderItems> orderItems = orderItemsRepository.findByOrderId_Id(orderId);
            for (OrderItems item : orderItems) {
                Products product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> CustomException.resourceNotFound("Product not found"));
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}