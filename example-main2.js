const transmogrifier = require("./transmogrifier2");
const fs = require('fs');
const http = require('http');
const https = require('https');

function loadManifest(manifestPath, callback) {
    if (manifestPath.startsWith("http://") || manifestPath.startsWith("https://")) {
        const httpModule = manifestPath.startsWith("https://") ? https : http;

        httpModule.get(manifestPath, (res) => {
            if (res.statusCode !== 200) {
                callback(new Error(`Failed to load manifest: HTTP status code ${res.statusCode}`));
                return;
            }

            let rawData = '';
            res.on('data', (chunk) => { rawData += chunk; });
            res.on('end', () => {
                try {
                    callback(null, rawData);
                }
                catch (e) {
                    callback(e);
                }
            });
        }).on('error', (e) => {
            callback(e);
        });
    }
    else {
        fs.readFile(manifestPath, 'utf8', (err, data) => {
            if (err) {
                callback(err);
            }
            else {
                callback(null, data);
            }
        });
    }
}

function main(argv) {
    
    const manifestUrl = argv[0];

    loadManifest(manifestUrl, async (err, data) => {
        if (err) {
            console.error(err);
            process.exit(1);
        }
        else {
            const manifest = JSON.parse(data);
            try {
            //    data = await transmogrifier.transmogrify(manifest);
            transmog = new transmogrifier.Transmogrifier(manifest);
            data = await transmog.transmogrify()
            }
            catch (error) {
                console.log(error);
            }
        }
    });
}

const argv = process.argv.slice(2);

if (argv.length === 0) {
    console.log("Please provide an argument");
    process.exit(1);
}

main(argv);
