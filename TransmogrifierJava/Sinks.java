package TransmogrifierJava;

import TransmogrifierJava.Writer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class Sinks {
    public static String null_sink(Map<String, Object> params, String data) {
        return null;
    }

    public static CompletableFuture<Void> fileWrite(Map<String, Object> params, String data) throws IOException {
        String path = (String) params.get("path");
        return Writer.writeFile(params, path);
    }

    // public static CompletableFuture<Void> urlWrite(String data, Map<String, Object> params) throws IOException {
    //     String path = (String) params.get("path");
    //     return Writer.writeUrl(params, path);
    // }

    // public static Map<String, CompletableFuture<String>> getSources() {
    // Map<String, CompletableFuture<String>> sources = new HashMap<>();

    // // Usage example with empty params
    // Map<String, Object> emptyParams = new HashMap<>();

    // sources.put("file_read", CompletableFuture.supplyAsync(() -> {
    // try {
    // return fileRead(emptyParams).get(); // Use get() to retrieve the result
    // } catch (IOException | InterruptedException | ExecutionException e) {
    // throw new RuntimeException(e);
    // }
    // }));

    // sources.put("url_read", CompletableFuture.supplyAsync(() -> {
    // try {
    // return urlRead(emptyParams).get(); // Use get() to retrieve the result
    // } catch (IOException | InterruptedException | ExecutionException e) {
    // throw new RuntimeException(e);
    // }
    // }));

    // return sources;
    // }
}