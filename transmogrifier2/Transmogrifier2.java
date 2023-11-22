package transmogrifier2;
import java.io.BufferedReader;
import java.io.FileReader;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.crypto.Data;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Transmogrifier2 {

  private List<JSONObject> manifest;
  private List<SchemaEntry> schemaEntries;
  
    public Transmogrifier2(List<JSONObject> manifest) {
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
        for(SchemaEntry schemaEntry : schemaList){
          System.out.println(schemaEntry.schema);
          System.out.println(schemaEntry.entries);
          System.out.println(schemaEntry.filters);
          System.out.println(schemaEntry.sinks);
        }
        return schemaList;
    }

    public List<?> transmogrify() {
      List<?> schemaEntryDatas = new ArrayList<>();
      for (SchemaEntry schemaEntry : this.schemaEntries) {
          // completable future thing - original: let entryData = await transmogrifyEntry(entry, schemaEntry.schema);
          schemaEntry.transmogrifyEntry();
          schemaEntryDatas.add(schemaEntry.runPipelineSchemaEntry());
      }
      return schemaEntryDatas;
    }

    public void transmogrifySchemaEntry() {

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
    }
} //end of Transmogrifier class


class Entry{
      // path to transmogrify - which open data we will standardize (calgary public art/van public art)
      public JSONObject source;
      // the url to the source 
      public JSONObject sourceParams;
      // filter applied to the source
      public List<JSONObject> filters;
      // where to store transmogrified data
      public List<JSONObject> sinks;
      // public String schema;  
      
      public Entry(JSONObject source, List<JSONObject> filters, List<JSONObject> sinks){
        this.source = source;
        this.filters = filters;
        this.sinks = sinks;
      }

      public Entry() {
        
      }

    // "filters": [
    //       {
    //         "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/canada/british-columbia/vancouver/public-art-json-to-json.js",
    //         "params": {
    //           "library": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/libraries/standard.js"
    //         }
    //       },
    //       {
    //         "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/validate.js",
    //         "params": {
    //           "validator": "json"
    //         }
    //       },
    //       {
    //         "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/stringify.js",
    //         "params": {
    //           "indent": true
    //         }
    //       }
    //     ]

    @FunctionalInterface
    interface MyFunction {
        void perform();
    }

      // async
      public Entry runPipelineEntry(String schema){
        // data is the text received from the urlread (validate.js/.json file)
        CompletableFuture<String> data = HelperFunctions.getSourceFunction(this.source);

        for (JSONObject filter : this.filters) {
            String func_filter = (String) filter.get("func"); // filter fun url or "null_filter", etc
            Map<String, Object> params = (Map<String, Object>) filter.get("params"); // params: {indent: true}/{validator: json}

            // get parameters
            MyFunction myfunction = new MyFunction();
            

            // get filter function & apply parameters

    
            // get the filter function and parameters
            // if ("null_filter".equals(func_filter)) {
            //   CompletableFuture<String> the_data = Filters.null_filter(data);
            //     // data = data.thenApply(Transmogrifier2.Filters::null_filter);
            // } else if ("to_upper".equals(func_filter)) {
            //     data = data.thenApply(Transmogrifier2.Filters::to_upper);
            // } else if ("to_lower".equals(func_filter)) {
            //     data = data.thenApply(Transmogrifier2.Filters::to_lower);
            // } else {
            //     throw new IllegalArgumentException("Unsupported operation: " + name);
            // }
        }
        // Entry entry = new Entry();
        
        return new Entry();
      }
}

class SchemaEntry{
      // make all these attributes private later!!
      public String schema;
      public List<JSONObject> entries;
      public List<JSONObject> filters;
      public List<JSONObject> sinks;
      private List<Entry> transmogrifiedEntries;
      
      public SchemaEntry(String schema, List<JSONObject> entries, List<JSONObject> filters, List<JSONObject> sinks) {
        this.schema = schema;
        this.entries = entries;
        this.filters = filters;
        this.sinks = sinks;
        this.transmogrifiedEntries = new ArrayList<>();
      }

      // gets the schema and source functions, and runs the pipeline entry on the data
      public List<Entry> transmogrifyEntry() {
          List<Entry> entryData = new ArrayList<>();
          for (JSONObject entry : this.entries) {
            JSONObject source = (JSONObject)entry.get("source");
            List<JSONObject> filters = (List<JSONObject>)entry.get("filters");
            List<JSONObject> sinks = (List<JSONObject>)entry.get("sinks");
            Entry entryy = new Entry(source, filters, sinks);
            // maybe use completeable future here
            entryData.add(entryy.runPipelineEntry(this.schema));
          }
          this.transmogrifiedEntries = entryData;
          // return entryData;
      }

      // returns the filtered data, and sends it to the sink
      // public Data runPipelineSchemaEntry() {
      //     Data data = null;
      //     // for (Filter filter : this.filters) {
      //     //     FilterFunction filterFunc = HelperFunctions.getFilterFunction(filter.getFunc());
      //     //     FilterParameters filterParams = HelperFunctions.getFilterParameters(filter.getParams());
      //     //     filterParams.setSchema(this.schema);
      //     //     System.out.println(filterFunc);
      //     //     data = filterFunc.apply(this.transmogrifiedEntries, filterParams);
      //     // }

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


class HelperFunctions {

// START HERE IN LINE 235
  public static Runnable getFilterParameters(Map<String, Object> params) {
    return () -> {
      if (params.get("validator").equals("json")) {
        // VALIDATOR STUFF, LINE 228 transmogrifier2.js
        
      } else if (params.get("library") != null) {
        CompletableFuture<String> library = Reader.readUrlOrFile((String) params.get("library"));
        // runnable stuff 
        // set params.put("library", a function(library))
        
      }
      // return params;
    };
  }

//   public Runnable createFunction() {
//         // Return a reference to a method (a function)
//         return this::performAction;
//     }

//     public void performAction() {
//         System.out.println("Returned function performed!");
//     }




