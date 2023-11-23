package transmogrifier2;

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
        try (FileReader reader = new FileReader("manifest_example.json")) {
            Object obj = parser.parse(reader);
            List<JSONObject> jsonObject = (List<JSONObject>) obj; // list of json object
            // System.out.println(jsonObject.get(0).get("schema"));
            Transmogrifier2 transmogrifier = new Transmogrifier2(jsonObject);
            transmogrifier.transmogrify();
            // transmogrifier.getSchemaEntries();

        } catch (IOException | ParseException e) {
        }
    }
} // end of Transmogrifier class

class Entry {
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
        CompletableFuture<String> data = null;

        for (JSONObject filter : this.filters) {
            // data = the original JSON data from the each city (https://opendata.vancouver.ca/api/explore/v2.1/catalog/datasets/public-art/exports/json?lang=en&timezone=America%2FLos_Angeles)
            data = HelperFunctions.getSourceFunction(this.source);
            String func_filter = (String) filter.get("func"); // filter fun url or "null_filter", etc
            Map<String, Object> params = (Map<String, Object>) filter.get("params"); // params: {indent:
            data = (CompletableFuture<String>) HelperFunctions.getFilterFunctionEntry(func_filter, data, params);
        }

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

class SchemaEntry {
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

class HelperFunctions {
    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
    private static ClassLoader classLoader = ValidateJSON.class.getClassLoader();

    // public Boolean validateJSONWithSchema(String schema, String data){

    // }
      
      // public static CompletableFuture<?> getFilterFunction(CompletableFuture<String> data, Map<String, Object> params) {
      //     // String data = the source function 'url_read' applied to the source params
  
      //     String filter_function_text = "";
      //     String filter_function = (String) params.get("func"); // url_read
      //     if (filter_function.startsWith("http://") || filter_function.startsWith("https://")) {
      //         try {
      //           // text from the filter function url
      //             filter_function_text = Reader.readURL(filter_function).get();
      //         } catch (IOException | InterruptedException | ExecutionException e) {
      //           System.out.println("please pass a filter function url!");
      //           e.printStackTrace();
      //         }
      //     }

      //     if (params.get("validator").equals("json")) {
      //       try {
      //         InputStream inputStream = classLoader.getResourceAsStream("./transmogrifier2/manifest.json");
      //           if (inputStream != null) {
      //             // try this if schemaText doesnt work: JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
      //             org.json.JSONObject rawSchema = new org.json.JSONObject(filter_function_text);
      //             Schema schema = SchemaLoader.load(rawSchema);
      //             String data_string = data.toString();
      //             schema.validate(new org.json.JSONObject(data_string));
      //             return data; //JSON object
      //           }
      //       } catch (Exception e) {
      //         System.out.println("Issue with filter > validator > json");
      //       }

      //   } else if (params.get("library") != null) {
      //       try {
      //           String library = Reader.readUrlOrFile((String) params.get("library")).get();
      //           engine.eval(library); // puts the string from url to the engine
      //           // filter_func(data, params);
      //           /*
      //           * COME BACK TO THIS, TO DO
      //           */
      //       } catch (InterruptedException | ExecutionException | ScriptException e) {
      //           System.out.println(e);
      //       }
      //   }
      //     // TO DO: USE JS CONVERTER SOMETIME LATER
      //     // engine.eval(library)
      //     return Filters.getFilters(filter_function, new org.json.JSONObject(data));
      // }


    public static CompletableFuture<?> getFilterFunctionEntry(String filter_function, CompletableFuture<String> data, Map<String, Object> params) {
      
        // String data = the source function 'url_read' applied to the source params
          System.out.println("in getFilterFunctionEntry function");
          System.out.println(params);
          String filter_function_text = "";
          if (filter_function.startsWith("http://") || filter_function.startsWith("https://")) {
              try {
                // text from the filter function url
                  filter_function_text = Reader.readURL(filter_function).get();
              } catch (IOException | InterruptedException | ExecutionException e) {
                System.out.println("please pass a filter function url!");
                e.printStackTrace();
              }
          }

          if (params.get("validator") != null && params.get("validator").equals("json")) {
            try {
              InputStream inputStream = classLoader.getResourceAsStream("./transmogrifier2/manifest.json");
                if (inputStream != null) {
                  // try this if schemaText doesnt work: JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
                  org.json.JSONObject rawSchema = new org.json.JSONObject(filter_function_text);
                  Schema schema = SchemaLoader.load(rawSchema);
                  String data_string = data.toString();
                  schema.validate(new org.json.JSONObject(data_string));
                  return data; //JSON object
                }
            } catch (Exception e) {
              System.out.println("Issue with filter > validator > json");
            }

        } else if (params.get("library") != null) {
            try {
                String library = Reader.readUrlOrFile((String) params.get("library")).get();
                System.out.println(library);
                engine.eval(library); // puts the string from url to the engine
                // filter_func(data, params);
                /*
                * COME BACK TO THIS, TO DO
                */
            } catch (InterruptedException | ExecutionException | ScriptException e) {
                System.out.println(e);
            }
        }
          // TO DO: USE JS CONVERTER SOMETIME LATER
          // engine.eval(library)
          return Filters.getFilters(filter_function, new org.json.JSONObject(data));
      }


