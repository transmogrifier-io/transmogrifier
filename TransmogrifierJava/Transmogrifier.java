package TransmogrifierJava;

import java.io.FileReader;
import java.io.IOException;
// import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
// import javax.xml.validation.Schema;

// to create a json object
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Transmogrifier {
  private List<JSONObject> manifest;
  private List<SchemaEntry> schemaEntries;

    public Transmogrifier(List<JSONObject> manifest) {
        this.manifest = manifest;
        // all the entries in the manifest, ie schema, filters, entries, sinks
        this.schemaEntries = getSchemaEntries();
    }

    /**
     * Creates SchemaEntry objects from manifest
     * @return list of Schema Entries
     */
    private List<SchemaEntry> getSchemaEntries() {
        List<SchemaEntry> schemaList = new ArrayList<>();
        for (JSONObject schema : this.manifest) {
            String schemaa = (String) schema.get("schema");
            List<JSONObject> entries = (List<JSONObject>) schema.get("entries");
            List<JSONObject> filters = (List<JSONObject>) schema.get("filters");
            List<JSONObject> sinks = (List<JSONObject>) schema.get("sinks");
            SchemaEntry schemaEntry = new SchemaEntry(schemaa, entries, filters, sinks);
            schemaList.add(schemaEntry);
        }
        return schemaList;
    }

    /**
     * Transmogrifies and runs the pipeline for each SchemaEntry in the Schema
     * @return List of schemaEntryDatas
     */
    public List<?> transmogrify() {
        List<Object> schemaEntryDatas = new ArrayList<>();
        for (SchemaEntry schemaEntry : this.schemaEntries) {
            //stores the source, filter and sink into the Entry object and transmogrifies it, returns the transmogrified data
            schemaEntry.transmogrifyEntry();
            //pass the transmogrified data from above into the main schema (top part of schema)
            List<String> result = schemaEntry.runPipelineSchemaEntry();
            schemaEntryDatas.add(result);
        }
        return schemaEntryDatas;
    }

    // example main atm
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("manifest_example_to_lower.json")) {
            Object obj = parser.parse(reader);
            List<JSONObject> jsonObject = (List<JSONObject>) obj; // list of json object
            Transmogrifier transmogrifier = new Transmogrifier(jsonObject);
            transmogrifier.transmogrify();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
