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
