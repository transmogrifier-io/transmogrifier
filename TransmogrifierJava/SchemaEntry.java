package TransmogrifierJava;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


// to create a json object
import org.json.simple.JSONObject;
import java.util.Map;

public class SchemaEntry {

    private String schema;
    private List<JSONObject> entries;
    private List<JSONObject> filters;
    private List<JSONObject> sinks;
    private List<String> transmogrifiedEntries;

    public SchemaEntry(String schema, List<JSONObject> entries, List<JSONObject> filters, List<JSONObject> sinks) {
        this.schema = schema; // this is the extracted text from the schema url
        this.entries = entries;
        this.filters = filters;
        this.sinks = sinks;
        this.transmogrifiedEntries = new ArrayList<>();
    }

    // gets the schema and source functions, and runs the pipeline entry on the data
    public List<String> transmogrifyEntry() {
        List<String> entryData = new ArrayList<>();
        for (JSONObject entry : this.entries) {
            JSONObject source = (JSONObject) entry.get("source");
            List<JSONObject> filters = (List<JSONObject>) entry.get("filters");
            List<JSONObject> sinks = (List<JSONObject>) entry.get("sinks");
            Entry entryy = new Entry(source, filters, sinks);
            // maybe use completeable future here
            String data = entryy.runPipelineEntry(this.schema);
            entryData.add(data);
           
        }
        this.transmogrifiedEntries = entryData;
        return entryData;
    }


    /**
     * Runs the pipeline for each SchemaEntry in the Schema
     * @param <T>
     * @param data
     * @return
     */
    public List<String> runPipelineSchemaEntry() {
        // data is the transmogrified data from schemaEntry.transmogrifyEntry()
        List<String> data = this.transmogrifiedEntries;
        try {
            // this is the text from the schema url
            this.schema = Reader.readURL(this.schema).get();
        } catch (InterruptedException | ExecutionException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /**
         * This needs to be finished up, applying filters does not work on javascript filters
         */
        // dont want to overwrite the whole data, so will just overwrite data_element which is just one city's public art 
        for (String data_element: data) {
            // iterate through all the filters in the entries > filters
            for (JSONObject filter : this.filters) {
                String func_filter = (String) filter.get("func");

                if(filter.get("params") != null){
                    //"params": {"validator": "json"} --> params = {"validator": "json"}
                    Map<String, String> params = (Map<String, String>) filter.get("params");
                    // merged getfilterparameters into  getfilterfunction
                    data_element = HelperFunctions.applyFilterFunctionEntry(func_filter, data_element, params);
                }
            }

            for (JSONObject sink: this.sinks) {
                Sinks.getSinkFunction((Map<String, String>) sink.get("params"), data_element);
            }
        }

        return data;
    }
}
