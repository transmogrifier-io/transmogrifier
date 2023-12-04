package TransmogrifierJava;

import java.io.IOException;
// import java.io.Reader;
import java.util.concurrent.ExecutionException;
// script engine for graalvm
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
// import javax.xml.validation.Schema;
// import javax.xml.validation.Validator;

// to create a json object

// to help read js files from url - will likely need this to read a js filter from url: https://www.graalvm.org/
// import org.graalvm.polyglot.Context;
// import org.graalvm.polyglot.Value;
// import org.graalvm.polyglot.*;
// import org.graalvm.polyglot.proxy.*;
import java.io.InputStream;

import org.json.JSONObject;

// JSON validator
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaLoader;

import java.util.Map;


public class HelperFunctions {
    // engine is part of graalvmpolyglot
    // private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
    private static ClassLoader classLoader = Validator.class.getClassLoader();

    /**
     * Applies the filter function to the data
     * @param filter_function
     * @param data
     * @param params
     * @return
     */
    public static String applyFilterFunctionEntry(String filter_function, String data, Map<String, String> params) {
        // String data = the source function 'url_read' applied to the source params
          String filter_function_text = "";
          String filter_params_text = "";
          if (filter_function.startsWith("http://") || filter_function.startsWith("https://")) {
              try {
                // text from the filter function url
                filter_function_text = Reader.readURLNoLastLine(filter_function).get();
              } catch (IOException | InterruptedException | ExecutionException e) {
                System.out.println("please pass a filter function url!");
                e.printStackTrace();
              }
          }

          if (params != null) {

            if (params.get("validator") != null && params.get("validator").equals("json")) {
              try {
                InputStream inputStream = classLoader.getResourceAsStream("..manifest_example_nofilters.json");
                  if (inputStream != null) {
                    org.json.JSONObject rawSchema = new org.json.JSONObject(filter_function_text);
                    Schema schema = SchemaLoader.load(rawSchema);
                    String data_string = data.toString();
                    schema.validate(new org.json.JSONObject(data_string));

                    return data; 
                  }
              } catch (Exception e) {
                System.out.println("!!!Issue with filter > validator > json!!!");
              }
  
          } else if (params.get("library") != null) {
              /**
              * TO DO: evaluate js files
              */
          }

          }

        return Filters.getFilters(filter_function, data);
      }

}
