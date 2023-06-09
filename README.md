# Transmogrifier
The Transmogrifier is a Data Processing Pipeline, which allows for conformation of external data to standardized schemae. It can take data from multiple sources, process it through a series of dataset-specific and broad-level filters, then output the transformed data to any number of sinks (individual or grouped datasets).

The filters can be highly customized to fit any given data standard or requirement and the sinks can be configured to output to a file, upload to a database, or write to a URL. 


# Purpose
Transmogrifier is a tool that automates the process of data extraction, transformation, and loading. The Transmogrifier supports extracting data from diverse sources, allows for multiple transformations, and facilitates loading the transformed data to various target destinations.

# Manifest
The manifest is the JSON file that defines the standardized schemae, data sources (datasets), filters, and sink(s). At the top level, it is an array of objects. Each object contains the following properties:
- a schema
- a top-level array of filters
- a top-level array of sinks (optional)
- an array of entries

Within each entry, each object contains the following properties:
- a source
- an array of filters
- an array of sinks (optional)

## Features
The Transmogrifier pipeline is outlined as the following: 
- schema
- filters (broad-level) 
- sinks
- entries

The functions `func` can either be referenced as built-in functions (from `transmogrifier.js`) or a URL to an external function. (**NOTE**: The Dart implementation does not currently support external functions).

The source built-in functions include (but might not be limited to) the following:
- `url_read` - reads data from a URL. Accepts the URL in the params.path attribute.
- `file_read` - reads data from a file. Accepts the file path in the params.path attribute.

The sink built-in functions include (but might not be limited to) the following:
- `url_write` - writes data to a URL. Accepts the URL in the params.path attribute.
- `file_write` - writes data to a file. Accepts the file path in the params.path attribute.

### Filters (broad-level)
These filters are broad-level filters that will be applied to all of the modified data that comes out of the dataset-specific filters.

The `filters` array defines multiple objects that contain the filter needed to transform the data as well as any parameters required. For example, the filter "public_art_json_to_json" requires a library parameter to define the location of the library of functions needed to transform the data.

The order that the filters are defined is the order the the data will pass through. For example, in the manifest below, the validator filter is run before the stringify filter.

[Here] (https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/validate.js) is an example of a filter.

### Sinks (broad-level)
The `sinks` array defines multiple objects that contain the function needed to output the data as well as any parameters required. For example, the function "file_write" requires a path parameter to define the location of the output file.

The broad-level sinks output all of the data into final destinations.


### Entries
An entry is an object that defines the `source` to read data from, the `filters` to apply to the data and the `sinks` that define where to write the filtered data for each particular dataset. 
- Sources

   The `source` object defines the function needed to get the data as well as any parameters required. For example, the function "url_read" requires a path parameter to define the location of the data.
- Filters (dataset-specific)

   These filters are specific to the `source` data you will be transforming. For the overall details, you can see the section [above](#Filters).
- Sinks (dataset-specific)
  

   See section [above](#Sinks).


## How to Write a Manifest
Below is an example of how a manifest should be written.
### Example
```json
[
  {
    "schema": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/schemas/public-art.json",
    "filters": [
      {
        "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/ms-ss-rg-collector-update/collectors/collector-json.js"
      },
      {
        "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/validate.js",
        "params": {
          "validator": "json"
        }
      },
      {
        "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/stringify.js",
        "params": {
          "indent": true
        }
      }
    ],
    "sinks": [
      {
        "func": "file_write",
        "params": {
          "path": "./all-my-data-1.json"
        }
      },
      {
        "func": "file_write",
        "params": {
          "path": "./all-my-data-2.json"
        }
      }
    ],
    "entries": [
      {
        "source": {
          "func": "url_read",
          "params": {
            "path": "https://opendata.vancouver.ca/api/explore/v2.1/catalog/datasets/public-art/exports/json?lang=en&timezone=America%2FLos_Angeles"
          }
        },
        "filters": [
          {
            "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/canada/british-columbia/vancouver/public-art-json-to-json.js",
            "params": {
              "library": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/libraries/standard.js"
            }
          },
          {
            "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/stringify.js",
            "params": {
              "indent": true
            }
          }
        ],
        "sinks": [
          {
            "func": "file_write",
            "params": {
              "path": "./my-vancouver-data-1.json"
            }
          },
          {
            "func": "file_write",
            "params": {
              "path": "./my-vancouver-data-2.json"
            }
          }
        ]
      },
      {
        "source": {
          "func": "url_read",
          "params": {
            "path": "https://data.austintexas.gov/resource/uuk6-933w.json"
          }
        },
        "filters": [
          {
            "func": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/filters/united-states/texas/austin/public-art-json-to-json.js",
            "params": {
              "library": "https://raw.githubusercontent.com/OpendataDeveloperNetwork/ODEN-Transmogrifiers/dev/libraries/standard.js"
            }
          }
        ]
      }
    ]
  }
]
```

### How to Run a Manifest with the Transmogrifier

To run the transmogrifier, you need an executor file, such as this [example](https://github.com/transmogrifier-io/transmogrifier/blob/dev/example-main.js). 
This file simply reads and parses the manifest and passes it to the transmogrify function defined in `transmogrifier.js`.
The example transmogrifier takes the manifest as a command line argument, so to initiate execution with node, for example, you will run the following command:
(assuming you are in the same directory as the example-main.js) 
`node example-main.js my_manifest.json` 

# Implementations
This repository includes implementations for the transmogrifier in both Javascript and Dart.

# Limitations
The following sections cover any limitations that the different transmogrifier implemenations may have.

## Javascript implementation limitations
The Javascript implementation uses node.js for some of its functionality, applications that are developed using flutter will
not be able to use this implementation. The Dart implementation should be used if this is the case.

## Dart implementation limitations
The Dart implementation currently only supports builtin source/sink functions. 
If performing data validation, this [file] (https://github.com/abner/flutter_js/blob/master/example/assets/js/ajv.js) should be included in the assets folder of the flutter project.


