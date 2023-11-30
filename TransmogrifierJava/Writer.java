package TransmogrifierJava;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import java.util.Map;

public class Writer {

    public static CompletableFuture<Void> writeFile(Map<String, Object> params, String data) {
        return CompletableFuture.runAsync(() -> {
            try {
                writeLocalFile(params, data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void writeLocalFile(Map<String, Object> params, String data) throws IOException {
        String filePath = (String) params.get("path");
        boolean append = Boolean.parseBoolean((String) params.get("append"));

        Path path = Paths.get(filePath);

        if (append) {
            Files.write(path, data.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } else {
            Files.write(path, data.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}
