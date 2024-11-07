package org.example.server.repositoies;

import org.example.server.model.Task;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface TaskRepository extends R2dbcRepository<Task, Long> {
}
