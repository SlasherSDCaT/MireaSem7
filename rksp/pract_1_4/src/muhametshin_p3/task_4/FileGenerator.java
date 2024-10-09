package muhametshin_p3.task_4;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;

public class FileGenerator {
    private static final AtomicInteger counter = new AtomicInteger(0);
    // Генерирует файлы асинхронно с задержкой
    public Observable<File> generateFile() {
        return Observable
                .fromCallable(() -> {
                    try {
                        String[] fileTypes = {"XML", "JSON", "XLS"};
                        String fileType = fileTypes[(int) (Math.random() * 3)];
                        int fileSize = (int) (Math.random() * 81) + 10;
                        Thread.sleep((long) (Math.random() * 801) + 100);
                        return new File(fileType, fileSize);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .repeat() // Повторяем бесконечно
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }
}
