// import org.json.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Transmogrifier {

    // Get the source function from the given parameter, if param is a URL, get the function from the URL,
  // otherwise, get the function from the mapping
//   private static String getSourceFunction(String name) {
//       let source;

//       if (name.startsWith("http://") || name.startsWith("https://")) {
//           source = await readURL(name);
//           source = new Function(source)();
//       }
//       else {
//           source = sources[name];
//       }

//       return source
//   }

//   //async function  - need to incorporate async somehow
//   private static String readFile(String path) {
//     String data = "";
//     try (FileReader reader = new FileReader(path)) {
//       int character;
//       while ((character = reader.read()) != -1) {
//         data += (char) character;
//       }
//     } catch (IOException e) {
//       e.printStackTrace();
//     }
//     return data;
//   }
  
//   private static String readURLOrFile(String path){
//     String data;
//     if (path.startsWith("http://") || path.startsWith("https://")) {
//       data = readURL(path);
//     }else{
//       data = readFile(path);
//     }
//     return data;
//   }

//   private static String getSchema(String path){
//     if(path == null){
//       return "";
//     }
//     ???? schema = readURLOrFile(path);
//     return schema;
//   }


//   private static List<????????> transmogrifyEntry(JSONObject entry, String schema_path) {
//     JSONObject source = (JSONObject) entry.get("source");
//     List<JSONObject> filters = (List<JSONObject>) entry.get("filters");
//     List<JSONObject> sinks = (List<JSONObject>) entry.get("sinks");

//     ????? schema = getSchema(schema_path);
//     ???? sourceFunc = getSourceFunction(source.get("func"));

//     return runPipelineEntry(sourceFunc, source.params, filters, sinks, schema)
//   }
  
//   private static List<??????> transmogrifySchemaEntry(List<JSONObject>data, JSONObject schemaEntry) {
//     const filters = schemaEntry.filters ?? [];
//     const sinks = schemaEntry.sinks ?? []; 

//     const schema = await getSchema(schemaEntry.schema);

//     return runPipelineSchemaEntry(data, filters, sinks, schema);
// }

  //get schema 
  public static CompletableFuture<List<JSONObject>> transmogrify(List<JSONObject> manifest) {
    System.out.println("this is the manifest inside the transmogrify function");

    return CompletableFuture.supplyAsync(() -> {
      // List<JSONObject> list = new ArrayList<JSONObject>();
      // return list;
    
      List<JSONObject> schemaEntryDatas = new ArrayList<JSONObject>();
      // System.out.println(manifest);
      try {
        System.out.println("before sleeping");
        Thread.sleep(1);
        System.out.println("in the try block");
      }
      catch (Exception e) {
        System.out.println("catching");
      }
      System.out.println("before return");
      // for (JSONObject schemaEntry: manifest) {
      //   List<Object> entryDatas = new ArrayList<Object>();
      //   // schemaEntry.schema = (schemaEntry.schema != null) ? schemaEntry.schema : "";
      //   // treat the schema as an empty string if its null
      //   String originalSchema = (String) schemaEntry.get("schema");
      //   String tempSchema = (schemaEntry.get("schema") == null) ? "" : originalSchema;
      //   schemaEntry.put("schema", tempSchema);
      //   for (JSONObject entry: (List<JSONObject>) schemaEntry.get("entries")) {
      //     System.out.println(entry);
      //     // CHECK THIS TYPE
      //     // JSONObject entryData = transmogrifyEntry(entry, schemaEntry.get("schema"));
      //     // entryDatas.add(entryData);        
      //   }
      //   // CHECK THIS TYPE
      //   // JSONObject schemaEntryData = transmogrifySchemaEntry(entryDatas, schemaEntry);
      //   schemaEntryDatas.add(schemaEntryData);
      // }    
  
      return schemaEntryDatas;
    });
  }

  public static void main(String[] args) {
    Transmogrifier transmogrifier = new Transmogrifier();
    List<JSONObject> manifest = new ArrayList<JSONObject>();
    JSONParser parser = new JSONParser();
    
    // manifest-schema.json file reader
    try (FileReader reader = new FileReader("manifest_example.json")) {
      Object obj = parser.parse(reader);
      List<JSONObject> jsonObject = (List<JSONObject>) obj;
      CompletableFuture<List<JSONObject>> future = transmogrify(jsonObject);
      future.thenAccept(result -> {
        System.out.println(result);
      });

      future.join();
      System.out.println("MAIN FUNCTION");

    } catch (IOException | ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}