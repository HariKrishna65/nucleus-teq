package com.example.usernotificationsystem.controller;

import com.example.usernotificationsystem.model.User;
import com.example.usernotificationsystem.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users
    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    // POST /users
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // GET /users/{id}
    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return userService.getUserById(id);
    }
}
