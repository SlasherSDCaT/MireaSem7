package muhametshin_p2;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class task_2 {

    public static void createFile(String name, int sizeInMB) throws IOException {
        byte [] data = new byte[1024 * 1024];
        try (FileOutputStream fos = new FileOutputStream(name)) {
            for (int i = 0; i < sizeInMB; i++) {
                fos.write(data);
            }
        }
    }

    public static void copyFileWithStreams(String source, String dest) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fis.close();
        fos.close();
    }

    public static void copyFileWithChannels(String source, String dest) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);
        FileChannel sourceChannel = fis.getChannel();
        FileChannel destinationChannel = fos.getChannel();

        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);

        sourceChannel.close();
        destinationChannel.close();
        fis.close();
        fos.close();
    }

    public static void copyFileWithApacheCommonsIO(String source, String dest) throws IOException {
        File sourceFile = new File(source);
        File destFile = new File(dest);
        FileUtils.copyFile(sourceFile, destFile);

    }

    private static void copyFileWithFiles(String source, String dest) throws IOException {
        Path sourcePath = Path.of(source);
        Path destinationPath = Path.of(dest);
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void resourceUsageReport(String method, long startTime, long endTime) {
        long elapsedTime = endTime - startTime;
        System.out.println("Метод " + method + ":");
        System.out.println("Время выполнения: " + elapsedTime + " мс");
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Использование памяти: " + memoryUsed / (1024) + " KБ");
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        String sourceFileName = "source.txt";
        String destinationFileName = "destination.txt";

        createFile(sourceFileName, 100);

        // Тестирование разными методами
        long startTime1 = System.currentTimeMillis();
        copyFileWithStreams(sourceFileName, destinationFileName);
        long endTime1 = System.currentTimeMillis();
        resourceUsageReport("FileInputStream/FileOutputStream", startTime1, endTime1);

        long startTime2 = System.currentTimeMillis();
        copyFileWithChannels(sourceFileName, destinationFileName);
        long endTime2 = System.currentTimeMillis();
        resourceUsageReport("FileChannel", startTime2, endTime2);

        long startTime3 = System.currentTimeMillis();
        copyFileWithApacheCommonsIO(sourceFileName, destinationFileName);
        long endTime3 = System.currentTimeMillis();
        resourceUsageReport("Apache Commons IO", startTime3, endTime3);

        long startTime4 = System.currentTimeMillis();
        copyFileWithFiles(sourceFileName, destinationFileName);
        long endTime4 = System.currentTimeMillis();
        resourceUsageReport("Files class", startTime4, endTime4);


    }
}
