package TransmogrifierJava;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
// import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

// script engine for graalvm
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.crypto.Data;
import javax.xml.transform.Source;
// import javax.xml.validation.Schema;

// to create a json object
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.InputStream;

// JSON validator
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;

// import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Reader {

    public static void testFunction(){
        System.out.println("test function");
    }
    public static CompletableFuture<String> readUrlOrFile(String path) {
        System.out.println("in readUrlOrFile function, returns completable future<string>");
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    // System.out.println(readURL(path).get());
                    return readURL(path).get();
                } else {
                    return readFile(path).get();
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<String> readURL(String url) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder rawData = new StringBuilder();

            try {
                URL urlObj = new URL(url);
                connection = (HttpURLConnection) urlObj.openConnection();

                // Adjust connection settings if needed (e.g., timeouts, headers)
                // connection.setConnectTimeout(5000);
                // connection.setReadTimeout(5000);

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

    public static CompletableFuture<String> readFile(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return readLocalFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String readLocalFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // Assuming UTF-8 encoding, adjust as needed
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes, "UTF-8");
    }
}
