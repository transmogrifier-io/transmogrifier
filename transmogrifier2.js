const { error } = require("console");

class Transmogrifier {
    /**
     * Class represents a Transmogrifier object
     * @param {Array} manifest - Array of objects containing schema, entries, filters, and sinks
     */
    constructor(manifest) {
        this.manifest = manifest;
        this.schemaEntries = this.getSchemaEntries();
    }

    /**
     * Function creates SchemaEntry objects from manifest
     * @returns {Array} - Array of SchemaEntry objects
     */
    getSchemaEntries() {
        let schemaList = [];
        for (const schema of this.manifest) {
            let schemaEntry = new SchemaEntry(schema.schema, schema.entries, schema.filters, schema.sinks);
            schemaList.push(schemaEntry);
        }
        return schemaList;
    }

    /**
     * Function transmogrifies and runs the pipeline for each SchemaEntry in the Schema
     * @returns {Array} - Array of data from each SchemaEntry
     */
    async transmogrify() {
        const schemaEntryDatas = [];
        for (const schemaEntry of this.schemaEntries) {
            await schemaEntry.transmogrifyEntry();
            schemaEntryDatas.push(await schemaEntry.runPipelineSchemaEntry());
        }

        return schemaEntryDatas;
    }
}

class SchemaEntry {
    /**
     * Class represents a Schema Entry Object
     * @param {string} schema - Path to schema file
     * @param {object} entries - Array of objects containing entries
     * @param {object} filters - Array of objects containing filters
     * @param {object} sinks - Array of objects containing sinks
     */
    constructor(schema, entries, filters, sinks) {
        this.schema = schema ?? "";
        this.entries = this.entriesToObj(entries);
        this.filters = filters;
        this.sinks = sinks;
        this.transmogrifiedEntries;
    }

    /**
     * Function creates Entry objects 
     * @param {object} data - Array of objects containing entries
     * @returns {Array} - Array of Entry objects
     */
    entriesToObj(data) {
        let entries = [];
        for (const entry of data) {
            entries.push(new Entry(entry.source, entry.filters, entry.sinks));
        }
        return entries;
    }
    /**
     * Function transmogrifies each entry in the SchemaEntry and stores in this.transmogrifiedEntries
     */
    async transmogrifyEntry() {
        try {
            let entryData = [];
            for (const entry of this.entries) {
                entryData.push(await entry.runPipelineEntry());
            }
            this.transmogrifiedEntries = entryData;
        } catch (error) {
            console.error("Error in transmogrifyEntry:", error);
            throw error;
        }
    }

    /**
     * Function runs data through the filters and sinks and returns the processed data
     * @returns {Array} - Array of processed data from a SchemaEntry
     */
    async runPipelineSchemaEntry() {
        let data = this.transmogrifiedEntries;
        for (const filter of this.filters) {
            const filterFunc = await HelperFunctions.getFilterFunction(filter.func);
            const filterParams = await HelperFunctions.getFilterParameters(filter.params ?? {});
            filterParams.schema = await HelperFunctions.getSchema(this.schema);
            data = await filterFunc(data, filterParams);
        }
        for (const sink of this.sinks) {
            const sinkFunc = await HelperFunctions.getSinkFunction(sink.func);
            const sinkParams = sink.params ?? {};
            await sinkFunc(sinkParams, data);
        }
        return data;
    }
}

class Entry {
    /**
     * Class represents an Entry Object
     * @param {object} source - Object containing source of data
     * @param {object} filters - Array of objects containing filters
     * @param {object} sinks - Array of objects containing sinks
     */
    constructor(source, filters, sinks) {
        this.source = source;
        this.sourceParams = this.source.params;
        this.filters = filters;
        this.sinks = sinks;
    }

    /**
     * Function gets the data from the source and runs it through the filters and sinks
     * @returns {Array} - Array of processed data 
     */
    async runPipelineEntry() {
        let data;
        try {
            data = await HelperFunctions.getSourceFunction(this.source.func)
                .then((sourceFunc) => sourceFunc(this.sourceParams));

            for (const filter of this.filters) {

                const filterFunc = await HelperFunctions.getFilterFunction(filter.func);
                const filterParams = await HelperFunctions.getFilterParameters(filter.params ?? {});
                data = await filterFunc(data, filterParams);
            }

            if (this.sinks) {
                for (const sink of this.sinks) {
                    const sinkFunc = await HelperFunctions.getSinkFunction(sink.func);
                    const sinkParams = sink.params ?? {};
                    await sinkFunc(sinkParams, data);
                }

            }
            return data;
        } catch (error) {
            console.error("Error in runPipelineEntry:", error);
            throw error;
        }
    }
}

