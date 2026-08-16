const path = require('path');
const os = require('os');
const fs = require('fs');

const gameDir = path.join(os.homedir(), '.mooclient');
const targetLibDir = path.join(gameDir, 'libraries', 'com', 'mooclient', 'moo-client', '1.4.3');
fs.mkdirSync(targetLibDir, { recursive: true });

const jarSrc = path.join(__dirname, 'build', 'libs', 'moo-client-1.4.2.jar');
const jarDst = path.join(targetLibDir, 'moo-client-1.4.3.jar');
if (fs.existsSync(jarSrc)) {
    fs.copyFileSync(jarSrc, jarDst);
    console.log('Copied core mod to library path:', jarDst);
}

// Check version json
const vJsonPath = path.join(gameDir, 'versions', 'fabric-loader-1.21.4', 'fabric-loader-1.21.4.json');
if (fs.existsSync(vJsonPath)) {
    const vJson = JSON.parse(fs.readFileSync(vJsonPath, 'utf8'));
    vJson.libraries = vJson.libraries.filter(l => !l.name || !l.name.includes('com.mooclient:moo-client'));
    vJson.libraries.push({ name: 'com.mooclient:moo-client:1.4.3' });
    fs.writeFileSync(vJsonPath, JSON.stringify(vJson, null, 2));
    console.log('Updated fabric-loader-1.21.4.json with Moo Client library!');
}
