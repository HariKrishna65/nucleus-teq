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

    // Constructor Injection
    public TodoServiceImpl(TodoRepository repository) {
        this.repository = repository;
    }

    @Override
    public TodoDTO createTodo(TodoDTO dto) {
        Todo todo = mapToEntity(dto);

        if (dto.getStatus() == null) {
            todo.setStatus(Todo.Status.PENDING);
        } else {
            todo.setStatus(parseStatus(dto.getStatus()));
        }

        todo.setCreatedAt(LocalDateTime.now());

        Todo saved = repository.save(todo);
        return mapToDTO(saved);
    }

    @Override
    public List<TodoDTO> getAllTodos() {
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TodoDTO getTodoById(Long id) {
        Todo todo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        return mapToDTO(todo);
    }

    @Override
    public TodoDTO updateTodo(Long id, TodoDTO dto) {
        Todo existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        // Validate status transition
        if (dto.getStatus() != null) {
            Todo.Status newStatus = parseStatus(dto.getStatus());

            boolean validTransition =
                    (existing.getStatus() == Todo.Status.PENDING && newStatus == Todo.Status.COMPLETED) ||
                    (existing.getStatus() == Todo.Status.COMPLETED && newStatus == Todo.Status.PENDING);

            if (validTransition) {
                existing.setStatus(newStatus);
            } else {
                throw new InvalidStatusException("Invalid status transition");
            }
        }

        if (dto.getTitle() != null) {
            existing.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }

        Todo updated = repository.save(existing);
        return mapToDTO(updated);
    }

    @Override
    public void deleteTodo(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Todo not found");
        }
        repository.deleteById(id);
    }

    // 🔁 Manual Mapping
    private Todo mapToEntity(TodoDTO dto) {
        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            todo.setStatus(parseStatus(dto.getStatus()));
        }

        return todo;
    }

    private TodoDTO mapToDTO(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setStatus(todo.getStatus().name());
        return dto;
    }

    // ✅ Safe Enum Parsing
    private Todo.Status parseStatus(Object status) {
        try {
            return Todo.Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new InvalidStatusException("Invalid status value: " + object);
        }
    }
}