class HelperFunctions {
    /**
     * Function returns the source function
     * @param {string} name - Name or URL of the function to get
     * @returns the source function
     */
    static async getSourceFunction(name) {
        let source;
        try {
            if (name.startsWith("http://") || name.startsWith("https://")) {
                source = await Reader.readURL(name);
                source = new Function(source)();
            } else {
                source = sources[name];
            }
        } catch (error) {
            console.log(error)
        }
        return source;
    }

    /**
     * Function returns the filter function
     * @param {string} name - Name or URL of the function to get
     * @returns the filter function
     */
    static async getFilterFunction(name) {
        let filter;

        if (name.startsWith("http://") || name.startsWith("https://")) {
            filter = await Reader.readURL(name);
            filter = new Function(filter)();

        } else {
            filter = filters[name];
        }

        return filter;
    }

    /**
     * Function returns the sink function
     * @param {string} name - Name or URL of the function to get
     * @returns the sink function
     */
    static async getSinkFunction(name) {
        let sink;

        if (name.startsWith("http://") || name.startsWith("https://")) {
            sink = await Reader.readURL(name);
            sink = new Function(sink)();
        } else {
            sink = sinks[name];
        }

        return sink;
    }

    /**
     * Function returns the schema
     * @param {string} path - URL to schema file
     * @returns schema as a string
     */
    static async getSchema(path) {
        if (!path) {
            return "";
        }
        const schema = await Reader.readURLOrFile(path);
        return schema;
    }

    /**
     * Function returns the filter parameters
     * @param {object} params 
     * @returns params object
     */
    static async getFilterParameters(params) {
        if (params["validator"] === "json") {
            const validator = require("jsonschema").Validator;
            params["jsonschema"] = new validator();
        }

        if (params["library"]) {
            let library = await Reader.readURLOrFile(params["library"]);
            params["library"] = new Function(library)();
        }
        return params;
    }
}

class Reader {
    // Platform-specific functions for reading a file

    // Node.js
    static readLocalFileNode(filePath) {
        const fs = require("fs");
        return new Promise(function (resolve, reject) {
            fs.readFile(filePath, "utf8", function (err, data) {
                if (err) {
                    reject(err);
                } else {
                    resolve(data);
                }
            });
        });
    }
    // Web browser
    static readLocalFileWeb(filePath) {
        const xhr = new XMLHttpRequest();
        return new Promise(function (resolve, reject) {
            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        resolve(xhr.responseText);
                    } else {
                        reject(new Error("Error reading file"));
                    }
                }
            };
            xhr.open("GET", filePath);
            xhr.send();
        });
    }

    // Android
    static readLocalFileAndroid(filePath) {
        const scheme = "content://";
        const uri = android.net.Uri.parse(scheme + filePath);
        const stream = context.getContentResolver().openInputStream(uri);
        const reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
        let data = "";
        let line;
        while ((line = reader.readLine())) {
            data += line + "\n";
        }
        return Promise.resolve(data);
    }

    // iOS
    static readLocalFileiOS(filePath) {
        const data = window.webkit.messageHandlers.readFile.postMessage(filePath);
        return Promise.resolve(data);
    }


    // Function for reading a file
    static async readFile(filePath) {
        let data;
        if (typeof window === "undefined") {
            // Running in Node.js
            data = await readLocalFileNode(filePath);
        } else if (typeof XMLHttpRequest !== "undefined") {
            // Running in a web browser
            data = await readLocalFileWeb(filePath);
        } else if (typeof android !== "undefined") {
            // Running in an Android app
            data = await readLocalFileAndroid(filePath);
        } else if (typeof window.webkit !== "undefined" && typeof window.webkit.messageHandlers.readFile !== "undefined") {
            // Running in an iOS app
            data = await readLocalFileiOS(filePath);
        } else {
            throw new Error("Environment not supported");
        }
        return data;
    }

    /**
     * Function for reading a URL
     * @param {string} url - URL to read
     * @returns data from URL
     */
    static async readURL(url) {
        let data;
        const http = require("http");
        const https = require("https");

        const httpModule = url.startsWith("https://") ? https : http;

        data = await new Promise((resolve, reject) => {
            httpModule.get(url, (res) => {
                if (res.statusCode !== 200) {
                    reject(new Error(`Failed to read URL \"${url}\": HTTP status code ${res.statusCode}`));
                    return;
                }

                let rawData = "";
                res.on("data", (chunk) => { rawData += chunk; });
                res.on("end", () => {
                    try {
                        resolve(rawData);
                    } catch (e) {
                        reject(e);
                    }
                });
            })
                .on("error", (e) => {
                    reject(e);
                });
        });
        return data;
    }

    /**
     * Function to return data from a URL or file path
     * @param {string} path - URL or file path
     * @returns data as string
     */
    static async readURLOrFile(path) {
        let data;
        if (path.startsWith("http://") || path.startsWith("https://")) {
            data = await Reader.readURL(path);
        } else {
            data = await Reader.readFile(path);
        }
        return data;
    }
}

