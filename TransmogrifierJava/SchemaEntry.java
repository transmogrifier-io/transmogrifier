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

public class SchemaEntry {
    // make all these attributes private later!!
    public String schema;
    public List<JSONObject> entries;
    public List<JSONObject> filters;
    public List<JSONObject> sinks;
    private List<?> transmogrifiedEntries;

    public SchemaEntry(String schema, List<JSONObject> entries, List<JSONObject> filters, List<JSONObject> sinks) {
        this.schema = schema;
        this.entries = entries;
        this.filters = filters;
        this.sinks = sinks;
        this.transmogrifiedEntries = new ArrayList<>();
    }

    // gets the schema and source functions, and runs the pipeline entry on the data
    public List<?> transmogrifyEntry() {
        List<CompletableFuture<String>> entryData = new ArrayList<>();
        for (JSONObject entry : this.entries) {
            JSONObject source = (JSONObject) entry.get("source");
            List<JSONObject> filters = (List<JSONObject>) entry.get("filters");
            List<JSONObject> sinks = (List<JSONObject>) entry.get("sinks");
            Entry entryy = new Entry(source, filters, sinks);
            // maybe use completeable future here
            entryData.add(entryy.runPipelineEntry(this.schema));
        }
        this.transmogrifiedEntries = entryData;
        return entryData;
    }


    // returns the filtered data, and sends it to the sink
    /// TO DO WORK ON THIS NEXT!!!!!!
    public <T> T runPipelineSchemaEntry() {
        List<?> data = this.transmogrifiedEntries;
        try {
            // this is the text from the schema url
            this.schema = Reader.readURL(this.schema).get();
        } catch (InterruptedException | ExecutionException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // validate the JSON with the schema



        for (JSONObject filter : this.filters) {
          // {"func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/ms-ss-rg-collector-update/collectors/collector-json.js"}
            String func_filter = (String) filter.get("func");

            if(filter.get("params") != null){
              Map<String, Object> params = (Map<String, Object>) filter.get("params"); //"validator": "json"
              try {
                // merged getfilterparameters into  getfilterfunction
                  data = (List<?>) HelperFunctions.getFilterFunctionSchemaEntry(data, params).get();
              } catch (InterruptedException | ExecutionException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
            }
        }

        for (JSONObject sink: this.sinks) {
            HelperFunctions.getSinkFunctionSchemaEntry((Map<String, String>) sink.get("params"), data);
        }
        // Entry entry = new Entry();

        return (T) data;

    }
}
