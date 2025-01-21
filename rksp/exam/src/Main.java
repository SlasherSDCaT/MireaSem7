import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Main {

    // Пример работы с буфером
    public static void bufferExample() {
        // Создание буфера
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // Запись данных в буфер
        String text = "Hello, NIO!";
        buffer.put(text.getBytes());

        // Подготовка буфера к чтению
        buffer.flip();

        // Чтение данных из буфера
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println(new String(bytes));

        // Очистка буфера
        buffer.clear();
    }

    // Пример работы с файловым каналом
    public static void fileChannelExample() throws IOException {
        Path path = Path.of("test.txt");

        // Запись в файл через канал
        try (FileChannel writeChannel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)) {

            String text = "Testing FileChannel";
            ByteBuffer buffer = ByteBuffer.wrap(text.getBytes());
            writeChannel.write(buffer);
        }

        // Чтение из файла через канал
        try (FileChannel readChannel = FileChannel.open(path,
                StandardOpenOption.READ)) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            readChannel.read(buffer);
            buffer.flip();

            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            System.out.println(new String(bytes));
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== Buffer Example ===");
        bufferExample();

        System.out.println("\n=== FileChannel Example ===");
        fileChannelExample();
    }
}