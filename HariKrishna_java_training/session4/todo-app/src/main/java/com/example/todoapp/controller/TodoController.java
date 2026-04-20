package com.example.todoapp.controller;

import com.example.todoapp.dto.TodoDTO;
import com.example.todoapp.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @PostMapping
    public TodoDTO create(@Valid @RequestBody TodoDTO dto) {
        logger.info("Received create TODO request: title='{}'", dto.getTitle());
        TodoDTO result = service.createTodo(dto);
        logger.info("Created TODO response: title='{}' status='{}'", result.getTitle(), result.getStatus());
        return result;
    }

    @GetMapping
    public List<TodoDTO> getAll() {
        logger.info("Received request to retrieve all TODOs");
        return service.getAllTodos();
    }

    @GetMapping("/{id}")
    public TodoDTO getById(@PathVariable Long id) {
        logger.info("Received request to retrieve TODO id={}", id);
        return service.getTodoById(id);
    }

    @PutMapping("/{id}")
    public TodoDTO update(@PathVariable Long id, @RequestBody TodoDTO dto) {
        logger.info("Received request to update TODO id={}", id);
        return service.updateTodo(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        logger.info("Received request to delete TODO id={}", id);
        service.deleteTodo(id);
    }
}