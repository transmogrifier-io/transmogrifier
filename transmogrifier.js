// Platform-specific functions for reading local files

const { rejects } = require('assert');
const { resolve } = require('path');

// Node.js
function readLocalFileNode(filePath) {
    const fs = require('fs');
    return new Promise(function (resolve, reject) {
        fs.readFile(filePath, 'utf8', function (err, data) {
            if (err) {
                reject(err);
            }
            else {
                resolve(data);
            }
        });
    });
}

// Web browser
function readLocalFileWeb(filePath) {
    const xhr = new XMLHttpRequest();
    return new Promise(function (resolve, reject) {
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    resolve(xhr.responseText);
                }
                else {
                    reject(new Error('Error reading file'));
                }
            }
        };
        xhr.open('GET', filePath);
        xhr.send();
    });
}

// Android
function readLocalFileAndroid(filePath) {
    const scheme = 'content://';
    const uri = android.net.Uri.parse(scheme + filePath);
    const stream = context.getContentResolver().openInputStream(uri);
    const reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
    let data = '';
    let line;
    while ((line = reader.readLine())) {
        data += line + '\n';
    }
    return Promise.resolve(data);
}

// iOS
function readLocalFileiOS(filePath) {
    const data = window.webkit.messageHandlers.readFile.postMessage(filePath);
    return Promise.resolve(data);
}

// Promisified versions of the platform-specific functions

// Node.js
function readLocalFileNodeAsync(filePath) {
    return readLocalFileNode(filePath);
}

// Web browser
function readLocalFileWebAsync(filePath) {
    return readLocalFileWeb(filePath);
}

// Android
function readLocalFileAndroidAsync(filePath) {
    return readLocalFileAndroid(filePath);
}

// iOS
function readLocalFileiOSAsync(filePath) {
    return readLocalFileiOS(filePath);
}

// Function for reading a file

async function readFile(filePath) {
    let data;
    if (typeof window === 'undefined') {
        // Running in Node.js
        data = await readLocalFileNodeAsync(filePath);
    } else if (typeof XMLHttpRequest !== 'undefined') {
        // Running in a web browser
        data = await readLocalFileWebAsync(filePath);
    } else if (typeof android !== 'undefined') {
        // Running in an Android app
        data = await readLocalFileAndroidAsync(filePath);
    } else if (typeof window.webkit !== 'undefined' && typeof window.webkit.messageHandlers.readFile !== 'undefined') {
        // Running in an iOS app
        data = await readLocalFileiOSAsync(filePath);
    } else {
        throw new Error('Environment not supported');
    }
    return data;
}

// Function for reading a URL

async function readURL(url) {
    let data;
    const http = require('http');
    const https = require('https');

    const httpModule = url.startsWith("https://") ? https : http;

    data = await new Promise((resolve, reject) => {
        httpModule.get(url, (res) => {
            if (res.statusCode !== 200) {
                reject(new Error(`Failed to read URL \"${url}\": HTTP status code ${res.statusCode}`));
                return;
            }

            let rawData = '';
            res.on('data', (chunk) => { rawData += chunk; });
            res.on('end', () => {
                try {
                    resolve(rawData);
                }
                catch (e) {
                    reject(e);
                }
            });
        }).on('error', (e) => {
            reject(e);
        });
    })

    return data;
}

// Platform-specific functions for writing local files

// Node.js
function writeLocalFileNode(filePath, data) {
    const fs = require('fs');
    return new Promise(function (resolve, reject) {
        fs.writeFile(filePath, data, 'utf8', function (err) {
            if (err) {
                reject(err);
            }
            else {
                resolve();
            }
        });
    });
}

// Web browser
function writeLocalFileWeb(filePath, data) {
    const xhr = new XMLHttpRequest();
    return new Promise(function (resolve, reject) {
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    resolve();
                }
                else {
                    reject(new Error('Error writing file'));
                }
            }
        };
        xhr.open('PUT', filePath);
        xhr.setRequestHeader('Content-Type', 'text/plain');
        xhr.send(data);
    });
}

// Android
function writeLocalFileAndroid(filePath, data) {
    const scheme = 'content://';
    const uri = android.net.Uri.parse(scheme + filePath);
    const outputStream = context.getContentResolver().openOutputStream(uri);
    const writer = new java.io.OutputStreamWriter(outputStream);
    writer.write(data);
    writer.close();
    return Promise.resolve();
}

