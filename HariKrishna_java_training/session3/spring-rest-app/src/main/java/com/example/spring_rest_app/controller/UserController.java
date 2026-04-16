// package com.example.spring_rest_app.controller;

// import com.example.spring_rest_app.model.User;
// import com.example.spring_rest_app.service.UserService;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Map;

// @RestController
// public class UserController {

//     private final UserService service;

//     public UserController(UserService service) {
//         this.service = service;
//     }

//     @GetMapping("/users/search")
//     public ResponseEntity<List<User>> searchUsers(
//             @RequestParam(required = false) String name,
//             @RequestParam(required = false) Integer age,
//             @RequestParam(required = false) String role
//     ) {
//         return ResponseEntity.ok(service.searchUsers(name, age, role));
//     }

//     @PostMapping("/submit")
//     public ResponseEntity<?> submit(@RequestBody Map<String, Object> data) {

//         if (data == null || data.isEmpty()) {
//             return ResponseEntity.badRequest().body("Invalid input");
//         }

//         return ResponseEntity.status(201).body("Data submitted successfully");
//     }

//     @DeleteMapping("/users/{id}")
//     public ResponseEntity<String> delete(
//             @PathVariable Long id,
//             @RequestParam(required = false) Boolean confirm
//     ) {
//         if (confirm == null || !confirm) {
//             return ResponseEntity.badRequest().body("Confirmation required");
//         }

//         service.deleteUser(id);
//         return ResponseEntity.ok("User deleted successfully");
//     }
// }
