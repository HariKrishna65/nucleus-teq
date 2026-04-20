package com.example.todoapp.service;

import com.example.todoapp.dto.TodoDTO;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.exception.InvalidStatusException;
import com.example.todoapp.exception.ResourceNotFoundException;
import com.example.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TodoServiceImplTest {

    @Mock
    private TodoRepository repository;

    @Mock
    private NotificationServiceClient notificationClient;

    private TodoServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TodoServiceImpl(repository, notificationClient);
    }

    @Test
    void createTodo_savesTodoAndSendsNotification() {
        TodoDTO dto = new TodoDTO();
        dto.setTitle("Buy milk");
        dto.setDescription("Buy milk from the store");
        dto.setStatus(null);

        Todo saved = new Todo(1L, "Buy milk", "Buy milk from the store", Todo.Status.PENDING, LocalDateTime.now());
        when(repository.save(any(Todo.class))).thenReturn(saved);

        TodoDTO result = service.createTodo(dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Buy milk");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(repository).save(any(Todo.class));
        verify(notificationClient).sendNotification("Notification sent for new TODO: Buy milk");
    }

    @Test
    void getAllTodos_returnsMappedList() {
        Todo todo = new Todo(1L, "Task", "Description", Todo.Status.PENDING, LocalDateTime.now());
        when(repository.findAll()).thenReturn(List.of(todo));

        List<TodoDTO> result = service.getAllTodos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Task");
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        verify(repository).findAll();
    }

    @Test
    void getTodoById_whenMissing_throwsResourceNotFoundException() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTodoById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todo not found");
    }

    @Test
    void updateTodo_transitionsStatusWithValidData() {
        Todo existing = new Todo(1L, "Task", "Description", Todo.Status.PENDING, LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        TodoDTO dto = new TodoDTO();
        dto.setTitle("Updated task");
        dto.setDescription("Updated description");
        dto.setStatus("COMPLETED");

        Todo updated = new Todo(1L, "Updated task", "Updated description", Todo.Status.COMPLETED, existing.getCreatedAt());
        when(repository.save(any(Todo.class))).thenReturn(updated);

        TodoDTO result = service.updateTodo(1L, dto);

        assertThat(result.getTitle()).isEqualTo("Updated task");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(repository).save(any(Todo.class));
    }

    @Test
    void updateTodo_invalidTransition_throwsInvalidStatusException() {
        Todo existing = new Todo(1L, "Task", "Description", Todo.Status.PENDING, LocalDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        TodoDTO dto = new TodoDTO();
        dto.setTitle("Task");
        dto.setDescription("Description");
        dto.setStatus("PENDING");

        assertThatThrownBy(() -> service.updateTodo(1L, dto))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessage("Invalid status transition");

        verify(repository, never()).save(any(Todo.class));
    }

    @Test
    void deleteTodo_whenMissing_throwsResourceNotFoundException() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteTodo(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Todo not found");

        verify(repository).existsById(5L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void deleteTodo_whenExists_callsRepositoryDelete() {
        when(repository.existsById(5L)).thenReturn(true);

        service.deleteTodo(5L);

        verify(repository).deleteById(5L);
    }
}