// iOS
function writeLocalFileiOS(filePath, data) {
    window.webkit.messageHandlers.writeFile.postMessage({ path: filePath, data: data });
    return Promise.resolve();
}

// Promisified versions of the platform-specific functions

// Node.js
function writeLocalFileNodeAsync(filePath, data) {
    return writeLocalFileNode(filePath, data);
}

// Web browser
function writeLocalFileWebAsync(filePath, data) {
    return writeLocalFileWeb(filePath, data);
}

// Android
function writeLocalFileAndroidAsync(filePath, data) {
    return writeLocalFileAndroid(filePath, data);
}

// iOS
function writeLocalFileiOSAsync(filePath, data) {
    return writeLocalFileiOS(filePath, data);
}

// Function for writing a file

async function writeFile(filePath, data) {
    if (typeof window === 'undefined') {
        // Running in Node.js
        await writeLocalFileNodeAsync(filePath, data);
    }
    else if (typeof XMLHttpRequest !== 'undefined') {
        // Running in a web browser
        await writeLocalFileWebAsync(filePath, data);
    }
    else if (typeof android !== 'undefined') {
        // Running in an Android app
        await writeLocalFileAndroidAsync(filePath, data);
    }
    else if (typeof window.webkit !== 'undefined' && typeof window.webkit.messageHandlers.writeFile !== 'undefined') {
        // Running in an iOS app
        await writeLocalFileiOSAsync(filePath, data);
    }
    else {
        throw new Error('Environment not supported');
    }
}

// Function for writing to a URL

async function writeURL(url, data) {
    // TODO
}

// Platform-specific functions for listing files in a directory

// Node.js
function listFilesNode(dirPath, extension) {
    const fs = require('fs');
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
                    const fileList = xhr.responseText.split('\n');
                    const filteredFiles = fileList.filter(function (file) {
                        return file.endsWith(extension);
                    });
                    resolve(filteredFiles);
                } else {
                    reject(new Error('Error listing files'));
                }
            }
        };
        xhr.open('GET', dirPath);
        xhr.send();
    });
}

// Android
function listFilesAndroid(dirPath, extension) {
    const scheme = 'content://';
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
    if (typeof window === 'undefined') {
        // Running in Node.js
        fileList = await listFilesNodeAsync(dirPath, extension);
    }
    else if (typeof XMLHttpRequest !== 'undefined') {
        // Running in a web browser
        fileList = await listFilesWebAsync(dirPath, extension);
    }
    else if (typeof android !== 'undefined') {
        // Running in an Android app
        fileList = await listFilesAndroidAsync(dirPath, extension);
    }
    else if (typeof window.webkit !== 'undefined' && typeof window.webkit.messageHandlers.listFiles !== 'undefined') {
        // Running in an iOS app
        fileList = await listFilesiOSAsync(dirPath, extension);
    }
    else {
        throw new Error('Environment not supported');
    }
    return fileList;
}

const sources =
{
    file_read: async function (params) {
        return await readFile(params.path);
    },
    url_read: async function (params) {
        return await readURL(params.path);
    }
};

const filters =
{
    null_filter: async function (data, params) {
        return data;
    },
    to_upper: async function (data, params) {
        return data.toUpperCase()
    },
    to_lower: async function (data, params) {
        return data.toLowerCase()
    }
};

const sinks =
{
    file_write: async function (params, data) {
        return await writeFile(params.path, data);
    },
    url_write: async function (params, data) {
        return await writeURL(params.path, data);
    }
};

async function runPipeline(sourceFunc, sourceParams, filters, sinkFunc, sinkParams, schema) {
    let data = await sourceFunc(sourceParams);

    for (const filter of filters) {
        const filterFunc = await getFilterFunction(filter.func);
        data = await filterFunc(data, filter.params);
    }

    await sinkFunc(sinkParams, data);
}

