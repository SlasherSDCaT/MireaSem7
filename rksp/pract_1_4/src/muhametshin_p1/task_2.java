package muhametshin_p1;

import java.util.Scanner;
import java.util.concurrent.*;

public class task_2 {
    // Метод для вычисления квадрата числа с задержкой
    private static int calculateSquare(int number) {
        int delayInSeconds = ThreadLocalRandom.current().nextInt(1, 6); // случайная задержка от 1 до 5 секунд
        try {
            Thread.sleep(delayInSeconds * 1000L); // задержка в секундах
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return number * number;
    }

    // Асинхронный метод для выполнения calculateSquare с использованием CompletableFuture
    public static CompletableFuture<Integer> calculateSquareAsync(int number) {
        return CompletableFuture.supplyAsync(() -> calculateSquare(number)); // Выполняем вычисление в другом потоке
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5); // Пул из 5 потоков для работы с задачами

        while (true) {
            try {
                System.out.print("Введите число (или 'exit' для выхода): ");
                Scanner scanner = new Scanner(System.in); // Ввод от пользователя
                String userInput = scanner.nextLine();

                if ("exit".equalsIgnoreCase(userInput)) break; // Выход из цикла по команде "exit"

                int number = Integer.parseInt(userInput); // Преобразуем введённое значение в число

                // Используем CompletableFuture для асинхронного вычисления
                calculateSquareAsync(number)
                        .thenAccept(result -> System.out.println("\nРезультат: " + result))
                        .exceptionally(throwable -> {
                            System.err.println("Произошла ошибка: " + throwable.getMessage());
                            return null;
                        });
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат числа. Пожалуйста, введите целое число."); // Сообщение об ошибке
            }
        }

        // Завершаем пул потоков после выхода из цикла
        executorService.shutdown();
    }
}
