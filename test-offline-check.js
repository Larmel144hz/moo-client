const path = require('path');
const os = require('os');
const fs = require('fs');

const gameDir = path.join(os.homedir(), '.mooclient');
const offlineDir = path.join(gameDir, 'offline', 'multiver');
const coreModPath = path.join(offlineDir, 'moo-client.jar');

console.log('1. Checking offline folder:');
console.log('   Path:', offlineDir);
console.log('   Exists:', fs.existsSync(offlineDir));

console.log('\n2. Checking core mod jar:');
console.log('   Path:', coreModPath);
console.log('   Exists:', fs.existsSync(coreModPath));
if (fs.existsSync(coreModPath)) {
    console.log('   Size:', (fs.statSync(coreModPath).size / 1024).toFixed(1), 'KB');
}

console.log('\n3. Checking mods folder:');
const modsDir = path.join(gameDir, 'mods');
if (fs.existsSync(modsDir)) {
    const mods = fs.readdirSync(modsDir);
    console.log('   Files in mods folder:', mods);
}

console.log('\n4. Injected JVM Argument:');
console.log(`   -Dfabric.addMods=${coreModPath}`);
