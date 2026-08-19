const fs = require('fs');
const path = require('path');
const os = require('os');

const gameDir = path.join(os.homedir(), '.mooclient');
const offlineDir = path.join(gameDir, 'offline', 'multiver');
fs.mkdirSync(offlineDir, { recursive: true });

const targetJar = path.join(offlineDir, 'moo-client.jar');
const srcJar = path.join(__dirname, 'build', 'libs', 'moo-client-1.4.6.jar');

if (fs.existsSync(srcJar)) {
  fs.copyFileSync(srcJar, targetJar);
  console.log('SUCCESS: Copied moo-client.jar to offline folder:', targetJar);
} else {
  console.log('Source jar not found at:', srcJar);
}

console.log('JVM Argument to be injected:');
console.log(`-Dfabric.addMods=${targetJar}`);
