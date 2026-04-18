package com.example.todoapp.service;

import com.example.todoapp.dto.TodoDTO;
import java.util.List;

public interface TodoService {
    TodoDTO createTodo(TodoDTO dto);
    List<TodoDTO> getAllTodos();
    TodoDTO getTodoById(Long id);
    TodoDTO updateTodo(Long id, TodoDTO dto);
    void deleteTodo(Long id);
}