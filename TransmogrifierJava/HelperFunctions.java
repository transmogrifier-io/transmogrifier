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

import javax.script.Bindings;
import javax.script.Invocable;
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

import javax.script.*;


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

    public static CompletableFuture<?> getFilterFunctionEntry(String filter_function, CompletableFuture<String> data, Map<String, String> params) {
        // String data = the source function 'url_read' applied to the source params
          System.out.println("HelperFunc: in getFilterFunctionEntry function");
        // System.out.println(data);
        //   System.out.println(params);
          String filter_function_text = "";
          String filter_params_text = "";
          if (filter_function.startsWith("http://") || filter_function.startsWith("https://")) {
              try {
                System.out.println(params);
                // text from the filter function url
                String new_filter_function = "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/ss-ee/filters/canada/british-columbia/vancouver/public-art-json-to-json.js";
                // https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/canada/british-columbia/vancouver/public-art-json-to-json.js : NEED (data, param)
                  filter_function_text = Reader.readURLNoLastLine(new_filter_function).get();
                //   System.out.println(filter_function_text);
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
                // use engine to run the js script func : filter_function_text
                // pass in data and the library url to the above
                try {
                    filter_params_text = Reader.readURLNoLastLine(params.get("library")).get();
                    filter_params_text += " function getLib() { return lib; }";
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // System.out.println("!!!!filter_params_text: " + filter_params_text + "\n");

                Object content_result = engine.eval(filter_params_text); // evalutes the library params from the filter entries list
                // params.library = Map<String, Object>
                // Object csv_parser = ((Map<String, Object>) content_result).get("csv_parser");
                // Object add_ ((Map<String, Object>) content_result).get("add_required");
                // Object add_if_not_required =null = ((Map<String, Object>) content_result).get("add_if_not_null");
                // Object remove_if_null = ((Map<String, Object>) content_result).get("remove_if_null");
                // Object remove_if_empty = ((Map<String, Object>) content_result).get("remove_if_empty");
                // Object create_dates_template = ((Map<String, Object>) content_result).get("create_dates_template");
                // Object remove_null_date_fields = ((Map<String, Object>) content_result).get("remove_null_date_fields");
                // Object remove_if_zero = ((Map<String, Object>) content_result).get("remove_if_zero");
                // Object validate_params = ((Map<String, Object>) content_result).get("validate_params"); 
                
                StringBuilder library_string = new StringBuilder("{");
                JSONObject library_json = new JSONObject();
                // Map<String, Object> library = new HashMap<String, Object>();
                for (Map.Entry<String, Object> entry: ((Map<String, Object>) content_result).entrySet()) {
                    String functionName = entry.getKey();
                    Object function = entry.getValue();
                    library_string.append("'" + functionName + "' : '" + function + "', "); //{"key" : "value"}
                    library_json.put(functionName, function);
                    // library.put(functionName, function);
                }
                library_string.deleteCharAt(library_string.length() - 2);
                //  JSONObject library_json = new JSONObject(library_string);

                library_string.append("}");
                String params_string = "{ 'library': '" + library_string.toString() + "' }";
                System.out.println("params_string: " + params_string);
                // JSONObject jsonObject = new JSONObject(jsonString);
                // JSONObject params_json = new JSONObject(params_string);
                // params_json.put("library", library_json);

                // System.out.println("\nlibrary: " + params_json.toString() + "\n");
                // params.put("library", library.toString());

                // String library_info = params.get("library");
                // System.out.println("!!!!!!!!!!!!!!");
                // System.out.println(library_info);

               
                // System.out.println("library_remove_null: " + library_remove_null);

                // Object content_result = engine.eval(content);
                // System.out.println(((Map<String, Object>) content_result).get("remove_null_date_fields"));
                
                // System.out.println("filter param text: " + libFunction);
                
                //filter_function_text = public-art-json-to-json.js
                engine.eval(filter_function_text); // evalutes the library func from the filter entries list
                Object filterFunction = engine.get("filter");
                // Retrieves the filter function
                System.out.println("filterFunction: " + filterFunction);


                Invocable invocable = (Invocable) engine;

                
                // Object libraryResult = invocable.invokeFunction("lib");
                params.put("library", filter_params_text);

                //pase json


                //creates a java map representing the javascript object 
                Bindings paramsObject = engine.createBindings();
                paramsObject.put("library", "value1");
                
                // Object json = engine.eval("function a() { JSON.parse({ \"library\" :\"hello\" }) }");
                // System.out.println("!!!!" + json);
                // Object result = engine.eval("function a() { return JSON.parse('{ \"library\" :\"hello\" }'); }");
                Object result = engine.eval("function a() { return { \"library\": \"hello\" }; }");
                Object json = ((javax.script.Invocable) engine).invokeFunction("a");
                System.out.println("Parsed JSON object: " + json);
                
                Object filterResult = invocable.invokeFunction("filter", data.get(), json);
                System.out.println("SUCCESSFULLY PASSED LINE 225!!!");
                // Object filterResult = invocable.invokeFunction("filter", data.get(), "{ library: 'hello'}");
                System.out.println("Result of filter function: " + filterResult);

                return (CompletableFuture<?>) filterResult;
                
                // filter_func(data, params);
                /*
                * COME BACK TO THIS, TO DO
                */
            } catch (InterruptedException | ExecutionException | ScriptException e) {
                System.out.println(e);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String data_string = "";
        try {
            data_string = data.get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        // System.out.println("!!!");
        // System.out.println(data);
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
