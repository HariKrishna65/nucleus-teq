package com.example.spring_rest_app.service;

import com.example.spring_rest_app.model.User;
import com.example.spring_rest_app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> searchUsers(String name, Integer age, String role) {
        return repository.getAllUsers().stream()
                .filter(user -> name == null || user.getName().equalsIgnoreCase(name))
                .filter(user -> age == null || user.getAge().equals(age))
                .filter(user -> role == null || user.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}
