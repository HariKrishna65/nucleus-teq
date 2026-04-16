// package com.example.spring_rest_app.repository;

// import com.example.spring_rest_app.model.User;
// import org.springframework.stereotype.Repository;
// import java.util.ArrayList;
// import java.util.List;

// @Repository
// public class UserRepository {

//     private final List<User> users = new ArrayList<>();

//     public UserRepository() {
//         users.add(new User(1L, "Priya", 25, "USER"));
//         users.add(new User(2L, "Rahul", 30, "ADMIN"));
//         users.add(new User(3L, "Anjali", 28, "USER"));
//         users.add(new User(4L, "Kiran", 30, "USER"));
//         users.add(new User(5L, "Arjun", 35, "ADMIN"));
//     }

//     public List<User> getAllUsers() {
//         return users;
//     }

//     public void deleteById(Long id) {
//         users.removeIf(user -> user.getId().equals(id));
//     }
// }
