package TransmogrifierJava;

import TransmogrifierJava.Writer;
import TransmogrifierJava.Filters;
import TransmogrifierJava.Reader;
import TransmogrifierJava.Sources;

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
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaLoader;

// import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class HelperFunctions {
    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
    private static ClassLoader classLoader = Validator.class.getClassLoader();

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

    // TO DO NOV 23
    public static CompletableFuture<?> getFilterFunctionEntry(String filter_function, CompletableFuture<String> data, Map<String, String> params) {
        // String data = the source function 'url_read' applied to the source params
          System.out.println("in getFilterFunctionEntry function");
        System.out.println(data);
          System.out.println(params);
          String filter_function_text = "";
          if (filter_function.startsWith("http://") || filter_function.startsWith("https://")) {
              try {
                System.out.println(params);
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
                //text version of whatever was retrieved from https:// 
                String library = Reader.readUrlOrFile((String) params.get("library")).get();
                // System.out.println(library);
                // TEST
                Object library_object = engine.eval(library); // puts the string from url to the engine
                System.out.println(library_object);
                // filter_func(data, params);
                /*
                * COME BACK TO THIS, TO DO
                */
            } catch (InterruptedException | ExecutionException | ScriptException e) {
                System.out.println(e);
            }
        }
        String data_string = "";
        try {
            data_string = data.get();
            System.out.println(data.get());
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        System.out.println("!!!");
        System.out.println(data);
        // public static CompletableFuture<String> getFilters(String filter, org.json.JSONObject data) 
        return Filters.getFilters(filter_function, data_string);
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