    // public static void main(String[] args) {
    //     HelperFunctions myFunction = createFunction();
    //     myFunction.perform(); // Call the function
    // }

    // public static MyFunction createFunction() {
    //     // Using a lambda expression to create an instance of MyFunction
    //     return () -> System.out.println("Function performed!");
    // }

    // returns the result of the source function
    public static CompletableFuture<String> getSourceFunction(JSONObject source) {
        String name = (String) source.get("func"); //url_read
        Map<String, Object> params = (Map<String, Object>) source.get("params");

        if ("file_read".equals(name)) { // Correct way to compare strings
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Assuming Sources.fileRead() takes a Map<String, Object> as a parameter
                    return Sources.fileRead(params).get();
                } catch (InterruptedException | ExecutionException | IOException e) {
                    // Handle exceptions accordingly
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        } else if ("url_read".equals(name)) { // Correct way to compare strings
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Assuming Sources.urlRead() takes a Map<String, Object> as a parameter
                    return Sources.urlRead(params).get();
                } catch (InterruptedException | ExecutionException | IOException e) {
                    // Handle exceptions accordingly
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        } else {
            // Handle other cases or throw an exception for an unsupported operation
            throw new IllegalArgumentException("Unsupported operation: " + name);
        }
    }

      // public static CompletableFuture<SourceFunction> getSourceFunction(String name) {
      //   CompletableFuture<SourceFunction> futureSource = CompletableFuture.supplyAsync(() -> {
      //       try {
      //           Source source;
      //           if (name.startsWith("http://") || name.startsWith("https://")) {
      //               String sourceContent = Reader.readURL(name);
      //               SourceFunction sourceFunction = createFunctionFromSourceContent(sourceContent);
      //               return sourceFunction;
      //           } else {
      //               SourceFunction sourceFunction = sources.get(name);
      //               return sourceFunction;
      //           }
      //       } catch (Exception e) {
      //           throw new RuntimeException(e);
      //       }
      //   });

      //   return futureSource;
      // }

    }

class Reader{

  public static CompletableFuture<String> readUrlOrFile(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (path.startsWith("http://") || path.startsWith("https://")) {
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
                  throw new IOException("Failed to read URL \"" + url + "\": HTTP status code " + connection.getResponseCode());
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

class Writer{

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

 class Sources {
  ////// call this by doing fileRead(params).get();
    public static CompletableFuture<String> fileRead(Map<String, Object> params) throws IOException {
        String path = (String) params.get("path");
        return Reader.readFile(path);
    }

    public static CompletableFuture<String> urlRead(Map<String, Object> params) throws IOException {
        String path = (String) params.get("path");
        return Reader.readURL(path);
    }

    //  public static Map<String, CompletableFuture<String>> getSources() {
    //     Map<String, CompletableFuture<String>> sources = new HashMap<>();
        
    //     // Usage example with empty params
    //     Map<String, Object> emptyParams = new HashMap<>();
        
    //     sources.put("file_read", CompletableFuture.supplyAsync(() -> {
    //         try {
    //             return fileRead(emptyParams).get(); // Use get() to retrieve the result
    //         } catch (IOException | InterruptedException | ExecutionException e) {
    //             throw new RuntimeException(e);
    //         }
    //     }));
        
    //     sources.put("url_read", CompletableFuture.supplyAsync(() -> {
    //         try {
    //             return urlRead(emptyParams).get(); // Use get() to retrieve the result
    //         } catch (IOException | InterruptedException | ExecutionException e) {
    //             throw new RuntimeException(e);
    //         }
    //     }));
        
    //     return sources;
    // }
}

class Filters {
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
}

class Sinks {
    public static String null_sink(Map<String, Object> params, String data) {
          return null;
    }

    public static CompletableFuture<String> fileWrite(Map<String, Object> params, String data) throws IOException {
        String path = (String) params.get("path");
        return Writer.writeFile(params, path);
    }

    public static CompletableFuture<String> urlWrite(String data, Map<String, Object> params) throws IOException {
        String path = (String) params.get("path");
        return Writer.writeUrl(params, path);
    }

    //  public static Map<String, CompletableFuture<String>> getSources() {
    //     Map<String, CompletableFuture<String>> sources = new HashMap<>();
        
    //     // Usage example with empty params
    //     Map<String, Object> emptyParams = new HashMap<>();
        
    //     sources.put("file_read", CompletableFuture.supplyAsync(() -> {
    //         try {
    //             return fileRead(emptyParams).get(); // Use get() to retrieve the result
    //         } catch (IOException | InterruptedException | ExecutionException e) {
    //             throw new RuntimeException(e);
    //         }
    //     }));
        
    //     sources.put("url_read", CompletableFuture.supplyAsync(() -> {
    //         try {
    //             return urlRead(emptyParams).get(); // Use get() to retrieve the result
    //         } catch (IOException | InterruptedException | ExecutionException e) {
    //             throw new RuntimeException(e);
    //         }
    //     }));
        
    //     return sources;
    // }
}
