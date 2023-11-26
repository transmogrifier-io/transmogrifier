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

public class Filters {
    public static CompletableFuture<String> null_filter(String data) {
        return CompletableFuture.supplyAsync(() -> {
            return data;
        });
    }

    public static CompletableFuture<String> to_upper(String data) {
        return CompletableFuture.supplyAsync(() -> {
            return data.toUpperCase();
        });
    }

    public static CompletableFuture<String> to_lower(String data) {
        return CompletableFuture.supplyAsync(() -> {
            return data.toLowerCase();
        });
    }

    public static CompletableFuture<String> getFilters(String filter, String data) {
        Map<String, CompletableFuture<String>> filters = new HashMap<>();
        // String data_string = data.toString();
        filters.put("null_filter", null_filter(data));

        filters.put("to_upper", to_upper(data));

        filters.put("to_lower", to_lower(data));
        if (filters.isEmpty()) {
            System.out.println("filter not found");
            return CompletableFuture.supplyAsync(() -> {
                return data;
            });
        }else{
            return filters.get(filter);
        }
    }
}