class Writer {
    // Platform-specific functions for writing a file

    // Node.js
    static writeLocalFileNode(filePath, append, data) {
        const fs = require("fs");
        let writeFunc;
        if (append) {
            data = "\n" + data;
            writeFunc = fs.appendFile;
        } else {
            writeFunc = fs.writeFile;
        }

        return new Promise(function (resolve, reject) {
            writeFunc(filePath, data, "utf8", function (err) {
                if (err) {
                    reject(err);
                } else {
                    resolve();
                }
            });
        });
    }

    // Web browser
    static writeLocalFileWeb(filePath, append, data) {
        const xhr = new XMLHttpRequest();
        return new Promise(function (resolve, reject) {
            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        resolve();
                    } else {
                        reject(new Error("Error writing file"));
                    }
                }
            };
            xhr.open("PUT", filePath);
            xhr.setRequestHeader("Content-Type", "text/plain");
            xhr.send(data);
        });
    }

    // Android
    static writeLocalFileAndroid(filePath, append, data) {
        const scheme = "content://";
        const uri = android.net.Uri.parse(scheme + filePath);
        const outputStream = context.getContentResolver().openOutputStream(uri);
        const writer = new java.io.OutputStreamWriter(outputStream);
        writer.write(data);
        writer.close();
        return Promise.resolve();
    }

    // iOS
    static writeLocalFileiOS(filePath, append, data) {
        window.webkit.messageHandlers.writeFile.postMessage({ path: filePath, data: data });
        return Promise.resolve();
    }


    // Function for writing a file
    static async writeFile(params, data) {
        let filePath;
        if (params.path) {
            filePath = params.path;
        }
        let append = false;
        if (params.append) {
            append = params.append;
        }
        if (typeof window === "undefined") {
            // Running in Node.js
            await Writer.writeLocalFileNode(filePath, append, data);
        } else if (typeof XMLHttpRequest !== "undefined") {
            // Running in a web browser
            await Writer.writeLocalFileWeb(filePath, append, data);
        } else if (typeof android !== "undefined") {
            // Running in an Android app
            await Writer.writeLocalFileAndroid(filePath, append, data);
        } else if (typeof window.webkit !== "undefined" && typeof window.webkit.messageHandlers.writeFile !== "undefined") {
            // Running in an iOS app
            await Writer.writeLocalFileiOS(filePath, append, data);
        } else {
            throw new Error("Environment not supported");
        }
    }

    // Function for writing to a URL

    static async writeURL(url, data) {
        let result;
        const http = require("http");
        const https = require("https");
        const newUrl = new URL(url);

        //specifies that it is a POST request
        const options = {
            hostname: newUrl.hostname,
            path: newUrl.pathname,
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Content-Length": data.length,
            },
        };

        const httpModule = url.startsWith("https://") ? https : http;

        result = await new Promise((resolve, reject) => {
            httpModule
                .request(options, (res) => {
                    if (res.statusCode !== 200) {
                        reject(new Error(`Failed to post data \"${options.hostname + options.path}\": HTTP status code ${res.statusCode}`));
                        return;
                    }
                    res.on("data", (d) => {
                        resolve(d);
                    });
                })
                .on("error", (e) => {
                    reject(e);
                });
        });

        return result;
    }
    // Platform-specific functions for writing local files

    // Node.js
    // This works for ODEN-specific JSON files
    // async function writeLocalFileNode(filePath, append, data) {
    //     const fs = require('fs');
    //     // check if we need to append, and if the file exists
    //     if (append && fs.existsSync(filePath)) {
    //         await new Promise(function (resolve, reject) {
    //             fs.readFile(filePath, (err, existing_data) => {
    //                 let data_in_file = JSON.parse(existing_data);

    //                 // field is data or errors
    //                 for (let field in data_in_file) {
    //                     let parsed_data = JSON.parse(data)
    //                     for (let entry of parsed_data[field]) {
    //                         let entry_append = true;
    //                         for (existing_entry of data_in_file[field]) {
    //                             if (JSON.stringify(entry) == JSON.stringify(existing_entry)) {
    //                                 entry_append = false;
    //                             }
    //                         }
    //                         if (entry_append) {
    //                             data_in_file[field].push(entry);
    //                         }
    //                     }

    //                 }
    //                 data = JSON.stringify(data_in_file, null, 2);
    //                 resolve();
    //             });
    //         });
    //     }
    //     // write to file
    //     return new Promise(function (resolve, reject) {
    //         fs.writeFile(filePath, data, 'utf8', function (err) {
    //             if (err) {
    //                 reject(err);
    //             }
    //             else {
    //                 resolve();
    //             }
    //         });
    //     });
    // }

}

