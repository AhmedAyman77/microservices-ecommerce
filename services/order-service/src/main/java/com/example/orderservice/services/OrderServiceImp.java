package com.example.orderservice.services;

import com.example.orderservice.abstracts.OrderService;
import com.example.orderservice.dtos.*;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.events.OrderEventProducer;
import com.example.orderservice.models.OrderItems;
import com.example.orderservice.models.Orders;
import com.example.orderservice.repository.OrderItemsRepository;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.share.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImp implements OrderService {
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public Orders checkout(UUID userId) {
        ApiResponse<List<CartItemsResponse>> cartApiResponse = webClientBuilder.build().get()
                .uri("http://cart-service/cart/")
                .header(HttpHeaders.AUTHORIZATION, getIncomingAuthHeader())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<CartItemsResponse>>>() {})
                .block();

        List<CartItemsResponse> cartItems = cartApiResponse != null ? cartApiResponse.data() : null;

        if (cartItems == null || cartItems.isEmpty()) {
            throw CustomException.resourceNotFound("Cart is empty");
        }

        Orders order = new Orders();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.ZERO);
        Orders savedOrder = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItemsResponse cartItem : cartItems) {
            UUID productId = cartItem.productId();

             ProductResponse product = callGetProduct(productId);

            if(product == null) {
                throw CustomException.resourceNotFound("Product not found");
            }

            if (product.quantity() < cartItem.quantity()) {
                throw CustomException.badRequest(
                        "Insufficient stock for product: " + product.name()
                );
            }

//            create updated product obj for calling updateProduct endpoint
            UpdateProduct updatedProduct = new UpdateProduct(
                    null,
                    product.quantity() - cartItem.quantity(),
                    null,
                    null
            );

//                call catalog-service to update product quantity
            callUpdateProduct(productId, updatedProduct);


            BigDecimal itemTotal = product.price()
                    .multiply(BigDecimal.valueOf(cartItem.quantity()));

            OrderItems orderItem = new OrderItems();
            orderItem.setOrderId(savedOrder);
            orderItem.setProductId(productId);
            orderItem.setQuantity(cartItem.quantity());
            orderItem.setPrice(product.price());
            orderItem.setTotalPrice(itemTotal);
            orderItemsRepository.save(orderItem);

            total = total.add(itemTotal);
        }

        savedOrder.setTotalPrice(total);
        orderRepository.save(savedOrder);

//        clear cart
        webClientBuilder.build()
                .delete()
                .uri("http://cart-service/cart")
                .header(HttpHeaders.AUTHORIZATION, getIncomingAuthHeader())
                .retrieve()
                .bodyToMono(void.class)
                .block();

//        payment

//        notification
//        publish event -> notification-service will pick it up and send the confirmation email
        String email = getUserEmail();
        orderEventProducer.publishOrderPlaced(email, savedOrder.getId(), total);

        return savedOrder;
    }

    @Override
    public List<Orders> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId);
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
                ProductResponse product = callGetProduct(item.getProductId());

                if(product == null) {
                    throw CustomException.resourceNotFound("Product not found");
                }

                UpdateProduct updatedProduct = new UpdateProduct(
                        null,
                        product.quantity() + item.getQuantity(),
                        null,
                        null
                );

//                call catalog-service to update product quantity
                callUpdateProduct(item.getProductId(), updatedProduct);
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

//   private methods

    private String getIncomingAuthHeader() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            throw CustomException.resourceNotFound("No active request context");
        }

        HttpServletRequest request = attrs.getRequest();
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw CustomException.resourceNotFound("Missing or invalid Authorization header");
        }

        return authHeader;
    }

    private ProductResponse callGetProduct(UUID productId) {
        ApiResponse<ProductResponse> productApiResponse = webClientBuilder.build()
                .get()
                .uri("http://catalog-service/products/{productID}", productId)
                .header(HttpHeaders.AUTHORIZATION, getIncomingAuthHeader())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {})
                .block();

        ProductResponse product = productApiResponse != null ? productApiResponse.data() : null;

        return product;
    }

    private void callUpdateProduct(UUID productId, UpdateProduct updatedProduct) {
        webClientBuilder.build().put()
                .uri("http://catalog-service/products/{productID}", productId)
                .header(HttpHeaders.AUTHORIZATION, getIncomingAuthHeader())
                .bodyValue(updatedProduct)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {})
                .block();
    }

    private String getUserEmail() {
        ApiResponse<UserResponse> userApiResponse = webClientBuilder.build()
                .get()
                .uri("http://identity-service/users/me")
                .header(HttpHeaders.AUTHORIZATION, getIncomingAuthHeader())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponse>>() {})
                .block();

        UserResponse user = userApiResponse != null ? userApiResponse.data() : null;

        if (user == null) {
            throw CustomException.resourceNotFound("User not found");
        }

        return user.email();
    }
}