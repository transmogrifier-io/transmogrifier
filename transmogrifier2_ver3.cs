using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.IO;
using System.Net;
using System.Text;
using System.Linq;
using Newtonsoft.Json;
using Newtonsoft.Json.Schema;

public class Transmogrifier
{
    private List<SchemaEntry> schemaEntries;
    private List<Dictionary<string, object>> manifest;

    public Transmogrifier(List<Dictionary<string, object>> manifest)
    {
        this.manifest = manifest;
        this.schemaEntries = GetSchemaEntries();
    }

    private List<SchemaEntry> GetSchemaEntries()
    {
        var schemaList = new List<SchemaEntry>();
        foreach (var schema in manifest)
        {
            var schemaEntry = new SchemaEntry(
                schema["schema"].ToString(), 
                (List<object>)schema["entries"],
                (List<object>)schema["filters"],
                (List<object>)schema["sinks"]
            );
            schemaList.Add(schemaEntry);
        }
        return schemaList;
    }

    public async Task<List<object>> Transmogrify()
    {
        var schemaEntryDatas = new List<object>();
        foreach (var schemaEntry in schemaEntries)
        {
            await schemaEntry.TransmogrifyEntry();
            schemaEntryDatas.Add(await schemaEntry.RunPipelineSchemaEntry());
        }

        return schemaEntryDatas;
    }
}


public class SchemaEntry
{
    private string schema;
    private List<Entry> entries;
    private List<object> filters;
    private List<object> sinks;
    private List<object> transmogrifiedEntries;

    public SchemaEntry(string schema, List<object> entries, List<object> filters, List<object> sinks)
    {
        this.schema = schema ?? "";
        this.entries = EntriesToObj(entries);
        this.filters = filters;
        this.sinks = sinks;
    }

    private List<Entry> EntriesToObj(List<object> data)
    {
      var entryList = new List<Entry>();
      foreach (var entry in data)
      {
          var entryDict = entry as Dictionary<string, object>;
          var source = entryDict["source"];
          var filters = entryDict["filters"] as List<object> ?? new List<object>();
          var sinks = entryDict["sinks"] as List<object> ?? new List<object>();

          var newEntry = new Entry(source, filters, sinks);
          entryList.Add(newEntry);
      }
      return entryList;
    }

    public async Task TransmogrifyEntry()
    {
        try
        {
            var entryData = new List<object>();
            foreach (var entry in entries)
            {
                entryData.Add(await ((Entry)entry).RunPipelineEntry());
            }
            transmogrifiedEntries = entryData;
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error in TransmogrifyEntry: " + ex.Message);
            throw;
        }
    }

    public async Task<List<object>> RunPipelineSchemaEntry()
    {
        var data = transmogrifiedEntries;
        foreach (var filter in filters)
        {
            var filterFunc = await HelperFunctions.GetFilterFunction(((Dictionary<string, object>)filter)["func"].ToString());
            var filterParams = await HelperFunctions.GetFilterParameters(((Dictionary<string, object>)filter)["params"] as Dictionary<string, object> ?? new Dictionary<string, object>());
            filterParams["schema"] = await HelperFunctions.GetSchema(schema);

            // Renaming 'filterFunc' to 'filterFunction' in the method invocation
            data = (List<object>)await ((Func<object, Dictionary<string, object>, Task<object>>)filterFunc)(data, filterParams);
        }
        foreach (var sink in sinks)
        {
            var sinkFunc = await HelperFunctions.GetSinkFunction(((Dictionary<string, object>)sink)["func"].ToString());
            var sinkParams = (Dictionary<string, object>)(((Dictionary<string, object>)sink)["params"]) ?? new Dictionary<string, object>();

            // Renaming 'sinkFunc' to 'sinkFunction' in the method invocation
            await ((Func<Dictionary<string, object>, object, Task>)sinkFunc)(sinkParams, data);
        }
        return data;
    }

}



public class Entry
{
    private object source;
    private object sourceParams;
    private List<object> filters; // Assuming filters and sinks are arrays of objects
    private List<object> sinks;

