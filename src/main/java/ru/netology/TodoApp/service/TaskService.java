package ru.netology.TodoApp.service;

import ru.netology.TodoApp.model.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {
    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public Task createTask(Task task) {
        task.setId(counter.incrementAndGet());
        tasks.add(task);
        return task;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public Optional<Task> getTaskById(Long id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    public Optional<Task> updateTask(Long id, Task updatedTask) {
        return getTaskById(id).map(existingTask -> {
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setCompleted(updatedTask.isCompleted());
            return existingTask;
        });
    }

    public boolean deleteTask(Long id) {
        return tasks.removeIf(task -> task.getId().equals(id));
    }
}