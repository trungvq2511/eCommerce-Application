package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {

    private UserController userController;
    private CartController cartController;
    private OrderController orderController;
    private ItemController itemController;

    private CartRepository cartRepository;

    private UserRepository userRepository;

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private ItemRepository itemRepository;
    private OrderRepository orderRepository;

    @Before
    public void init() {
        userController = new UserController();
        cartController = new CartController();
        orderController = new OrderController();
        itemController = new ItemController();
        //mock 3 object dependency
        cartRepository = Mockito.mock(CartRepository.class);
        bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        userRepository = Mockito.mock(UserRepository.class);
        itemRepository = Mockito.mock(ItemRepository.class);
        orderRepository = Mockito.mock(OrderRepository.class);

        //inject mock object dependency vào object target
        injectObject(userController, "cartRepository", cartRepository);
        injectObject(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);
        injectObject(userController, "userRepository", userRepository);

        injectObject(cartController, "userRepository", userRepository);
        injectObject(cartController, "cartRepository", cartRepository);
        injectObject(cartController, "itemRepository", itemRepository);

        injectObject(orderController, "userRepository", userRepository);
        injectObject(orderController, "orderRepository", orderRepository);

        injectObject(itemController, "itemRepository", itemRepository);
    }

    @Test
    public void testAll() throws ValidationException, NotFoundException, IOException {

        //test register user
        //success register
        CreateUserRequest userRequest = new CreateUserRequest();
        String username = "username";
        String password = "password";
        String encodedPassword = "encoded";
        userRequest.setUsername(username);
        userRequest.setPassword(password);
        userRequest.setConfirmPassword(password);

        Mockito.when(bCryptPasswordEncoder.encode(password)).thenReturn(encodedPassword);
        ResponseEntity<User> createUserResponse = userController.createUser(userRequest);
        User user = createUserResponse.getBody();

        assertEquals(userRequest.getUsername(), user.getUsername());
        assertEquals(encodedPassword, user.getPassword());

        //fail register
        String shortPassword = "123456";
        userRequest.setUsername(username);
        userRequest.setPassword(shortPassword);
        userRequest.setConfirmPassword(shortPassword);

        String notMatchPassword = "xxxxxx";
        userRequest.setUsername(username);
        userRequest.setPassword(password);
        userRequest.setConfirmPassword(notMatchPassword);

        //test find by user
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
        ResponseEntity<User> findByUserNameResponse = userController.findByUserName(username);
        User user2 = findByUserNameResponse.getBody();

        assertEquals(username, user2.getUsername());

        //test get item
        double price = 2.99;
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Round Widget");
        item1.setPrice(new BigDecimal(price));
        item1.setDescription("A widget that is round");

        double price2 = 1.99;
        Item item2 = new Item();
        item1.setId(2L);
        item2.setName("Square Widget");
        item2.setPrice(new BigDecimal(price2));
        item2.setDescription("A widget that is square");

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        ResponseEntity<Item> itemById = itemController.getItemById(1L);
        Item body1 = itemById.getBody();
        assertEquals(item1.getName(), "Round Widget");

        List<Item> items = new ArrayList<>();
        items.add(item2);
        Mockito.when(itemRepository.findByName("Square Widget")).thenReturn(items);
        ResponseEntity<List<Item>> listItem = itemController.getItemsByName("Square Widget");
        List<Item> body = listItem.getBody();
        Item itemget = body.get(0);
        assertEquals(itemget.getName(), "Square Widget");

        //test add to cart
        Item item = new Item();
        item.setName("Round Widget");
        item.setPrice(new BigDecimal(price));
        item.setDescription("A widget that is round");
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
        modifyCartRequest.setUsername(username);
        modifyCartRequest.setItemId(1);
        modifyCartRequest.setQuantity(2);

        ResponseEntity<Cart> cartResponseEntity = cartController.addTocart(modifyCartRequest);
        Cart cart = cartResponseEntity.getBody();
        assertEquals(cart.getItems().size(), 2);

        //test get cart detail
        ResponseEntity<Cart> cartDetail = cartController.getCartDetail(username);
        Cart body2 = cartDetail.getBody();
        assertEquals(body2.getItems().size(), 2);

        //test remove from cart
        modifyCartRequest.setUsername(username);
        modifyCartRequest.setItemId(1);
        modifyCartRequest.setQuantity(1);
        ResponseEntity<Cart> cartResponseEntity2 = cartController.removeFromCart(modifyCartRequest);
        Cart cart2 = cartResponseEntity.getBody();
        assertEquals(cart.getItems().size(), 1);

        //test submit order
        orderController.submit(username);

        //test get order
        ResponseEntity<List<UserOrder>> ordersForUser = orderController.getOrdersForUser(username);
    }

    public void injectObject(Object target, String fieldName, Object toInject) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            //inject dependency vào object target
            field.set(target, toInject);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
