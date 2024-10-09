package muhametshin_p2;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class task_4 {
    private final WatchService watchService;
    private final Path pathToWatch;
    private final Map<Path, List<String>> fileContents = new HashMap<>(); // Для хранения содержимого файлов

    public task_4(Path pathToWatch) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.pathToWatch = pathToWatch;
        this.pathToWatch.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
    }

    public void processEvents() throws InterruptedException, IOException, NoSuchAlgorithmException {
        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path filename = (Path) event.context();
                Path filePath = pathToWatch.resolve(filename);

                // Игнорирование временных файлов
                if (filename.toString().endsWith("~")) {
                    continue;
                }

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    System.out.println("Файл создан: " + filename);
                    // Сохраняем содержимое нового файла
                    fileContents.put(filePath, readFile(filePath));
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    System.out.println("Файл изменён: " + filename);

                    // Читаем текущее содержимое файла
                    List<String> newContents = readFile(filePath);
                    List<String> oldContents = fileContents.getOrDefault(filePath, new ArrayList<>());
                    if (!newContents.equals(oldContents)) {
                        List<String> addedLines = new ArrayList<>(newContents);
                        addedLines.removeAll(oldContents);
                        List<String> removedLines = new ArrayList<>(oldContents);
                        removedLines.removeAll(newContents);

                        if (!addedLines.isEmpty()) {
                            System.out.println("Добавленные строки: " + addedLines);
                        }
                        if (!removedLines.isEmpty()) {
                            System.out.println("Удалённые строки: " + removedLines);
                        }
                    }
                    // Обновляем содержимое файла
                    fileContents.put(filePath, newContents);
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    System.out.println("Файл удалён: " + filename);
                    // Удалённый файл не может быть прочитан для контрольной суммы
                    if (Files.exists(filePath)) {
                        long size = Files.size(filePath);
                        String checksum = calculateChecksum(filePath);
                        System.out.println("Размер файла: " + size);
                        System.out.println("Контрольная сумма: " + checksum);
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    // Чтение содержимого файла в список строк
    private List<String> readFile(Path filePath) throws IOException {
        if (Files.isReadable(filePath)) {
            return Files.readAllLines(filePath);
        }
        return new ArrayList<>();
    }

    // Расчет контрольной суммы (например, используя SHA-256)
    private String calculateChecksum(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashBytes = digest.digest(fileBytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        Path pathToWatch = Paths.get("./observed"); // Укажите каталог для наблюдения
        task_4 watcher = new task_4(pathToWatch);
        watcher.processEvents();
    }
}