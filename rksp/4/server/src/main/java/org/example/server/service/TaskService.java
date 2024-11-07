package org.example.server.service;

import org.example.server.model.Task;
import org.example.server.repositoies.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class TaskService implements CommandLineRunner {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Mono<Task> createTask(String description) {
        Task task = new Task();
        task.setDescription(description);
        task.setCreatedAt(LocalDateTime.now());
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    public Flux<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Mono<Object> completeTask(Long taskId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setCompleted(true);
                    return task;
                }).map(o -> taskRepository.save(o));
    }
    public Mono<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(taskRepository.findAll().blockFirst());

    }
}