import 'dart:io';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:flutter_js/flutter_js.dart';
// http and https
// ajv

/// To count the number of times a function is called
int count = 0;

final JavascriptRuntime jsRuntime = getJavascriptRuntime();

Future<dynamic> readFile(String filePath) {
  File src = File(filePath);
  return src.readAsString();
}

Future<dynamic> readURL(String uri_string) async {
  Uri uri = Uri.parse(uri_string);
  var response = await http.get(Uri.http(uri.authority, uri.path));
  return response.body;
}

dynamic readURLOrFile(String path) async {
  dynamic data;
  if (path.startsWith('http://') || path.startsWith('https://')) {
    data = await readURL(path);
  } else {
    data = await readFile(path);
  }
  return data;
}

Future<void> writeFile(String filePath, dynamic data) async {
  File dst = File(filePath);
  await dst.writeAsString(data);
}

Future<void> writeURL(String url, dynamic data) async {
  // should write data to a url
  var postURL = Uri.https(url, '');
  var response = await http.post(postURL, body: data);

  JsEvalResult result = jsRuntime.evaluate('response.body');
  return result.rawResult;
}

Map sources = {
  'file_read': (Map params) async {
    return await readFile(params['path']);
  },
  'url_read': (Map params) async {
    return await readURL(params['path']);
  }
};

Map filters = {
  'null_filter': (dynamic data, Map params) async {
    return data;
  },
  'to_upper': (dynamic data, Map params) async {
    return data.toString().toUpperCase();
  },
  'to_lower': (dynamic data, Map params) async {
    return data.toString().toLowerCase();
  }
};

Map sinks = {
  'null': (Map params, dynamic data) async {
    return;
  },
  'file_write': (Map params, dynamic data) async {
    return await writeFile(params['path'], data);
  },
  'url_write': (Map params, dynamic data) async {
    return await writeURL(params['path'], data);
  }
};

dynamic runJSFilter(dynamic data) async {
  JsEvalResult filterResult = jsRuntime.evaluate("""JSON.stringify(filter(${jsonEncode(data)}, params), null);""");
  
  return jsonDecode(filterResult.toString());
}

// data comes in as a string, leaves as a Map (I think types could be messed up here)
Future<dynamic> runPipelineEntry(Function sourceFunc, Map sourceParams, List filters, Function sinkFunc, Map sinkParams, String schema) async {
  dynamic data = await sourceFunc(sourceParams);

  for (Map filter in filters) {
    dynamic filterFunc = await getFilterFunction(filter['func']);
    Map filterParams = await getFilterParameters(filter['params'] ?? {});
    filterParams['schema'] = schema;
    if (filterFunc is Function) {
      data = await filterFunc(data, filterParams);
    } else {
      data = await runJSFilter(data);
    }
  }
  await sinkFunc(sinkParams, data);
  return data;
}

Future<dynamic> runPipelineSchemaEntry(dynamic data, List filters, Function sinkFunc, Map sinkParams, String schema) async {
  for (Map filter in filters) {
    dynamic filterFunc = await getFilterFunction(filter['func']);
    Map filterParams = await getFilterParameters(filter['params'] ?? {});
    filterParams['schema'] = schema;
    if (filterFunc is Function) {
      data = await filterFunc(data, filterParams);
    } else {
      data = await runJSFilter(data);
    }
  }

  await sinkFunc(sinkParams, data);
  return data;
}

Future<Function> getSourceFunction(String name) async {
  Function source = sources[name];

  return source;
}

Future<dynamic> getFilterFunction(String name) async {
  if (name.startsWith('http://') || name.startsWith('https://')) {
    String filterStr = await readURL(name);

    jsRuntime.evaluate("""var get = function() {$filterStr}; var filter = get();""");
    return filterStr;

  } else {
    return filters[name];
  }
}

Future<Function> getSinkFunction(String name) async {
  Function sink = sinks[name];  

  return sink;
}

Future<String> getSchema(String path) async {
  String schema = await readURLOrFile(path);
  jsRuntime.evaluate("""var schema = $schema;""");
  return schema;
}

Future<Map> getFilterParameters(Map params) async {
  jsRuntime.evaluate("""var params = {};""");
  
  if (params['validator'] == 'json') {
    // some validator
  }

  if (params.containsKey('library')) {
    String library = await readURLOrFile(params['library']);
    params['library'] = library;
    jsRuntime.evaluate("""var get = function() {$library}; params.library = get();""");
  }

  return params;
}

Future<dynamic> transmogrifyEntry(Map entry, String schemaPath) async {
  Map source = entry['source'];
  List filters = entry['filters'];
  Map sink = entry['sink'] ?? {'func': 'null', 'params': {}};

  String schema = await getSchema(schemaPath);
  Function sourceFunc = await getSourceFunction(source['func']);
  Function sinkFunc = await getSinkFunction(sink['func']);

  return runPipelineEntry(sourceFunc, source["params"], filters, sinkFunc, sink['params'], schema);
}

Future<dynamic> transmogrifySchemaEntry(List data, Map schemaEntry) async {
  List filters = schemaEntry['filters'] ?? [];
  Map sink = schemaEntry['sink'] ?? {'func': 'null', 'params': {}};

  String schema = await getSchema(schemaEntry['schema']);
  Function sinkFunc = await getSinkFunction(sink['func']);

  return runPipelineSchemaEntry(data, filters, sinkFunc, sink['params'], schema);
}

Future<List> transmogrify(List manifest) async {
  List schemaEntryDatas = [];
  for (Map schemaEntry in manifest) {
    List entryDatas = [];
    for (Map entry in schemaEntry['entries']) {
      dynamic entryData = await transmogrifyEntry(entry, schemaEntry['schema']);
      entryDatas.add(entryData);
    }
    dynamic schemaEntryData = await transmogrifySchemaEntry(entryDatas, schemaEntry);
    schemaEntryDatas.add(schemaEntryData);
  }

  return schemaEntryDatas;
}

Future<List> main() async {
  // File manifest_file = File("assets\\van-texas-manifest.json");
  String manifest_string = await rootBundle.loadString("assets/van-texas-manifest.json");
  // print('Manifest: $manifest_string');
  // String manifest_string = await manifest_file.readAsString();
  List manifest = jsonDecode(manifest_string);

  List data = await transmogrify(manifest);
  return data;
}