class Lister {
    // Platform-specific functions for listing files in a directory
    // Node.js
    listFilesNode(dirPath, extension) {
        const fs = require("fs");
        return new Promise(function (resolve, reject) {
            fs.readdir(dirPath, function (err, files) {
                if (err) {
                    reject(err);
                } else {
                    const filteredFiles = files.filter(function (file) {
                        return file.endsWith(extension);
                    });
                    resolve(filteredFiles);
                }
            });
        });
    }

    // Web browser
    listFilesWeb(dirPath, extension) {
        return new Promise(function (resolve, reject) {
            const xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        const fileList = xhr.responseText.split("\n");
                        const filteredFiles = fileList.filter(function (file) {
                            return file.endsWith(extension);
                        });
                        resolve(filteredFiles);
                    } else {
                        reject(new Error("Error listing files"));
                    }
                }
            };
            xhr.open("GET", dirPath);
            xhr.send();
        });
    }

    // Android
    listFilesAndroid(dirPath, extension) {
        const scheme = "content://";
        const uri = android.net.Uri.parse(scheme + dirPath);
        const contentResolver = context.getContentResolver();
        const selection = android.provider.MediaStore.Files.FileColumns.DATA + " like ?";
        const selectionArgs = ["%" + extension];
        const projection = [android.provider.MediaStore.Files.FileColumns.DATA];
        const cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        const fileList = [];
        while (cursor.moveToNext()) {
            const filePath = cursor.getString(0);
            fileList.push(filePath);
        }
        cursor.close();
        return Promise.resolve(fileList);
    }

    // iOS
    listFilesiOS(dirPath, extension) {
        const fileList = window.webkit.messageHandlers.listFiles.postMessage({ path: dirPath, extension: extension });
        return Promise.resolve(fileList);
    }

    // Promisified versions of the platform-specific functions

    // Node.js
    listFilesNodeAsync(dirPath, extension) {
        return listFilesNode(dirPath, extension);
    }

    // Web browser
    listFilesWebAsync(dirPath, extension) {
        return listFilesWeb(dirPath, extension);
    }

    // Android
    listFilesAndroidAsync(dirPath, extension) {
        return listFilesAndroid(dirPath, extension);
    }

    // iOS
    listFilesiOSAsync(dirPath, extension) {
        return listFilesiOS(dirPath, extension);
    }

    // Function for listing files

    async listFiles(dirPath, extension) {
        let fileList;
        if (typeof window === "undefined") {
            // Running in Node.js
            fileList = await listFilesNodeAsync(dirPath, extension);
        } else if (typeof XMLHttpRequest !== "undefined") {
            // Running in a web browser
            fileList = await listFilesWebAsync(dirPath, extension);
        } else if (typeof android !== "undefined") {
            // Running in an Android app
            fileList = await listFilesAndroidAsync(dirPath, extension);
        } else if (typeof window.webkit !== "undefined" && typeof window.webkit.messageHandlers.listFiles !== "undefined") {
            // Running in an iOS app
            fileList = await listFilesiOSAsync(dirPath, extension);
        } else {
            throw new Error("Environment not supported");
        }
        return fileList;
    }
}


const sources = {
    file_read: async function (params) {
        return await Reader.readFile(params.path);
    },
    url_read: async function (params) {
        return await Reader.readURL(params.path);
    },
};

const filters = {
    null_filter: async function (data, params) {
        return data;
    },
    to_upper: async function (data, params) {
        return data.toUpperCase();
    },
    to_lower: async function (data, params) {
        return data.toLowerCase();
    },
};

const sinks = {
    null: async function (params, data) {
        return;
    },
    file_write: async function (params, data) {
        return await Writer.writeFile(params, data);
    },
    url_write: async function (params, data) {
        return await Writer.writeURL(params, data);
    },
};



module.exports = {
    Transmogrifier,
};
