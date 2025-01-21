package org.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Main {
    public static void main(String[] args) {
        // Примеры с Mono
        System.out.println("=== Примеры работы с Mono ===");

        // Создание Mono из одного значения
        Mono<String> mono = Mono.just("Привет, Reactor!");
        mono.subscribe(
                value -> System.out.println("Получено значение: " + value),
                error -> System.err.println("Ошибка: " + error),
                () -> System.out.println("Mono завершен")
        );

        // Создание пустого Mono
        Mono<String> emptyMono = Mono.empty();
        emptyMono.subscribe(
                value -> System.out.println("Значение: " + value),
                error -> System.err.println("Ошибка: " + error),
                () -> System.out.println("Пустой Mono завершен")
        );

        // Примеры с Flux
        System.out.println("\n=== Примеры работы с Flux ===");

        // Создание Flux из нескольких значений
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5);
        flux.subscribe(
                value -> System.out.println("Получено значение: " + value),
                error -> System.err.println("Ошибка: " + error),
                () -> System.out.println("Flux завершен")
        );

        // Преобразование данных с помощью map
        System.out.println("\n=== Преобразование данных ===");
        Flux<Integer> numbers = Flux.range(1, 4);
        numbers
                .map(n -> n * 2)
                .subscribe(value -> System.out.println("Удвоенное значение: " + value));

        // Фильтрация данных
        System.out.println("\n=== Фильтрация данных ===");
        Flux<Integer> filtered = Flux.range(1, 10)
                .filter(n -> n % 2 == 0);
        filtered.subscribe(
                value -> System.out.println("Четное число: " + value)
        );

        // Обработка ошибок
        System.out.println("\n=== Обработка ошибок ===");
        Flux<Integer> errorFlux = Flux.range(1, 4)
                .map(i -> {
                    if (i == 3) throw new RuntimeException("Ошибка на числе " + i);
                    return i;
                })
                .onErrorReturn(-1);

        errorFlux.subscribe(
                value -> System.out.println("Значение с обработкой ошибки: " + value),
                error -> System.err.println("Произошла ошибка: " + error)
        );

        // Объединение потоков
        System.out.println("\n=== Объединение потоков ===");
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("X", "Y", "Z");

        Flux.concat(flux1, flux2)
                .subscribe(value -> System.out.println("Объединенное значение: " + value));
    }
}