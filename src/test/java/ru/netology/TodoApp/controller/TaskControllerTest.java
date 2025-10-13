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

    // В данном случае, поскольку TaskService хранит данные в памяти,
    // он будет очищаться при каждом запуске теста @SpringBootTest.
    // Если бы мы использовали БД, могли бы использовать @BeforeEach для очистки.

    @BeforeEach
    void setUp() throws Exception {
        // Очистка всех задач перед каждым тестом, чтобы обеспечить изоляцию.
        // Поскольку у нас нет прямого доступа к TaskService для очистки,
        // мы можем удалить все задачи через API.
        mockMvc.perform(delete("/tasks/1")); // Удаляем задачу 1, если она есть
        mockMvc.perform(delete("/tasks/2")); // Удаляем задачу 2, если она есть
        // Это не идеальный способ, т.к. мы зависим от ID, которые генерирует сервис.
        // Более правильный подход для тестов в памяти - это иметь возможность
        // очищать TaskService напрямую или мокать его.
        // Для этого примера, давайте предположим, что каждый тест будет работать
        // с новым набором ID, начинающимся с 1.
        // Если бы TaskService был @Autowired здесь, мы бы делали taskService.clear();
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
        // Создадим несколько задач для теста
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new Task("Задача 1"))));
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(new Task("Задача 2"))));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Ожидаем 2 задачи
                .andExpect(jsonPath("$[0].description", is("Задача 1")))
                .andExpect(jsonPath("$[1].description", is("Задача 2")));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        // Сначала создадим задачу
        String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Task("Исходная задача"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);
        Long taskId = createdTask.getId();

        // Затем обновим ее
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

        mockMvc.perform(put("/tasks/999") // ID, которого точно нет
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        // Сначала создадим задачу
        String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Task("Задача для удаления"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);
        Long taskId = createdTask.getId();

        // Затем удалим ее
        mockMvc.perform(delete("/tasks/" + taskId))
                .andExpect(status().isNoContent());

        // Проверим, что задача действительно удалена
        mockMvc.perform(get("/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingTask() throws Exception {
        mockMvc.perform(delete("/tasks/999")) // ID, которого точно нет
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidTaskCreation() throws Exception {
        Task invalidTask = new Task(""); // Пустое описание
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidTaskUpdate() throws Exception {
        // Сначала создадим задачу
        String response = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Task("Valid Task"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Task createdTask = objectMapper.readValue(response, Task.class);
        Long taskId = createdTask.getId();

        Task invalidTask = new Task(""); // Пустое описание
        mockMvc.perform(put("/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists());
    }
}