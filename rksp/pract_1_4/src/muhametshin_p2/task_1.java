package muhametshin_p2;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class task_1 {
    private static void writeLinesToFile(String fileName, List<String> lines) {
        Path filePath = Paths.get(fileName);
        try {
            Files.write(filePath, lines);
            System.out.println("Файл успешно создан: " + fileName);
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static List<String> readFileContent(String fileName) {
        Path filePath = Paths.get(fileName);
        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return null;
    }


    public static void main(String[] args) {
        String fileName = "test.txt";
        List<String> fileLines = new ArrayList<>();
        Random random = new Random();
        int iterations = random.nextInt(5) + 5;
        for (int i = 0; i < iterations; i++) {
            fileLines.add(RandomStringUtils.randomAlphabetic(random.nextInt(20) + 10));
        }

        writeLinesToFile(fileName, fileLines);

        List<String> fileContent = readFileContent(fileName);

        assert fileContent != null;
        for (String line : fileContent) {
            System.out.println(line);
        }
    }
}