    public Entry(object source, List<object> filters, List<object> sinks)
    {
        this.source = source;
        this.sourceParams = ((Dictionary<string, object>)source)["params"]; // Assuming "params" is a property in the source object
        this.filters = filters;
        this.sinks = sinks;
    }

    public async Task<object> RunPipelineEntry()
    {
        try
        {
            object data;
            var funcValue = ((Dictionary<string, object>)source)["func"];
            var funcString = funcValue != null ? funcValue.ToString() : null; // Convert funcValue to string
            var sourceFunc = await HelperFunctions.GetSourceFunction(funcString);
            
            if (sourceFunc != null && sourceParams != null)
            {
                data = await ((Func<object, Task<object>>)sourceFunc)(sourceParams); // Removed unnecessary .Invoke()
            }
            else
            {
                throw new ArgumentException("Invalid source function or source parameters");
            }

            foreach (var filter in filters)
            {
                var filterDict = (Dictionary<string, object>)filter;
                var filterFunc = await HelperFunctions.GetFilterFunction(filterDict["func"].ToString());
                var filterParams = await HelperFunctions.GetFilterParameters(filterDict["params"] as Dictionary<string, object> ?? new Dictionary<string, object>());
                data = await ((Func<object, Dictionary<string, object>, Task<object>>)filterFunc)(data, filterParams); // Changed the invocation
            }

            foreach (var sink in sinks)
            {
                var sinkDict = sink as Dictionary<string, object>; // Cast sink to Dictionary<string, object>
                var sinkFunc = await HelperFunctions.GetSinkFunction(sinkDict["func"].ToString());
                var sinkParams = sinkDict["params"] as Dictionary<string, object> ?? new Dictionary<string, object>();
                // Check if sinkFunc is a delegate (a function or method) before invoking it
                if (sinkFunc is Func<Dictionary<string, object>, object, Task>)
                {
                    // Invoke the delegate sinkFunc with parameters sinkParams and data
                    await ((Func<Dictionary<string, object>, object, Task>)sinkFunc)(sinkParams, data);
                }
                else
                {
                    throw new InvalidOperationException("sinkFunc is not a valid delegate");
                }
            }
            return data;

        }
        catch (Exception ex)
        {
            Console.WriteLine("Error in RunPipelineEntry: " + ex.Message);
            throw;
        }
    }
}


