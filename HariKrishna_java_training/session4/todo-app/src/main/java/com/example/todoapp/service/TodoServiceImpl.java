package com.example.todoapp.service;

import com.example.todoapp.dto.TodoDTO;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.exception.InvalidStatusException;
import com.example.todoapp.exception.ResourceNotFoundException;
import com.example.todoapp.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TodoServiceImpl implements TodoService {

    private static final Logger logger = LoggerFactory.getLogger(TodoServiceImpl.class);

    private final TodoRepository repository;
    private final NotificationServiceClient notificationClient;

    public TodoServiceImpl(TodoRepository repository, NotificationServiceClient notificationClient) {
        this.repository = repository;
        this.notificationClient = notificationClient;
    }

    @Override
    public TodoDTO createTodo(TodoDTO dto) {
        logger.info("Creating new TODO request: title='{}'", dto.getTitle());

        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());

        if (dto.getStatus() == null) {
            todo.setStatus(Todo.Status.PENDING);
        } else {
            todo.setStatus(Todo.Status.valueOf(dto.getStatus()));
        }

        todo.setCreatedAt(LocalDateTime.now());

        Todo saved = repository.save(todo);

        String notification = "Notification sent for new TODO: " + saved.getTitle();
        notificationClient.sendNotification(notification);
        logger.info("Created TODO id={} title='{}'", saved.getId(), saved.getTitle());

        return mapToDTO(saved);
    }

    @Override
    public List<TodoDTO> getAllTodos() {
        logger.info("Retrieving all TODOs");
        return repository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TodoDTO getTodoById(Long id) {
        logger.info("Retrieving TODO by id={}", id);
        Todo todo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        return mapToDTO(todo);
    }

    @Override
    public TodoDTO updateTodo(Long id, TodoDTO dto) {
        logger.info("Updating TODO id={}", id);
        Todo existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        if (dto.getStatus() != null) {
            Todo.Status newStatus = Todo.Status.valueOf(dto.getStatus());

            boolean validTransition =
                    (existing.getStatus() == Todo.Status.PENDING && newStatus == Todo.Status.COMPLETED) ||
                    (existing.getStatus() == Todo.Status.COMPLETED && newStatus == Todo.Status.PENDING);

            if (!validTransition) {
                logger.warn("Invalid status transition from {} to {}", existing.getStatus(), newStatus);
                throw new InvalidStatusException("Invalid status transition");
            }

            existing.setStatus(newStatus);
        }

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());

        Todo updated = repository.save(existing);
        logger.info("Updated TODO id={} status={}", updated.getId(), updated.getStatus());
        return mapToDTO(updated);
    }

    @Override
    public void deleteTodo(Long id) {
        logger.info("Deleting TODO id={}", id);

        if (!repository.existsById(id)) {
            logger.warn("Delete failed: TODO id={} not found", id);
            throw new ResourceNotFoundException("Todo not found");
        }

        repository.deleteById(id);
        logger.info("Deleted TODO id={}", id);
    }

    private TodoDTO mapToDTO(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setStatus(todo.getStatus().name());
        return dto;
    }
}