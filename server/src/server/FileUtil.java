package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static File saveClassToFile(Class<?> clazz, String filePath) throws IOException {
        InputStream inputStream = clazz.getResourceAsStream(clazz.getSimpleName() + ".class");
        File file = new File(filePath);
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        
        return file;
    }
}
