package transmogrifier2;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.crypto.Data;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Transmogrifier2 {

  private List<JSONObject> manifest;
  private List<SchemaEntry> schemaEntries;
  
    public Transmogrifier2(List<JSONObject> manifest) {
        this.manifest = manifest;
        // all the entries in the manifest, ie schema, filters, entries, sinks
        this.schemaEntries = getSchemaEntries();
    }

    public void transmogrify() {
      for (SchemaEntry schemaEntry: this.schemaEntries) {
        schemaEntry.transmogrifyEntry();
      }
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
        for(SchemaEntry schemaEntry : schemaList){
          System.out.println(schemaEntry.schema);
          System.out.println(schemaEntry.entries);
          System.out.println(schemaEntry.filters);
          System.out.println(schemaEntry.sinks);
        }
        return schemaList;
    }

    
    public List<SchemaEntryData> transmogrify() {
      List<SchemaEntryData> schemaEntryDatas = new ArrayList<>();
      for (SchemaEntry schemaEntry : this.schemaEntries) {
          schemaEntry.transmogrifyEntry();
          schemaEntryDatas.add(schemaEntry.runPipelineSchemaEntry());
      }
      return schemaEntryDatas;
    }

    // public void transmogrifyEntry() {


    // }

    public void transmogrifySchemaEntry() {

    }
    
    ///////////////////////////////////
    private class SchemaEntry {
      private String schema;
      private List<JSONObject> entries;
      private List<JSONObject> filters;
      private List<JSONObject> sinks;
      
      public SchemaEntry(String schema, List<JSONObject> entries, List<JSONObject> filters, List<JSONObject> sinks) {
        this.schema = schema;
        this.entries = entries;
        this.filters = filters;
        this.sinks = sinks;
      }

      // gets the schema and source functions, and runs the pipeline entry on the data
      public List<Entry> transmogrifyEntry() {
          List<Entry> entryData = new ArrayList<>();
          for (JSONObject entry : this.entries) {
            JSONObject source = (JSONObject)entry.get("source");
            List<JSONObject> filters = (List<JSONObject>)entry.get("filters");
            List<JSONObject> sinks = (List<JSONObject>)entry.get("sinks");
            Entry entryy = new Entry(source, filters, sinks);
            entryData.add(entryy.runPipelineEntry(this.schema));
          }
          // this.transmogrifiedEntries = entryData;
          return entryData;
      }

      // returns the filtered data, and sends it to the sink
      // public Data runPipelineSchemaEntry() {
      //     Data data = null;
      //     for (Filter filter : this.filters) {
      //         FilterFunction filterFunc = HelperFunctions.getFilterFunction(filter.getFunc());
      //         FilterParameters filterParams = HelperFunctions.getFilterParameters(filter.getParams());
      //         filterParams.setSchema(this.schema);
      //         System.out.println(filterFunc);
      //         data = filterFunc.apply(this.transmogrifiedEntries, filterParams);
      //     }

      //     for (Sink sink : this.sinks) {
      //         SinkFunction sinkFunc = HelperFunctions.getSinkFunction(sink.getFunc());
      //         SinkParameters sinkParams = sink.getParams();
      //         if (sinkParams == null) {
      //             sinkParams = new SinkParameters();
      //         }
      //         sinkFunc.apply(sinkParams, data);
      //     }
      //     return data;
      // }
      
    }

    public class Entry {

      // path to transmogrify - which open data we will standardize (calgary public art/van public art)
      private JSONObject source;
      // the url to the source 
      private JSONObject sourceParams;
      // filter applied to the source
      private List<JSONObject> filters;
      // where to store transmogrified data
      private List<JSONObject> sinks;
      private JSONObject schema;  
      
      public Entry(JSONObject source, List<JSONObject> filters, List<JSONObject> sinks){
        this.source = source;
        this.filters = filters;
        this.sinks = sinks;
      }

      public Data runPipelineEntry(String schema){

        
        return null;
      }
    }

    public class HelperFunctions {
      public static void getSourceFunction(name) {
        
      }

      public static CompletableFuture<SourceFunction> getSourceFunction(String name) {
        CompletableFuture<SourceFunction> futureSource = CompletableFuture.supplyAsync(() -> {
            try {
                Source source;
                if (name.startsWith("http://") || name.startsWith("https://")) {
                    String sourceContent = Reader.readURL(name);
                    SourceFunction sourceFunction = createFunctionFromSourceContent(sourceContent);
                    return sourceFunction;
                } else {
                    SourceFunction sourceFunction = sources.get(name);
                    return sourceFunction;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return futureSource;
      }

      private static SourceFunction createFunctionFromSourceContent(String sourceContent) {
          try {
              ScriptEngineManager manager = new ScriptEngineManager();
              ScriptEngine engine = manager.getEngineByName("js");
              engine.eval(sourceContent);
              return (SourceFunction) engine.eval("new Function()");
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
      } 
    }
  

  public static void main(String[] args) {
    JSONParser parser = new JSONParser();
    try (FileReader reader = new FileReader("manifest_example.json")) {
      Object obj = parser.parse(reader);
      List<JSONObject> jsonObject = (List<JSONObject>) obj; //list of json object
      // System.out.println(jsonObject.get(0).get("schema"));
      Transmogrifier2 transmogrifier = new Transmogrifier2(jsonObject);
      transmogrifier.transmogrify();
      // transmogrifier.getSchemaEntries();

    } catch (IOException | ParseException e) {}



      // List<JSONObject> manifest = new ArrayList<JSONObject>();
      // JSONParser parser = new JSONParser();
      
      // // manifest-schema.json file reader
      // try (FileReader reader = new FileReader("manifest_example.json")) {
      //   Object obj = parser.parse(reader);
      //   List<JSONObject> jsonObject = (List<JSONObject>) obj;
      //   CompletableFuture<List<JSONObject>> future = transmogrify(jsonObject);
      //   future.thenAccept(result -> {
      //     System.out.println(result);
      //   });
  
      //   // future.join();
      //   System.out.println("MAIN FUNCTION");
  
      // } catch (IOException | ParseException e) {
      //   // TODO Auto-generated catch block
      //   e.printStackTrace();
      // }
    }
}