     public static void getSinkFunctionSchemaEntry(Map<String, String> map, List<?> data) {
    }


    public static CompletableFuture<?> getFilterFunctionSchemaEntry(List<?> data, Map<String, Object> params) {
        return null;
      }



    // String data = the json object that we write to the file - not sure if it should be a JSONobject
    public static void getSinkFunction(Map<String, String> sink_params, String data) {
        String file_path = (String) sink_params.get("path");

        // write to a file
        try (FileWriter fileWriter = new FileWriter(file_path)) {
            fileWriter.write(data);
            System.out.println("JSON data has been written to the file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing JSON data to the file.");
            e.printStackTrace();
        }
    }


    // returns the result of the source function
    public static CompletableFuture<String> getSourceFunction(JSONObject source) {
        String name = (String) source.get("func"); // url_read
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

    // public static CompletableFuture<SourceFunction> getSourceFunction(String
    // name) {
    // CompletableFuture<SourceFunction> futureSource =
    // CompletableFuture.supplyAsync(() -> {
    // try {
    // Source source;
    // if (name.startsWith("http://") || name.startsWith("https://")) {
    // String sourceContent = Reader.readURL(name);
    // SourceFunction sourceFunction =
    // createFunctionFromSourceContent(sourceContent);
    // return sourceFunction;
    // } else {
    // SourceFunction sourceFunction = sources.get(name);
    // return sourceFunction;
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // });

    // return futureSource;
    // }

}

class Reader {

    public static CompletableFuture<String> readUrlOrFile(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    System.out.println(readURL(path).get());
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
                    throw new IOException(
                            "Failed to read URL \"" + url + "\": HTTP status code " + connection.getResponseCode());
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

class Writer {

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

    // public static Map<String, CompletableFuture<String>> getSources() {
    // Map<String, CompletableFuture<String>> sources = new HashMap<>();

    // // Usage example with empty params
    // Map<String, Object> emptyParams = new HashMap<>();

    // sources.put("file_read", CompletableFuture.supplyAsync(() -> {
    // try {
    // return fileRead(emptyParams).get(); // Use get() to retrieve the result
    // } catch (IOException | InterruptedException | ExecutionException e) {
    // throw new RuntimeException(e);
    // }
    // }));

    // sources.put("url_read", CompletableFuture.supplyAsync(() -> {
    // try {
    // return urlRead(emptyParams).get(); // Use get() to retrieve the result
    // } catch (IOException | InterruptedException | ExecutionException e) {
    // throw new RuntimeException(e);
    // }
    // }));

    // return sources;
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

    public static CompletableFuture<String> getFilters(String filter, org.json.JSONObject data) {
        Map<String, CompletableFuture<String>> filters = new HashMap<>();
        String data_string = data.toString();
        filters.put("null_filter", null_filter(data_string));

        filters.put("to_upper", to_upper(data_string));

        filters.put("to_lower", to_lower(data_string));

        return filters.get(filter);
    }
}

class Sinks {
    public static String null_sink(Map<String, Object> params, String data) {
        return null;
    }

    public static CompletableFuture<Void> fileWrite(Map<String, Object> params, String data) throws IOException {
        String path = (String) params.get("path");
        return Writer.writeFile(params, path);
    }

    // public static CompletableFuture<Void> urlWrite(String data, Map<String, Object> params) throws IOException {
    //     String path = (String) params.get("path");
    //     return Writer.writeUrl(params, path);
    // }

    // public static Map<String, CompletableFuture<String>> getSources() {
    // Map<String, CompletableFuture<String>> sources = new HashMap<>();

    // // Usage example with empty params
    // Map<String, Object> emptyParams = new HashMap<>();

    // sources.put("file_read", CompletableFuture.supplyAsync(() -> {
    // try {
    // return fileRead(emptyParams).get(); // Use get() to retrieve the result
    // } catch (IOException | InterruptedException | ExecutionException e) {
    // throw new RuntimeException(e);
    // }
    // }));

    // sources.put("url_read", CompletableFuture.supplyAsync(() -> {
    // try {
    // return urlRead(emptyParams).get(); // Use get() to retrieve the result
    // } catch (IOException | InterruptedException | ExecutionException e) {
    // throw new RuntimeException(e);
    // }
    // }));

    // return sources;
    // }
}
