package muhametshin_p2;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class task_3 {
    public static short calculateChecksum(String filePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        FileChannel fileChannel = fileInputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(2);
        short checksum = 0;

        while (fileChannel.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                checksum ^= buffer.get();
            }
            buffer.clear();
        }

        fileChannel.close();
        fileInputStream.close();

        return checksum;
    }

    public static void main(String[] args) {
        String filePath = "checksum.txt";
        try {
            short checksum = calculateChecksum(filePath);
            // %[аргумент_индекс][флаги][ширина][.точность]спецификатор типа
            // если перед шириной стоит 0, то число будет дополняться нулями
            // выводим в 16-ричной
            System.out.printf("Контрольная сумма файла %s: 0x%010x%n", filePath, checksum);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
