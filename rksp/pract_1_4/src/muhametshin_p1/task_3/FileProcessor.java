package muhametshin_p1.task_3;

import java.util.concurrent.BlockingQueue;

class FileProcessor implements Runnable {
    private BlockingQueue<File> queue;
    private String allowedFileType;
    public FileProcessor(BlockingQueue<File> queue, String allowedFileType) {
        this.queue = queue;
        this.allowedFileType = allowedFileType;
    }
    @Override
    public void run() {
        while (true) {
            try {
                File file = queue.take(); // Получаем файл из очереди
                if (file.getFileType().equals(allowedFileType)) {
                    long processingTime = file.getFileSize() * 7L;
                    Thread.sleep(processingTime);
                    System.out.println("Обработан файл типа " + file.getFileType() + " с размером " + file.getFileSize() + ". Время обработки: " + processingTime + " мс.");
                } else {
                    queue.put(file); // положили его обратно
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
