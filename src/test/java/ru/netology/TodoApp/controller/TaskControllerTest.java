package ru.netology.TodoApp.controller;

import ru.netology.TodoApp.model.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc.perform(delete("/tasks/1"));
        mockMvc.perform(delete("/tasks/2"));
    }


    @Test
    void shouldCreateTask() throws Exception {
        Task newTask = new Task("Купить хлеб");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description", is("Купить хлеб")))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void shouldReturnAllTasks() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new Task("Задача 1"))));
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new Task("Задача 2"))));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("Задача 1")))
                .andExpect(jsonPath("$[1].description", is("Задача 2")));
    }

    @Test
    void shouldUpdateTask() throws Exception {
       String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Task("Исходная задача"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);
        Long taskId = createdTask.getId();

        Task updatedTask = new Task("Обновленная задача");
        updatedTask.setCompleted(true);

        mockMvc.perform(put("/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(taskId.intValue())))
                .andExpect(jsonPath("$.description", is("Обновленная задача")))
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingTask() throws Exception {
        Task updatedTask = new Task("Несуществующая задача");
        updatedTask.setCompleted(true);

        mockMvc.perform(put("/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Task("Задача для удаления"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);
        Long taskId = createdTask.getId();

        mockMvc.perform(delete("/tasks/" + taskId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingTask() throws Exception {
        mockMvc.perform(delete("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidTaskCreation() throws Exception {
        Task invalidTask = new Task("");
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidTaskUpdate() throws Exception {
        String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Task("Valid Task"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);
        Long taskId = createdTask.getId();

        Task invalidTask = new Task("");
        mockMvc.perform(put("/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());
    }
}