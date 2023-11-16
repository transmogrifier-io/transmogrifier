class Transmogrifier {
    schemaEntries = []
    constructor(manifest) {
        this.manifest = manifest;
        this.schemaEntries = this.getSchemaEntries();
    }
    getSchemaEntries(){
        let schemaList = [];
        for (const schema of this.manifest){
            let schemaEntry = new SchemaEntry(schema.schema, schema.entries, schema.filters, schema.sinks);
            schemaList.push(schemaEntry);
        }
        return schemaList;
    }
    async transmogrify() {
        const schemaEntryDatas = [];
        this.schemaEntries.forEach(async (schemaEntry)=> {
            schemaEntry.transmogrifyEntry();
            schemaEntryDatas.push(await schemaEntry.runPipelineSchemaEntry());
        });
        // for (let schemaEntry in this.schemaEntries){
        //     console.log(schemaEntry)
        //     schemaEntry.transmogrifyEntry();
        //     schemaEntryDatas.push(await schemaEntry.runPipelineSchemaEntry());
        // }
        return schemaEntryDatas;
    }
}

class SchemaEntry{
    constructor(schema, entries, filters, sinks){
        this.schema = schema ?? "";
        this.entries = this.entriesToObj(entries);
        this.filters = filters;
        this.sinks = sinks;
    }

    entriesToObj(data){
        let entries = [];
        for (const entry of data){
            entries.push(new Entry(entry.source, entry.filters, entry.sinks, this.schema));
        }
        return entries;
    }

    async transmogrifyEntry(){
        let entryData = [];
        for (const entry of this.entries){
            entryData.push(await entry.runPipelineEntry());
        }
        console.log(entryData)
        this.transmogrifiedEntries = entryData;
    }

    async runPipelineSchemaEntry() {
        let data = null;
        for (const filter of this.filters) {
            const filterFunc = await HelperFunctions.getFilterFunction(filter.func);
            const filterParams = await HelperFunctions.getFilterParameters(filter.params ?? {});
            filterParams.schema = this.schema;
            // console.log(this.transmogrifiedEntries)
            data = await filterFunc(this.transmogrifiedEntries, filterParams);
        }
    
        for (const sink of this.sinks) {
            const sinkFunc = await HelperFunctions.getSinkFunction(sink.func);
            const sinkParams = sink.params ?? {};
            await sinkFunc(sinkParams, data);
        }
        return data;
    }
}

class Entry{
    constructor(source, filters, sinks, schema){
        this.source = source;
        this.sourceParams = this.source.params;
        this.filters = filters;
        this.sinks = sinks;
        this.schema = schema;
    }

    // async runPipelineEntry() {
    //     console.log("running pipeline entry")
    //     // let sourceFunc = await HelperFunctions.getSourceFunction(this.source.func);
    //     let data =await HelperFunctions.getSourceFunction(this.source.func).then(
    //         (value)=>{value(this.sourceParams).then(
    //             (result)=>{
    //                 data = result
    //                 return result
    //     })});
    //     console.log(data)
    //     // let data = await sourceFunc(this.sourceParams);
    //     for (const filter of this.filters) {
    //         await HelperFunctions.getFilterFunction(filter.func).then(
    //             (filterFunc)=>{
    //                 HelperFunctions.getFilterParameters(filter.params ?? {}).then(
    //                     (filterParams)=>{
    //                         filterParams.schema = this.schema;
    //                         console.log(data)
    //                         data = filterFunc(data, filterParams);
    //                         console.log(data)
    //                     })
    //             }
    //             );
    //         const filterFunc = await HelperFunctions.getFilterFunction(filter.func);
    //         const filterParams = await HelperFunctions.getFilterParameters(filter.params ?? {});
    //         filterParams.schema = schema;
    //         filtered_data = await filterFunc(data, filterParams);
    //     }
    
