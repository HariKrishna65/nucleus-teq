package com.example.todoapp.service;

import com.example.todoapp.dto.TodoDTO;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.exception.ResourceNotFoundException;
import com.example.todoapp.exception.InvalidStatusException;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository repository;

    // ✅ Constructor Injection (MANDATORY)
    public TodoServiceImpl(TodoRepository repository) {
        this.repository = repository;
    }

    // ✅ CREATE TODO
    @Override
    public TodoDTO createTodo(TodoDTO dto) {

        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());

        // ✅ Default status
        if (dto.getStatus() == null) {
            todo.setStatus(Todo.Status.PENDING);
        } else {
            todo.setStatus(Todo.Status.valueOf(dto.getStatus()));
        }

        // ✅ Auto timestamp
        todo.setCreatedAt(LocalDateTime.now());

        Todo saved = repository.save(todo);
        return mapToDTO(saved);
    }

    // ✅ GET ALL TODOS
    @Override
    public List<TodoDTO> getAllTodos() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✅ GET TODO BY ID
    @Override
    public TodoDTO getTodoById(Long id) {
        Todo todo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        return mapToDTO(todo);
    }

    // ✅ UPDATE TODO
    @Override
    public TodoDTO updateTodo(Long id, TodoDTO dto) {

        Todo existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        // ✅ Status transition validation
        if (dto.getStatus() != null) {

            Todo.Status newStatus = Todo.Status.valueOf(dto.getStatus());

            boolean validTransition =
                    (existing.getStatus() == Todo.Status.PENDING && newStatus == Todo.Status.COMPLETED) ||
                    (existing.getStatus() == Todo.Status.COMPLETED && newStatus == Todo.Status.PENDING);

            if (!validTransition) {
                throw new InvalidStatusException("Invalid status transition");
            }

            existing.setStatus(newStatus);
        }

        // ✅ Update fields
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());

        Todo updated = repository.save(existing);
        return mapToDTO(updated);
    }

    // ✅ DELETE TODO
    @Override
    public void deleteTodo(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Todo not found");
        }

        repository.deleteById(id);
    }

    // ✅ Manual DTO Mapping (MANDATORY)
    private TodoDTO mapToDTO(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setStatus(todo.getStatus().name());
        return dto;
    }
}