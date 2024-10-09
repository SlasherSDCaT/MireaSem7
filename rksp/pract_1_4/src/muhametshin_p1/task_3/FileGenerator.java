package muhametshin_p1.task_3;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

class FileGenerator implements Runnable {
    private BlockingQueue<File> queue;

    public FileGenerator(BlockingQueue<File> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        Random random = new Random();
        String[] fileTypes = {"XML", "JSON", "XLS"};
        while (true) {
            try {
                Thread.sleep(random.nextInt(808) + 100);

                String randomFileType = fileTypes[random.nextInt(fileTypes.length)];
                int randomFileSize = random.nextInt(808) + 10;
                File file = new File(randomFileType, randomFileSize);
                queue.put(file); // Добавляем файл в очередь
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}