    //     for (const sink of sinks) {
    //         const sinkFunc = await HelperFunctions.getSinkFunction(sink.func);
    //         const sinkParams = sink.params ?? {};
    //         await sinkFunc(sinkParams, data);
    //     }
    //     return data;
    // }
    async runPipelineEntry() {
        console.log("running pipeline entry");
    
        let data = await HelperFunctions.getSourceFunction(this.source.func)
            .then((sourceFunc) => {
                return sourceFunc(this.sourceParams)
            })
            .then((result) => {
                console.log("Result:")
                console.log(result);
                return result;
            })
        console.log("Data:", data);
        for (const filter of this.filters) {
            await HelperFunctions.getFilterFunction(filter.func)
                .then((filterFunc) => HelperFunctions.getFilterParameters(filter.params ?? {})
                    .then((filterParams) => {
                        filterParams.schema = this.schema;
                        console.log(data);
                        data = filterFunc(data, filterParams);
                        console.log(data);
                    }));
        }
    
        for (const sink of this.sinks) { // assuming sinks is an array property of the class
            await HelperFunctions.getSinkFunction(sink.func)
                .then((sinkFunc) => {
                    const sinkParams = sink.params ?? {};
                    return sinkFunc(sinkParams, data);
                });
        }
    
        return data;
    }
}

class HelperFunctions{
    static async getSourceFunction(name) {
        let source;
    
        if (name.startsWith("http://") || name.startsWith("https://")) {
            source = await Reader.readURL(name);
            source = new Function(source)();
        } else {
            source = sources[name];
        }
        
        return source;
    }
    
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
    
    static async getSchema(path) {
        if (!path) {
            return "";
        }
        const schema = await Reader.readURLOrFile(path);
        return schema;
    }
    
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
    // Platform-specific functions for reading local files

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

    // Function for reading a URL
    static async readURL(url) {
        let data;
        const http = require("http");
        const https = require("https");

        const httpModule = url.startsWith("https://") ? https : http;

        data = await new Promise((resolve, reject) => {
            httpModule
                .get(url, (res) => {
                    if (res.statusCode !== 200) {
                        reject(new Error(`Failed to read URL \"${url}\": HTTP status code ${res.statusCode}`));
                        return;
                    }

                    let rawData = "";
                    res.on("data", (chunk) => {
                        rawData += chunk;
                    });
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

    async readURLOrFile(path) {
        let data;
        if (path.startsWith("http://") || path.startsWith("https://")) {
            data = await readURL(path);
        } else {
            data = await readFile(path);
        }
        return data;
    }
}

class Writer {
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
            await writeLocalFileNode(filePath, append, data);
        } else if (typeof XMLHttpRequest !== "undefined") {
            // Running in a web browser
            await writeLocalFileWeb(filePath, append, data);
        } else if (typeof android !== "undefined") {
            // Running in an Android app
            await writeLocalFileAndroid(filePath, append, data);
        } else if (typeof window.webkit !== "undefined" && typeof window.webkit.messageHandlers.writeFile !== "undefined") {
            // Running in an iOS app
            await writeLocalFileiOS(filePath, append, data);
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

// Platform-specific functions for listing files in a directory
// Node.js
function listFilesNode(dirPath, extension) {
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
function listFilesWeb(dirPath, extension) {
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
function listFilesAndroid(dirPath, extension) {
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
function listFilesiOS(dirPath, extension) {
    const fileList = window.webkit.messageHandlers.listFiles.postMessage({ path: dirPath, extension: extension });
    return Promise.resolve(fileList);
}

// Promisified versions of the platform-specific functions

// Node.js
function listFilesNodeAsync(dirPath, extension) {
    return listFilesNode(dirPath, extension);
}

// Web browser
function listFilesWebAsync(dirPath, extension) {
    return listFilesWeb(dirPath, extension);
}

// Android
function listFilesAndroidAsync(dirPath, extension) {
    return listFilesAndroid(dirPath, extension);
}

// iOS
function listFilesiOSAsync(dirPath, extension) {
    return listFilesiOS(dirPath, extension);
}

// Function for listing files

async function listFiles(dirPath, extension) {
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
        return await writeFile(params, data);
    },
    url_write: async function (params, data) {
        return await writeURL(params, data);
    },
};

// async function transmogrifyEntry(entry, schema_path) {
//     const source = entry.source;
//     const filters = entry.filters;
//     const sinks = entry.sinks ?? [];

//     const schema = await getSchema(schema_path);
//     const sourceFunc = await getSourceFunction(source.func);

//     return runPipelineEntry(sourceFunc, source.params, filters, sinks, schema);
// }

// async function transmogrifySchemaEntry(data, schemaEntry) {
//     const filters = schemaEntry.filters ?? [];
//     const sinks = schemaEntry.sinks ?? [];

//     const schema = await getSchema(schemaEntry.schema);

//     return runPipelineSchemaEntry(data, filters, sinks, schema);
// }



module.exports = {
    Transmogrifier,
    
};
