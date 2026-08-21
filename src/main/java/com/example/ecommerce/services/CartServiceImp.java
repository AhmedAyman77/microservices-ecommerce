package com.example.ecommerce.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.ecommerce.abstracts.CartService;
import com.example.ecommerce.models.CartItems;
import com.example.ecommerce.models.Carts;
import com.example.ecommerce.models.Products;
import com.example.ecommerce.models.Users;
import com.example.ecommerce.repository.CartItemsRepository;
import com.example.ecommerce.repository.CartsRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.share.CustomException;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImp implements CartService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartsRepository cartsRepository;


    @Override
    @Transactional
    public void addProduct(Authentication authentication, UUID productId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Users user = userRepository.findByUsername(username).orElseThrow(
                () -> CustomException.resourceNotFound("User not found")
        );

        Products product = productRepository.findById(productId).orElseThrow(
            () -> CustomException.resourceNotFound("Product not found")
        );

        Carts cart = cartsRepository.findByUserId_Id(user.getId())
            .orElseGet(() -> {
                Carts newCart = new Carts();
                newCart.setUserId(user);
                return cartsRepository.save(newCart);
            });

        CartItems cartItem = cartItemsRepository.findByCartId_IdAndProductId_Id(cart.getId(), product.getId());
        if(cartItem == null) {
            cartItem = new CartItems();
            cartItem.setCartId(cart);
            cartItem.setProductId(product);
            cartItem.setQuantity(1);
        }
        else {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        }

        cartItemsRepository.save(cartItem);
    }

    @Override
    public void removeProduct(Authentication authentication, UUID productId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Users user = userRepository.findByUsername(username).orElseThrow(
                () -> CustomException.resourceNotFound("User not found")
        );

        Products product = productRepository.findById(productId).orElseThrow(
            () -> CustomException.resourceNotFound("Product not found")
        );

        Carts cart = cartsRepository.findByUserId_Id(user.getId()).orElseThrow(
            () -> CustomException.resourceNotFound("Cart not found")
        );

        CartItems cartItem = cartItemsRepository.findByCartId_IdAndProductId_Id(cart.getId(), product.getId());
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
    public List<CartItems> getUserCart(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Users user = userRepository.findByUsername(username).orElseThrow(
                () -> CustomException.resourceNotFound("User not found")
        );

        Carts cart = cartsRepository.findByUserId_Id(user.getId()).orElseThrow(
            () -> CustomException.resourceNotFound("Cart not found")
        );

        return cartItemsRepository.findByCartId_Id(cart.getId()).orElse(List.of());
    }

}
