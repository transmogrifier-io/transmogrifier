# Transmogrifier
The Transmogrifier is a Data Processing Pipeline, which takes data from multiple sources, processes it through a series of filters, then outputs to any number of sinks. The output can then be grouped and filtered as a whole 
The Transmogrifier is a Data Processing Pipeline, which takes data from multiple sources, processes it through a series of filters, and outputs it to any number of sinks. The filters can be highly customized to fit any given data standard or requirement and the sinks can be configured to output to a file or upload to a database. 

The Transmogrifier is a Data Processing Pipeline, which allows for conformation of external data to standardized schemae. It can take data from multiple sources, process it through a series of data source-specific and broad-level filters, then output the transformed data to any number of sinks (individual or grouped datasets).

The filters can be highly customized to fit any given data standard or requirement and the sinks can be configured to output to a file or upload to a database. 


# Purpose
Transmogrifier is a tool that automates the process of data extraction, transformation, and loading. The Transmogrifier supports extracting data from diverse sources, allows for multiple transformations, and facilitates loading the transformed data to various target destinations.

# Manifest
The manifest is the JSON file that defines the standardized schemae, data sources, filters, and sink(s). 

## Features
The Transmogrifier pipeline is outlined as the following: 
+schema
+filters (broad-level)
+sinks
### Sources
The Source object defines the function needed to get the data as well as any parameters required. For example, the function "url_read" requires a url parameter to define the location of the data.

### Filters
The Filters array defines multiple objects that contain the function needed to transform the data as well as any parameters required. For example, the function "public_art_json_to_json" requires a library parameter to define the location of the library of functions needed to transform the data.

### Sinks
The Sinks array defines multiple objects that contain the function needed to output the data as well as any parameters required. For example, the function "file_write" requires a path parameter to define the location of the output file.


## How to Write a Manifest

## Example
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

# Limitations