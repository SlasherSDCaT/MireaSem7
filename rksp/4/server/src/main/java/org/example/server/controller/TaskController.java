package org.example.server.controller;

import org.example.server.model.Task;
import org.example.server.service.TaskService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Controller
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1. Request-Response: Получение задачи по ID
    @MessageMapping("task/{id}")
    public Mono<Task> getTask(@DestinationVariable("id") Long id) {
        return taskService.getTaskById(id);
    }

    // 2. Request-Stream: Получение всех задач в потоке
    @MessageMapping("task/all")
    public Flux<Task> getAllTasks() {
        return taskService.getAllTasks();
    }


    // 3. Fire-and-Forget: Создание новой задачи (не возвращает ответ)
    @MessageMapping("task/new")
    public Mono<Void> setAlert(Mono<Task> taskMono) {
        System.out.println("creating task");
        return taskMono.doOnNext(task -> {
            taskService.createTask(task.getDescription());
        }).thenEmpty(Mono.empty());

//        taskService.createTask(taskMono.)
//        return alertMono
//                .doOnNext(event ->
//                        log.info("Event Id '{}' occurred of type '{}' at '{}'",
//                                event.getId(),
//                                event.getType(),
//                                event.getTimestamp())
//                )
//                .thenEmpty(Mono.empty());
    }


    // 4. Channel: Двусторонний обмен данными. Получаем поток описаний задач и создаем их.
    @MessageMapping("task/batchcreate")
    public Flux<Task> batchCreate(Flux<Task> tasks) {
        return tasks
                .flatMap(task -> taskService.createTask(task.getDescription()));
    }


}