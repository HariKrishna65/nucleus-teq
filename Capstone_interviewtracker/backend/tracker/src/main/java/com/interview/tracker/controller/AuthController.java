package com.interview.tracker.controller;

import com.interview.tracker.entity.User;
import com.interview.tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService service;
    

    @PostMapping("/register")
    public User register(@RequestBody User user) {
    System.out.println("USER DATA: " + user.getName());
    return service.register(user);
   }
}