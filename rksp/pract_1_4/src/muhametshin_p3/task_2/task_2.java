package muhametshin_p3.task_2;

import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class task_2 {
    public static List<Integer> getRandomArray(int n) {
        if (n == 0) n = new Random().nextInt(1001);
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < n; i++)
            numbers.add(new Random().nextInt(1001));
        return numbers;
    }
    public static void main(String[] args) {
        System.out.println("TASK 2.1.1 STARTS");
        Observable<Long> source = Observable.fromCallable(
                () -> {
                    return getRandomArray(0);
                }).flatMapIterable(numbers -> numbers)
                .count().toObservable();

        LongSubscriber longObserver = new LongSubscriber();
        source.subscribe(longObserver);
        System.out.println("TASK 2.1.1 FINISHED\n");

        System.out.println("TASK 2.2.1 STARTS");
        Observable<Integer> stream1 = Observable.fromCallable(
                () -> {
                    return getRandomArray(10);
                }).flatMapIterable(numbers -> numbers);

        Observable<Integer> stream2 = Observable.fromCallable(
                () -> {
                    return getRandomArray(10);
                }).flatMapIterable(numbers -> numbers);
        Observable<Integer> mergedStream = Observable.zip(stream1, stream2, Observable::just).flatMap(observable -> observable);

        IntegerSubscriber intObserver = new IntegerSubscriber();
        mergedStream.subscribe(intObserver);
        System.out.println("TASK 2.2.1 FINISHED\n");

        System.out.println("TASK 2.3.1 STARTS");
        Observable<Integer> stream = Observable.fromCallable(
                () -> {
                    return getRandomArray(0);
                }).flatMapIterable(number -> number);

        Observable<Integer> result = stream.lastElement().toObservable();
        result.subscribe(intObserver);
        System.out.println("TASK 2.3.1 FINISHED\n");




    }
}
