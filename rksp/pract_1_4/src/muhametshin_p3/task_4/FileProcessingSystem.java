package muhametshin_p3.task_4;

public class FileProcessingSystem {
    public static void main(String[] args) {
        int queueCapacity = 5;
        FileQueue fileQueue = new FileQueue(queueCapacity);
        String[] supportedFileTypes = {"XML", "JSON", "XLS"};
        for (String fileType : supportedFileTypes) {
            new FileProcessor(fileType)
                    .processFiles(fileQueue.getFileObservable())
                    .subscribe(
                            () -> {
                            }, // Обработка успешного завершения
                            throwable -> System.err.println("Error processing file: " + throwable)
                    );
        }
        try {
            Thread.sleep(10000); // Пусть система работает 10 секунд
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
