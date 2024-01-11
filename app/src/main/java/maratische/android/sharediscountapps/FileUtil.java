package maratische.android.sharediscountapps;

import android.content.Context;

import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {
    public static byte[] readFileToBytes(Context context, String fileName) {
        FileInputStream fileInputStream = null;

        try {
            // Открытие FileInputStream для чтения файла
            fileInputStream = context.openFileInput(fileName);

            // Определение размера файла
            int fileSize = fileInputStream.available();

            // Создание массива байт для хранения содержимого файла
            byte[] fileBytes = new byte[fileSize];

            // Чтение содержимого файла в массив байт
            fileInputStream.read(fileBytes);

            return fileBytes;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Закрытие FileInputStream после чтения файла
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null; // В случае ошибки возвращаем null
    }

}
