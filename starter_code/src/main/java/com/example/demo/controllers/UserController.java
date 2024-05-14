package com.example.demo.controllers;

import com.example.demo.exception.ValidationException;
import com.example.demo.logger.UserLogger;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.example.demo.enumerate.Status.FAIL;
import static com.example.demo.enumerate.Status.SUCCESS;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private UserLogger logger = new UserLogger();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/id/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return ResponseEntity.of(userRepository.findById(id));
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> findByUserName(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    public ResponseEntity createUser(@RequestBody CreateUserRequest createUserRequest) throws ValidationException, IOException {
        if (userRepository.findByUsername(createUserRequest.getUsername()) != null) {
            logger.writeLog(createUserRequest, "Username is existed.", FAIL);
            throw new ValidationException("Username is existed.");
        } else if (createUserRequest.getPassword().length() < 7) {
            logger.writeLog(createUserRequest, "Password must have more than 7 characters.", FAIL);
            throw new ValidationException("Password must have more than 7 characters.");
        } else if (!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())) {
            logger.writeLog(createUserRequest, "Password and confirm password do not match.", FAIL);
            throw new ValidationException("Password and confirm password do not match.");
        }
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        Cart cart = new Cart();
        cartRepository.save(cart);
        user.setCart(cart);
        user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
        userRepository.save(user);
        logger.writeLog(createUserRequest, "Created user successfully.", SUCCESS);
        return ResponseEntity.ok(user);

    }

}
