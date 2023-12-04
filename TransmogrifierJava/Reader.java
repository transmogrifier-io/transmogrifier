package TransmogrifierJava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Reader {
    /**
     * Reads the url or file
     */
    public static CompletableFuture<String> readUrlOrFile(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (path.startsWith("http://") || path.startsWith("https://")) {

                    return readURL(path).get();
                } else {
                    return readFile(path).get();
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Reads the url
     * @param url
     * @return
     * @throws IOException
     */
    public static CompletableFuture<String> readURL(String url) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder rawData = new StringBuilder();

            try {
                URL urlObj = new URL(url);
                connection = (HttpURLConnection) urlObj.openConnection();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException(
                            "Failed to read URL \"" + url + "\": HTTP status code " + connection.getResponseCode());
                }

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    rawData.append(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return rawData.toString();
        });
    }

    /**
     * Reads the url without the last line
     * This was used for reading the url javascript file, and there were issues returning the js with return lib, so we removed the last line
     * @param urlString
     * @return
     * @throws IOException
     */
    public static CompletableFuture<String> readURLNoLastLine(String urlString) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder contentBuilder = new StringBuilder();
            try {

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                String previousLine = null;

                while ((line = reader.readLine()) != null) {
                    if (previousLine != null) {
                        contentBuilder.append(previousLine).append("\n");
                    }
                    previousLine = line;
                }

                reader.close();
                connection.disconnect();
            
                // Display or use the content without the last line as needed
            } catch (Exception e) {
                e.printStackTrace();
            }
            return contentBuilder.toString();

        });
    }

    /**
     * Reads the file
     * @param filePath
     * @return
     */
    public static CompletableFuture<String> readFile(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return readLocalFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Reads the local file
     * @param filePath
     * @return
     * @throws IOException
     */
    private static String readLocalFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // Assuming UTF-8 encoding, adjust as needed
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes, "UTF-8");
    }
}
