package TransmogrifierJava;

import java.io.FileWriter;
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

    public static void getSinkFunction(Map<String, String> sink_params, String data) {
        System.out.println("in getsinkfunction");
        String file_path = (String) sink_params.get("path");

        // write to a file
        try (FileWriter fileWriter = new FileWriter(file_path)) {
            // Create a JSONArray object from String data
            org.json.JSONArray data_jsonArray = new org.json.JSONArray(data);
            
            // Add indent of 2 to the JSON representation when writing to file
            fileWriter.write(data_jsonArray.toString(4));
            System.out.println("JSON data has been written to the file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing JSON data to the file.");
            e.printStackTrace();
        }
    }
}