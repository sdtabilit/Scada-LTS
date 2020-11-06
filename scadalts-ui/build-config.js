/**
 * This script get the latest release tag from Scada-LTS GitHub repository
 * and update the tag property in package.json file. Run this script before
 * releasing the new Scada-LTS version for production. 
 * 
 * To run this script type:
 * 'npm run-script version update'
 * 
 * This tag is displayed in the new Scada-LTS SystemSettings view.
 * Also there on this page there is a UI version 
 * from the version property in the package.json file.
 * Remember to increment this value!
 */
const saveFile = require('fs').writeFileSync;
const https = require("https");
const pkgJsonPath = require.main.paths[0].split('node_modules')[0] + 'package.json';
const json = require(pkgJsonPath);

// ----- PACKAGE.JSON VARIABLES ----- //
var tag = '0.1.0';
var milestone = '2.0.0';
var build = "0";
var branch = "local"
// ----- ---------------------- ----- //

if(process.argv.length === 5) {
    milestone = process.argv[2];
    build = process.argv[3];
    branch = process.argv[4]
}

if (!json.hasOwnProperty('scripts')) {
  json.scripts = {};
}

var options = {
    host: 'api.github.com', 
    path: '/repos/SCADA-LTS/Scada-LTS/releases/latest',
    headers: { 'User-Agent': 'Mozilla/5.0' }
}

var request = https.request(options, function (res) {
    var data = '';
    res.on('data', function(chunk) {
        data += chunk;
    });
    res.on('end', function() {
        let webJson = JSON.parse(data);
        tag = webJson.tag_name.replace(/[^\d.-]/g, '');;
        json.tag = tag;
        json.milestone = milestone;
        json.build = build;
        json.branch = branch;
        console.log(json.tag)
        console.log(`${json.milestone}.${json.build}`)
        console.log(json.branch)
        saveFile(pkgJsonPath, JSON.stringify(json, null, 2));
    });
});
request.on('error', function (e) {
    console.log(e.message);
});
request.end();
