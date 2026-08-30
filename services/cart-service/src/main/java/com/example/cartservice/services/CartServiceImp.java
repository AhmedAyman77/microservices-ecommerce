package com.example.cartservice.services;

import com.example.cartservice.abstracts.CartService;
import com.example.cartservice.dtos.ApiResponse;
import com.example.cartservice.dtos.CartItemsResponse;
import com.example.cartservice.dtos.ProductResponse;
import com.example.cartservice.models.CartItems;
import com.example.cartservice.models.Carts;
import com.example.cartservice.repository.CartItemsRepository;
import com.example.cartservice.repository.CartsRepository;
import com.example.cartservice.share.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImp implements CartService {

    private final CartItemsRepository cartItemsRepository;
    private final CartsRepository cartsRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    @Transactional
    public void addProduct(UUID userId, UUID productId) {
        ProductResponse product = callGetProduct(productId);

        if(product == null) {
            throw CustomException.resourceNotFound("Product not found");
        }

        Carts userCart = getCartOrCreateOne(userId);

        CartItems cartItem = cartItemsRepository.findByCartId_IdAndProductId(userCart.getId(), productId);
        if(cartItem == null) {
            cartItem = new CartItems();
            cartItem.setCartId(userCart);
            cartItem.setProductId(productId);
            cartItem.setQuantity(1);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }

        cartItemsRepository.save(cartItem);
    }

    @Override
    public void removeProduct(UUID userId, UUID productId) {
        Carts cart = cartsRepository.findByUserId(userId).orElseThrow(
            () -> CustomException.resourceNotFound("Cart not found")
        );

        CartItems cartItem = cartItemsRepository.findByCartId_IdAndProductId(cart.getId(), productId);
        if(cartItem == null) {
            throw CustomException.resourceNotFound("Product not found in cart");
        }

        if(cartItem.getQuantity() > 1) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cartItemsRepository.save(cartItem);
        }
        else {
            cartItemsRepository.delete(cartItem);
        }
    }

    @Override
    @Transactional
    public void setProductQuantity(UUID userId, UUID productId, Integer quantity) {
        ProductResponse product = callGetProduct(productId);

        if(product == null) {
            throw CustomException.resourceNotFound("Product not found");
        }

        Carts userCart = getCartOrCreateOne(userId);

        CartItems cartItem = cartItemsRepository.findByCartId_IdAndProductId(userCart.getId(), productId);

        if(quantity == 0) {
            if(cartItem != null) {
                cartItemsRepository.delete(cartItem);
            }
            return;
        }

        if(cartItem == null) {
            cartItem = new CartItems();
            cartItem.setCartId(userCart);
            cartItem.setProductId(productId);
        }

        cartItem.setQuantity(quantity);
        cartItemsRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Carts cart = cartsRepository.findByUserId(userId).orElseThrow(
                () -> CustomException.resourceNotFound("Cart not found")
        );

        cartItemsRepository.deleteByCartId_Id(cart.getId());
    }

    @Override
    public List<CartItemsResponse> getUserCart(UUID userId) {
        Carts cart = cartsRepository.findByUserId(userId).orElseThrow(
            () -> CustomException.resourceNotFound("Cart not found")
        );

        List<CartItems> cartItems = cartItemsRepository.findByCartId_Id(cart.getId()).orElse(List.of());

        return convertCartItemToCartItemResponse(cartItems);
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

    private Carts getCartOrCreateOne(UUID userId) {
        return cartsRepository.findByUserId(userId)
                                .orElseGet(() -> {
                                    Carts newCart = new Carts();
                                    newCart.setUserId(userId);
                                    return cartsRepository.save(newCart);
                                });
    }

    private List<CartItemsResponse> convertCartItemToCartItemResponse(List<CartItems> cartItems) {
        List<CartItemsResponse> res = new ArrayList<>();

        for(CartItems cartItem: cartItems) {
            CartItemsResponse newCartItem = new CartItemsResponse(
                    cartItem.getCartId(),
                    cartItem.getProductId(),
                    cartItem.getQuantity()
            );

            res.add(newCartItem);
        }

        return res;
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
}
