package com.example.todoapp.controller;

import com.example.todoapp.dto.TodoDTO;
import com.example.todoapp.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @Test
    void create_returnsCreatedTodo() throws Exception {
        TodoDTO dto = new TodoDTO();
        dto.setTitle("Buy milk");
        dto.setDescription("Buy milk from the store");

        TodoDTO saved = new TodoDTO();
        saved.setTitle("Buy milk");
        saved.setDescription("Buy milk from the store");
        saved.setStatus("PENDING");

        when(todoService.createTodo(any(TodoDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(todoService).createTodo(any(TodoDTO.class));
    }

    @Test
    void getAll_returnsList() throws Exception {
        TodoDTO item = new TodoDTO();
        item.setTitle("Task");
        item.setDescription("Test");
        item.setStatus("PENDING");

        when(todoService.getAllTodos()).thenReturn(List.of(item));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task"));

        verify(todoService).getAllTodos();
    }
}
