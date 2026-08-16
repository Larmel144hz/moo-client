const os = require('os');
const fs = require('fs');
const path = require('path');
const cp = require('child_process');

const targetAsar = path.join(process.env.LOCALAPPDATA, 'Programs', 'Moo Client', 'resources', 'app.asar');
const targetExe = path.join(process.env.LOCALAPPDATA, 'Programs', 'Moo Client', 'Moo Client.exe');
const tempAsar = path.join(os.tmpdir(), 'moo-update-test.pkg');

// Create a dummy temp file
fs.copyFileSync(targetAsar, tempAsar);

const scriptPath = path.join(os.tmpdir(), 'test-updater.ps1');
const psContent = [
    "$ErrorActionPreference = 'Stop'",
    `Write-Output "Copying from ${tempAsar} to ${targetAsar}"`,
    `Copy-Item -Path '${tempAsar}' -Destination '${targetAsar}' -Force`,
    `Write-Output "Copy successful!"`,
    `Remove-Item -Path '${tempAsar}' -Force`,
    `Write-Output "Cleaned up temp"`
].join('\r\n');

fs.writeFileSync(scriptPath, psContent, 'utf8');

const res = cp.spawnSync('powershell.exe', [
    '-NoProfile',
    '-ExecutionPolicy', 'Bypass',
    '-File', scriptPath
], { encoding: 'utf8' });

console.log('STDOUT:', res.stdout);
console.log('STDERR:', res.stderr);
console.log('STATUS:', res.status);
