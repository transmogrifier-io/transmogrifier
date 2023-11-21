using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;


class Program
{
    static void Main(string[] args)
    {
        if (args.Length == 0)
        {
            Console.WriteLine("Please provide an argument");
            Environment.Exit(1);
        }

        string manifestUrl = args[0];
        LoadManifest(manifestUrl).Wait();
    }

    static async Task LoadManifest(string manifestPath)
    {
        if (manifestPath.StartsWith("http://") || manifestPath.StartsWith("https://"))
        {
            using (var client = new WebClient())
            {
                try
                {
                    string rawData = await client.DownloadStringTaskAsync(manifestPath);
                    await ProcessManifest(rawData);
                }
                catch (WebException e)
                {
                    Console.WriteLine($"Failed to load manifest: {e.Message}");
                    Environment.Exit(1);
                }
            }
        }
        else
        {
            try
            {
                string data = File.ReadAllText(manifestPath);
                await ProcessManifest(data);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
                Environment.Exit(1);
            }
        }
    }

    static async Task ProcessManifest(string data)
  {
      try
      {
          var manifest = JsonConvert.DeserializeObject<List<Dictionary<string, object>>>(data);

          if (manifest != null) // Check if deserialization was successful
          {
              // Handling arrays in the manifest if present
              foreach (var item in manifest)
              {
                  foreach (var key in item.Keys.ToList())
                  {
                      if (item[key] is JArray array && array != null) // Check if it's a JArray and not null
                      {
                          item[key] = array.ToObject<List<object>>() ?? new List<object>(); // Assign the converted value or an empty list if null
                      }
                  }
              }

              if (manifest.Count > 0) // Check if manifest has any data
              {
                  Transmogrifier transmog = new Transmogrifier(manifest);
                  List<object> transmogrifiedData = await transmog.Transmogrify();

                  if (transmogrifiedData != null) // Check if transmogrifiedData is not null
                  {
                      Console.WriteLine(string.Join(Environment.NewLine, transmogrifiedData));
                  }
                  else
                  {
                      Console.WriteLine("Transmogrified data is null");
                  }
              }
              else
              {
                  Console.WriteLine("Manifest does not contain any data");
              }
          }
          else
          {
              Console.WriteLine("Manifest deserialization resulted in null");
          }
      }
      catch (Exception e)
      {
          Console.WriteLine(e.Message);
      }
  }


}
