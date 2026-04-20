package com.example.todoapp.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.todoapp.controller.TodoController;
import com.example.todoapp.service.TodoService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        when(todoService.getTodoById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Todo not found"));

        mockMvc.perform(get("/todos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_whenNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Todo not found"))
                .when(todoService)
                .deleteTodo(anyLong());

        mockMvc.perform(delete("/todos/999"))
                .andExpect(status().isNotFound());
    }
}
