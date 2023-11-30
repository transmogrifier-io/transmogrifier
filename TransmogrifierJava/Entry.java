package TransmogrifierJava;

import java.util.List;
import java.util.concurrent.ExecutionException;

// to create a json object
import org.json.simple.JSONObject;
import java.util.Map;

public class Entry {

    // art/van public art)
    private JSONObject source;
    // the url to the source
    private JSONObject sourceParams;
    // filter applied to the source
    private List<JSONObject> filters;
    // where to store transmogrified data
    private List<JSONObject> sinks;
    // public String schema;

    public Entry(JSONObject source, List<JSONObject> filters, List<JSONObject> sinks) {
        this.source = source;
        this.filters = filters;
        this.sinks = sinks;
    }


    /**
     * Runs the pipeline for each SchemaEntry in the Schema
     * @param schema
     * @return transmogrified data as a String
     */
    public String runPipelineEntry(String schema) {
        System.out.println("Entry.java: in runPipelineEntry function");
        // the processed data for entry
        String data = "";
        try {
            data = Sources.getSourceFunction(this.source).get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (JSONObject filter : this.filters) {
            String func_filter = (String) filter.get("func"); // filter fun url or "null_filter", etc
            Map<String, String> params = (Map<String, String>) filter.get("params"); // params: {indent:
  
            data = HelperFunctions.applyFilterFunctionEntry(func_filter, data, params);
        }

        for (JSONObject sink: this.sinks) {
            Sinks.getSinkFunction((Map<String, String>) sink.get("params"), data);
        }

        return data;
    }
}
