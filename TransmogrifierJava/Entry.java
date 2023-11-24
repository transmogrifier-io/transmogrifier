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

public class Entry {
    // path to transmogrify - which open data we will standardize (calgary public
    // art/van public art)
    public JSONObject source;
    // the url to the source
    public JSONObject sourceParams;
    // filter applied to the source
    public List<JSONObject> filters;
    // where to store transmogrified data
    public List<JSONObject> sinks;
    // public String schema;

    public Entry(JSONObject source, List<JSONObject> filters, List<JSONObject> sinks) {
        this.source = source;
        this.filters = filters;
        this.sinks = sinks;
    }

    public Entry() {

    }

    @FunctionalInterface
    interface MyFunction {
        void perform();
    }

    // async
    public CompletableFuture<String> runPipelineEntry(String schema) {
        System.out.println("in runPipelineEntry function");
        // the processed data for entry
        CompletableFuture<String> data = HelperFunctions.getSourceFunction(this.source);

        for (JSONObject filter : this.filters) {
            // data = the original JSON data from the each city (https://opendata.vancouver.ca/api/explore/v2.1/catalog/datasets/public-art/exports/json?lang=en&timezone=America%2FLos_Angeles)
            String func_filter = (String) filter.get("func"); // filter fun url or "null_filter", etc
            Map<String, String> params = (Map<String, String>) filter.get("params"); // params: {indent:
            data = (CompletableFuture<String>) HelperFunctions.getFilterFunctionEntry(func_filter, data, params);
        }

        // START DEBUGGING HERE!!!
        for (JSONObject sink: this.sinks) {
            try {
              HelperFunctions.getSinkFunction((Map<String, String>) sink.get("params"), data.get());
            } catch (InterruptedException | ExecutionException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
        }
        // Entry entry = new Entry();

        return data;
    }
}
