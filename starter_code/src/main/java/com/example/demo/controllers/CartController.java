package com.example.demo.controllers;

import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.logger.CartLogger;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.IntStream;

import static com.example.demo.enumerate.Status.FAIL;
import static com.example.demo.enumerate.Status.SUCCESS;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private CartLogger logger = new CartLogger();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @PostMapping("/addToCart")
    public ResponseEntity<Cart> addTocart(@RequestBody ModifyCartRequest request) throws NotFoundException, ValidationException {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            logger.writeLog(request, "User is not found.", FAIL);
            throw new NotFoundException("User is not found.");
        }
        Optional<Item> item = itemRepository.findById(request.getItemId());
        if (!item.isPresent()) {
            logger.writeLog(request, "Item is not found.", FAIL);
            throw new NotFoundException("Item is not found.");
        }
        Cart cart = user.getCart();
        IntStream.range(0, request.getQuantity())
                .forEach(i -> cart.addItem(item.get()));
        cartRepository.save(cart);
        logger.writeLog(request, "Add to cart successfully.", SUCCESS);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/removeFromCart")
    public ResponseEntity<Cart> removeFromCart(@RequestBody ModifyCartRequest request) throws ValidationException, NotFoundException {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            logger.writeLog(request, "User is not found.", FAIL);
            throw new NotFoundException("User is not found.");
        }
        Optional<Item> item = itemRepository.findById(request.getItemId());
        if (!item.isPresent()) {
            logger.writeLog(request, "Item is not found.", FAIL);
            throw new NotFoundException("Item is not found.");
        }
        Cart cart = user.getCart();
        IntStream.range(0, request.getQuantity())
                .forEach(i -> cart.removeItem(item.get()));
        cartRepository.save(cart);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/getCartDetail")
    public ResponseEntity<Cart> getCartDetail(@PathVariable String username) throws NotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User is not found.");
        }
        Cart cart = user.getCart();
        return ResponseEntity.ok(cart);
    }

}
