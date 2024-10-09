package muhametshin_p3.task_4;

import io.reactivex.Observable;

public class FileQueue {
    private final int capacity;
    private final Observable<File> fileObservable;

    public FileQueue(int capacity) {
        this.capacity = capacity;
        this.fileObservable = new FileGenerator().generateFile()
                .replay(capacity) // Буферизирует источник файлов с ограниченной емкостью
                .autoConnect(); // Подключается автоматически к буферизированному источнику
    }

    public Observable<File> getFileObservable() {
        return fileObservable;
    }

}
