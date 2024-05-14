package com.example.demo.controllers;

import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.logger.OrderLogger;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.example.demo.enumerate.Status.SUCCESS;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private OrderLogger orderLogger = new OrderLogger();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/submit/{username}")
    public ResponseEntity submit(@PathVariable String username) throws ValidationException, NotFoundException, IOException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User is not found.");
        }
        if (user.getCart().getItems().size() == 0) {
            return ResponseEntity.ok("There's nothing to check out the order. Please add something to cart.");
        }
        UserOrder order = UserOrder.createFromCart(user.getCart());
        UserOrder save = orderRepository.save(order);
        orderLogger.writeLog(save, user.getCart(),"Check out order successfully.", SUCCESS);

        //clear cart
        user.setCart(new Cart());
        userRepository.save(user);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/history/{username}")
    public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) throws NotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User is not found.");
        }
        return ResponseEntity.ok(orderRepository.findByUser(user));
    }
}
