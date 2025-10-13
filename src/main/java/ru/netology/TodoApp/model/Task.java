package ru.netology.TodoApp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;

    @NotBlank(message = "Описание задачи не может быть пустым")
    @Size(min = 3, max = 255, message = "Описание задачи должно быть от 3 до 255 символов")
    private String description;
    private boolean completed;

    public Task(String description) {
        this.description = description;
        this.completed = false;
    }
}