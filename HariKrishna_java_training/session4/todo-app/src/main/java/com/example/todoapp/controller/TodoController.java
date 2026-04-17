package com.example.todoapp.controller;

import com.example.todoapp.dto.TodoDTO;
import com.example.todoapp.service.TodoService;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @PostMapping
    public TodoDTO create(@Valid @RequestBody TodoDTO dto) {
        return service.createTodo(dto);
    }

    @GetMapping
    public List<TodoDTO> getAll() {
        return service.getAllTodos();
    }

    @GetMapping("/{id}")
    public TodoDTO getById(@PathVariable Long id) {
        return service.getTodoById(id);
    }

    @PutMapping("/{id}")
    public TodoDTO update(@PathVariable Long id, @RequestBody TodoDTO dto) {
        return service.updateTodo(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteTodo(id);
    }
}