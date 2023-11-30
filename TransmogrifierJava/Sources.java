package TransmogrifierJava;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
// to create a json object
import org.json.simple.JSONObject;
import java.util.Map;

public class Sources {

    /**
     * Reads a file from the local file system
     * @param params
     * @return the string from the file
     * @throws IOException
     */
    public static CompletableFuture<String> fileRead(Map<String, Object> params) throws IOException {
        String path = (String) params.get("path");
        return Reader.readFile(path);
    }

    /**
     * Reads a file from a URL
     * @param params
     * @return returns string from the url
     * @throws IOException
     */
    public static CompletableFuture<String> urlRead(Map<String, Object> params) throws IOException {
        String path = (String) params.get("path");
        return Reader.readURL(path);
    }

    /**
     * Gets the source function from the source JSONObject
     * @param source
     * @return
     */
    public static CompletableFuture<String> getSourceFunction(JSONObject source) {
        String name = (String) source.get("func"); // url_read
        Map<String, Object> params = (Map<String, Object>) source.get("params");

        if ("file_read".equals(name)) { // Correct way to compare strings
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Assuming Sources.fileRead() takes a Map<String, Object> as a parameter
                    return Sources.fileRead(params).get();
                } catch (InterruptedException | ExecutionException | IOException e) {
                    // Handle exceptions accordingly
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        } else if ("url_read".equals(name)) { // Correct way to compare strings
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Assuming Sources.urlRead() takes a Map<String, Object> as a parameter
                    return Sources.urlRead(params).get();
                } catch (InterruptedException | ExecutionException | IOException e) {
                    // Handle exceptions accordingly
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        } else {
            // Handle other cases or throw an exception for an unsupported operation
            throw new IllegalArgumentException("Unsupported operation: " + name);
        }
    }
}