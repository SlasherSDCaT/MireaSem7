package muhametshin_p1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class task_1 {

    public static List<Integer> arrayGenerator() {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 10000; i++)
            list.add(random.nextInt());

        return list;
    }

    public static int findMaxInList(List<Integer> list) throws InterruptedException {
        if (list == null || list.isEmpty()) throw new IllegalArgumentException("List is empty or null");

        int maxNum = list.get(0);
        for (int number: list) {
            Thread.sleep(1);
            if (number > maxNum) maxNum = number;
        }
        return maxNum;
    }

    public static int findMaxInListWithThreads(List<Integer> list) throws InterruptedException, ExecutionException {
        if (list == null || list.isEmpty()) throw new IllegalArgumentException("List is empty or null");
        // хотим создать столько потоков, сколько есть логических ядер у процессора
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        // делим нашу большую задачу на равные задачи поменьше
        int batchSize = list.size() / threads;
        List<Callable<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int start = i * batchSize;
            int end = (i == threads - 1) ? list.size() : (i + 1) * batchSize;

            tasks.add(() -> findMaxInList(list.subList(start, end)));
        }

        // запустили все задачи параллельно
        List<Future<Integer>> futures = executorService.invokeAll(tasks);

        int maxNum = Integer.MIN_VALUE;
        for (Future<Integer> future : futures) {
            int subListMax = future.get();
            Thread.sleep(1);
            if (subListMax > maxNum) maxNum = subListMax;
        }
        executorService.shutdown();
        return maxNum;
    }

    static class MaxFinderTask extends RecursiveTask<Integer> {
        private List<Integer> list;
        private int start;
        private int end;
        MaxFinderTask(List<Integer> list, int start, int end) {
            this.list = list;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            // нас устраивает скорость обработки 1000 элементов линейно
            if (end - start <= 1000) {
                try {
                    return findMaxInList(list.subList(start, end));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            int middle = start + (end - start) / 2;
            MaxFinderTask leftTask = new MaxFinderTask(list, start, middle);
            MaxFinderTask rightTask = new MaxFinderTask(list, middle, end);

            leftTask.fork(); // левую половину считаем параллельно

            int rightResult = rightTask.compute(); // правую считаем прямо тут
            int leftResult = leftTask.join();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return Math.max(leftResult, rightResult);
        }
    }
    public static int findMaxWithForkJoin(List<Integer> list) {
        if (list == null || list.isEmpty()) throw new IllegalArgumentException("List is empty or null");

        try (ForkJoinPool pool = new ForkJoinPool()) { // хотим автоматически вызвать close()
            MaxFinderTask task = new MaxFinderTask(list, 0, list.size());
            return pool.invoke(task);
        }
    }

    private static void resourseUsageReport(String method, long startTime, long endTime) {
        long elapsedTime = endTime - startTime;
        System.out.println("Метод " + method + ":");
        System.out.println("Время выполнения: " + elapsedTime + " мс");
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Использование памяти: " + memoryUsed / (1024 * 1024) + " МБ");
        System.out.println();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        List<Integer> testList = arrayGenerator();

        long startTime = System.nanoTime();
        int result = findMaxInList(testList);
        long endTime = System.nanoTime();
        resourseUsageReport("Последовательная функция", startTime, endTime);

        startTime = System.nanoTime();
        result = findMaxInListWithThreads(testList);
        endTime = System.nanoTime();
        resourseUsageReport("Using Threads", startTime, endTime);

        startTime = System.nanoTime();
        result = findMaxWithForkJoin(testList);
        endTime = System.nanoTime();
        resourseUsageReport("Using Fork", startTime, endTime);

        System.out.println("Результат: " + result);
    }
}