// Get the source function from the given parameter, if param is a URL, get the function from the URL,
// otherwise, get the function from the mapping
async function getSourceFunction(name) {
    let source;

    const http = require('http');
    const https = require('https');

    if (name.startsWith("http://") || name.startsWith("https://")) {
        const httpModule = name.startsWith("https://") ? https : http;

        source = await new Promise((resolve, reject) => {
            httpModule.get(name, (res) => {
                if (res.statusCode !== 200) {
                    reject(new Error(`Failed to read source ${name}: HTTP status code ${res.statusCode}`));
                    return
                }
                let rawData = '';
                res.on('data', (chunk) => {rawData += chunk});
                res.on('end', () => {
                    try {
                        resolve(rawData);
                    }
                    catch(e) {
                        reject(e);
                    }
                });
            }).on('error', (e) => {
                reject(e);
            });
        })
        source = new Function(source)();
    }
    else {
        source = sources[name];
    }

    return source
}

async function getFilterFunction(name) {
    let filter;

    const http = require('http');
    const https = require('https');

    if (name.startsWith("http://") || name.startsWith("https://")) {
        const httpModule = name.startsWith("https://") ? https : http;

        filter = await new Promise((resolve, reject) => {
            httpModule.get(name, (res) => {
                if (res.statusCode !== 200) {
                    reject(new Error(`Failed to read filter ${name}: HTTP status code ${res.statusCode}`));
                    return;
                }

                let rawData = '';
                res.on('data', (chunk) => { rawData += chunk; });
                res.on('end', () => {
                    try {
                        resolve(rawData);
                    } 
                    catch (e) {
                        reject(e);
                    }
                });
            }).on('error', (e) => {
                reject(e);
            });
        })
    }
    else {
        filter = filters[name];
    }


    // TODO if "name" is a URL, read from the URL instead (look at main loadManifest())
    // can parse the function the same way as we do filters (new Function(url.data)())

    return filter;
}

async function getSinkFunction(name) {
    // TODO if "name" is a URL, read from the URL instead (look at main loadManifest())
    // can parse the function the same way as we do filters (new Function(url.data)())


    let sink;

    const http = require('http');
    const https = require('https');

    if (name.startsWith("http://") || name.startsWith("https://")) {
        const httpModule = name.startsWith("https://") ? https : http;

        sink = await new Promise((resolve, reject) => {
            httpModule.get(name, (res) => {
                if (res.statusCode !== 200) {
                    reject(new Error(`Failed to read sink ${name}: HTTP status code ${res.statusCode}`));
                    return;
                }

                let rawData = '';
                res.on('data', (chunk) => { rawData += chunk; });
                res.on('end', () => {
                    try {
                        resolve(rawData);
                    } 
                    catch (e) {
                        reject(e);
                    }
                });
            }).on('error', (e) => {
                reject(e);
            });
        })
        sink = new Function(sink)();
    }
    else {
        sink = sinks[name];
    }

    return sink
}

async function getSchema(path) {
    let schema;
    const http = require('http');
    const https = require('https');
    
    if (path.startsWith("http://") || path.startsWith("https://")) {
        const httpModule = path.startsWith("https://") ? https : http;

        schema = await new Promise((resolve, reject) => {
            httpModule.get(path, (res) => {
                if (res.statusCode !== 200) {
                    reject(new Error(`Failed to read schema \"${path}\": HTTP status code ${res.statusCode}`));
                    return;
                }

                let rawData = '';
                res.on('data', (chunk) => { rawData += chunk; });
                res.on('end', () => {
                    try {
                        resolve(rawData);
                    }
                    catch (e) {
                        reject(e);
                    }
                });
            }).on('error', (e) => {
                reject(e);
            });
        })
    }
    else {
        schema = await readFile(path);
    }

    return schema;
}

async function transmogrifyEntry(entry) {
    const source = entry.source;
    const filters = entry.filters;
    const sink = entry.sink;
    
    const schema = await getSchema(entry.schema);
    const sourceFunc = getSourceFunction(source.func);
    const sinkFunc = getSinkFunction(sink.func);

    return runPipeline(sourceFunc, source.params, filters, sinkFunc, sink.params, schema)
}

async function transmogrify(manifest) {
    manifest.forEach(function (schemaEntry) {
        const keys = Object.keys(schemaEntry);

        keys.forEach(function (entryName) {
            const entries = schemaEntry[entryName];

            entries.forEach(function (entry) {
                transmogrifyEntry(entry);
            });
        });
    });
}

module.exports =
{
    transmogrify: transmogrify
};
