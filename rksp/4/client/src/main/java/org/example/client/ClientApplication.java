package org.example.client;

import org.example.client.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Autowired
	RSocketRequester.Builder requesterBuilder;

	@Override
	public void run(String... args) throws Exception {
		RSocketRequester rsocketRequester = requesterBuilder.tcp("localhost", 7000);


		//Fire-and-Forget: Создание новой задачи (не возвращает ответ)
		System.out.println("Fire-and-Forget: Создание новой задачи (не возвращает ответ)");
		rsocketRequester
				.route("task/new")
				.data(new Task(null, "created", LocalDateTime.now(), false))
				.send()
				.subscribe();

		//request-response
		System.out.println("request-response - get 1 task by id");
		rsocketRequester
				.route("task/{id}", 1)
				.retrieveMono(Task.class)
				.subscribe(System.out::println);

		//request-stream
		System.out.println("request-response - get all tasks");
		rsocketRequester
				.route("task/all")
				.retrieveFlux(Task.class)
				.doOnNext(System.out::println)
				.subscribe();



		System.out.println("request-response - get all tasks");
		rsocketRequester
				.route("task/all")
				.retrieveFlux(Task.class)
				.doOnNext(System.out::println)
				.subscribe();
		//Channel: Двусторонний обмен данными. Получаем поток описаний задач и создаем их.
		System.out.println("Channel: Двусторонний обмен данными. Получаем поток описаний задач и создаем их.");
		Flux<Task> taskFlux =
				Flux.fromArray(new Task[]{
						new Task(null, "t1", LocalDateTime.now(), false),
						new Task(null, "t2", LocalDateTime.now(), false),
						new Task(null, "t3", LocalDateTime.now(), false),
						new Task(null, "t4", LocalDateTime.now(), false)
						})
						.delayElements(Duration.ofSeconds(2));
		rsocketRequester
				.route("task/batchcreate")
				.data(taskFlux)
				.retrieveFlux(Task.class)
				.subscribe(System.out::println);

	}

}
