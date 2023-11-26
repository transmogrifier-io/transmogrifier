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

public class Transmogrifier {
  private List<JSONObject> manifest;
    private List<SchemaEntry> schemaEntries;

    public Transmogrifier(List<JSONObject> manifest) {
        this.manifest = manifest;
        // all the entries in the manifest, ie schema, filters, entries, sinks
        this.schemaEntries = getSchemaEntries();
    }

    private List<SchemaEntry> getSchemaEntries() {
        List<SchemaEntry> schemaList = new ArrayList<>();
        for (JSONObject schema : this.manifest) {
            // System.out.println(schema);
            String schemaa = (String) schema.get("schema");
            List<JSONObject> entries = (List<JSONObject>) schema.get("entries");
            List<JSONObject> filters = (List<JSONObject>) schema.get("filters");
            List<JSONObject> sinks = (List<JSONObject>) schema.get("sinks");
            SchemaEntry schemaEntry = new SchemaEntry(schemaa, entries, filters, sinks);
            schemaList.add(schemaEntry);
        }
        for (SchemaEntry schemaEntry : schemaList) {
            System.out.println(schemaEntry.schema);
            System.out.println(schemaEntry.entries);
            System.out.println(schemaEntry.filters);
            System.out.println(schemaEntry.sinks);
            System.out.println();
        }
        return schemaList;
    }

    public List<?> transmogrify() {
        List<?> schemaEntryDatas = new ArrayList<>();
        for (SchemaEntry schemaEntry : this.schemaEntries) {
            // completable future thing - original: let entryData = await
            // transmogrifyEntry(entry, schemaEntry.schema);
            schemaEntry.transmogrifyEntry();
            schemaEntryDatas.add(schemaEntry.runPipelineSchemaEntry());
        }
        return schemaEntryDatas;
    }

    public void transmogrifySchemaEntry() {

    }

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("manifest_example_nofilters.json")) {
            Object obj = parser.parse(reader);
            List<JSONObject> jsonObject = (List<JSONObject>) obj; // list of json object
            // System.out.println(jsonObject.get(0).get("schema"));
            Transmogrifier transmogrifier = new Transmogrifier(jsonObject);
            transmogrifier.transmogrify();
            // transmogrifier.getSchemaEntries();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