public static class HelperFunctions
{
    public static async Task<object> GetSourceFunction(string name)
    {
        object source = null;
        try
        {
            if (name.StartsWith("http://") || name.StartsWith("https://"))
            {
                string sourceCode = await Sources.UrlRead(new { path = name }); // Access UrlRead from Sources class
                source = new Func<object>(() => { return sourceCode; });
            }
            else
            {
                source = await Sources.FileRead(new { path = name }); // Access FileRead from Sources class
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
        }
        return source;
    }

    public static async Task<object> GetFilterFunction(string name)
    {
        object filter = null;
        try
        {
            if (name.StartsWith("http://") || name.StartsWith("https://"))
            {
                string filterCode = await Reader.ReadURL(name);
                filter = new Func<object>(() => { return filterCode; });
            }
            else
            {
                // Assuming filters is a dictionary containing the functions
                filter = typeof(Filters).GetMethod(name).Invoke(null, new object[] { null, null });
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
        }
        return filter;
    }


    public static async Task<object> GetSinkFunction(string name)
    {
        object sink = null;
        try
        {
            if (name.StartsWith("http://") || name.StartsWith("https://"))
            {
                string sinkCode = await Reader.ReadURL(name);
                sink = new Func<object>(() => { return sinkCode; });
            }
            else
            {
                // Assuming sinks is a dictionary containing the functions
                sink = typeof(Sinks).GetMethod(name).Invoke(null, new object[] { null, null });
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
        }
        return sink;
    }


    public static async Task<string> GetSchema(string path)
    {
        if (string.IsNullOrEmpty(path))
        {
            return "";
        }
        string schema = await Reader.ReadURLOrFile(path);
        return schema;
    }

    public static async Task<Dictionary<string, object>> GetFilterParameters(Dictionary<string, object> parameters)
    {
        if (parameters.ContainsKey("validator") && parameters["validator"].ToString() == "json")
        {
            // Assuming parameters["schema"] contains the JSON schema as a string
            var schemaText = await Reader.ReadURLOrFile(parameters["schema"].ToString());

            // Validate JSON schema using Newtonsoft.Json.Schema
            JSchema schema = JSchema.Parse(schemaText);
            parameters["jsonschema"] = schema;
        }

        if (parameters.ContainsKey("library"))
        {
            string library = await Reader.ReadURLOrFile(parameters["library"].ToString());
            parameters["library"] = new Func<object>(() => { return library; });
        }
        return parameters;
    }
}


public static class Reader
{
    // Platform-specific functions for reading a file

    // Node.js equivalent for reading a local file
    public static Task<string> ReadLocalFileNode(string filePath)
    {
        return File.ReadAllTextAsync(filePath);
    }

    // Web browser equivalent for reading a local file
    public static Task<string> ReadLocalFileWeb(string filePath)
    {
        var webRequest = WebRequest.Create(filePath);
        using (var response = webRequest.GetResponse())
        using (var content = response.GetResponseStream())
        using (var reader = new StreamReader(content))
        {
            return reader.ReadToEndAsync();
        }
    }

    // Function for reading a file
    public static async Task<string> ReadFile(string filePath)
    {
        string data;
        if (Environment.GetEnvironmentVariable("NODE_ENV") != null)
        {
            // Running in Node.js (assumed)
            data = await ReadLocalFileNode(filePath);
        }
        else
        {
            // Assume running in a different environment (e.g., .NET Core)
            data = await ReadLocalFileWeb(filePath);
        }
        return data;
    }

    /**
     * Function for reading a URL
     * @param {string} url - URL to read
     * @returns data from URL
     */
    public static async Task<string> ReadURL(string url)
    {
        string data;
        var httpModule = url.StartsWith("https://") ? "https://" : "http://";

        using (var webClient = new WebClient())
        {
            try
            {
                data = await webClient.DownloadStringTaskAsync(url);
            }
            catch (WebException ex)
            {
                throw new Exception("Failed to read URL \"" + url + "\": " + ex.Message);
            }
        }

        return data;
    }

    /**
     * Function to return data from a URL or file path
     * @param {string} path - URL or file path
     * @returns data as string
     */
    public static async Task<string> ReadURLOrFile(string path)
    {
        string data;
        if (path.StartsWith("http://") || path.StartsWith("https://"))
        {
            data = await ReadURL(path);
        }
        else
        {
            data = await ReadFile(path);
        }
        return data;
    }
}


public static class Writer
{
    // Platform-specific functions for writing a file

    // Node.js equivalent for writing a local file
    public static Task WriteLocalFileNode(string filePath, bool append, string data)
    {
        if (append)
        {
            data = "\n" + data;
            return File.AppendAllTextAsync(filePath, data);
        }
        else
        {
            return File.WriteAllTextAsync(filePath, data);
        }
    }

    // Web browser equivalent for writing a local file
    public static async Task WriteLocalFileWeb(string filePath, bool append, string data)
    {
        var webRequest = WebRequest.Create(filePath) as HttpWebRequest;
        webRequest.Method = "PUT";
        webRequest.ContentType = "text/plain";

        byte[] byteArray = Encoding.UTF8.GetBytes(data);
        webRequest.ContentLength = byteArray.Length;

        using (var requestStream = await webRequest.GetRequestStreamAsync())
        {
            await requestStream.WriteAsync(byteArray, 0, byteArray.Length);
        }

        var response = await webRequest.GetResponseAsync() as HttpWebResponse;

        if (response.StatusCode != HttpStatusCode.OK)
        {
            throw new Exception("Error writing file");
        }
    }

    // Function for writing a file
    public static async Task WriteFile(dynamic parameters, string data)
    {
        string filePath = parameters.path;
        bool append = parameters.append;

        if (Environment.GetEnvironmentVariable("NODE_ENV") != null)
        {
            // Running in Node.js (assumed)
            await WriteLocalFileNode(filePath, append, data);
        }
        else
        {
            // Assume running in a different environment (e.g., .NET Core)
            await WriteLocalFileWeb(filePath, append, data);
        }
    }

    // Function for writing to a URL
    public static async Task<string> WriteURL(string url, string data)
    {
        string result = "";
        var newUrl = new Uri(url);

        var httpModule = url.StartsWith("https://") ? "https://" : "http://";

        var request = WebRequest.CreateHttp(url);
        request.Method = "POST";
        request.ContentType = "application/json";

        byte[] byteArray = Encoding.UTF8.GetBytes(data);
        request.ContentLength = byteArray.Length;

        using (var requestStream = await request.GetRequestStreamAsync())
        {
            await requestStream.WriteAsync(byteArray, 0, byteArray.Length);
        }

        try
        {
            var response = await request.GetResponseAsync() as HttpWebResponse;

            if (response.StatusCode != HttpStatusCode.OK)
            {
                throw new Exception("Failed to post data \"" + request.RequestUri + "\": HTTP status code " + response.StatusCode);
            }

            using (var responseStream = response.GetResponseStream())
            {
                using (var reader = new StreamReader(responseStream))
                {
                    result = await reader.ReadToEndAsync();
                }
            }
        }
        catch (WebException ex)
        {
            throw new Exception("Failed to post data \"" + request.RequestUri + "\": " + ex.Message);
        }

        return result;
    }
}


public static class Lister
{
    // Platform-specific functions for listing files in a directory

    // Node.js equivalent for listing files in a directory
    public static Task<List<string>> ListFilesNode(string dirPath, string extension)
    {
        return Task.Run(() =>
        {
            var files = Directory.GetFiles(dirPath);
            var filteredFiles = files.Where(file => file.EndsWith(extension)).ToList();
            return filteredFiles;
        });
    }

    // Web browser equivalent for listing files in a directory
    public static async Task<List<string>> ListFilesWeb(string dirPath, string extension)
    {
        var webRequest = WebRequest.Create(dirPath) as HttpWebRequest;
        var fileList = new List<string>();

        using (var response = await webRequest.GetResponseAsync() as HttpWebResponse)
        using (var content = response.GetResponseStream())
        using (var reader = new StreamReader(content))
        {
            var files = (await reader.ReadToEndAsync()).Split('\n');
            fileList = files.Where(file => file.EndsWith(extension)).ToList();
        }

        return fileList;
    }

    // Function for listing files

    public static async Task<List<string>> ListFiles(string dirPath, string extension)
    {
        List<string> fileList;

        if (Environment.GetEnvironmentVariable("NODE_ENV") != null)
        {
            // Running in Node.js (assumed)
            fileList = await ListFilesNode(dirPath, extension);
        }
        else
        {
            // Assume running in a different environment (e.g., .NET Core)
            fileList = await ListFilesWeb(dirPath, extension);
        }

        return fileList;
    }
}

public static class Sources
{
    public static async Task<string> FileRead(dynamic parameters)
    {
        return await Reader.ReadFile(parameters.path);
    }

    public static async Task<string> UrlRead(dynamic parameters)
    {
        return await Reader.ReadURL(parameters.path);
    }
}

public static class Filters
{
    public static async Task<string> NullFilter(string data, dynamic parameters)
    {
        return data;
    }

    public static async Task<string> ToUpper(string data, dynamic parameters)
    {
        return data.ToUpper();
    }

    public static async Task<string> ToLower(string data, dynamic parameters)
    {
        return data.ToLower();
    }
}

public static class Sinks
{
    public static async Task FileWrite(dynamic parameters, string data)
    {
        await Writer.WriteFile(parameters, data);
    }

    public static async Task UrlWrite(dynamic parameters, string data)
    {
        await Writer.WriteURL(parameters, data);
    }
}

