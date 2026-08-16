const os = require('os');
const fs = require('fs');
const path = require('path');
const cp = require('child_process');

const targetAsar = path.join(process.env.LOCALAPPDATA, 'Programs', 'Moo Client', 'resources', 'app.asar');
const targetExe = path.join(process.env.LOCALAPPDATA, 'Programs', 'Moo Client', 'Moo Client.exe');
const tempAsar = path.join(os.tmpdir(), 'moo-update-test2.pkg');

fs.copyFileSync(targetAsar, tempAsar);

const scriptPath = path.join(os.tmpdir(), 'test-spawn-updater.ps1');
const psContent = [
    "$ErrorActionPreference = 'SilentlyContinue'",
    "Start-Sleep -Milliseconds 300",
    "",
    "$copied = $false",
    "for ($i = 0; $i -lt 10; $i++) {",
    "    try {",
    `        Copy-Item -Path '${tempAsar.replace(/'/g, "''")}' -Destination '${targetAsar.replace(/'/g, "''")}' -Force -ErrorAction Stop`,
    "        $copied = $true",
    `        Remove-Item -Path '${tempAsar.replace(/'/g, "''")}' -Force -ErrorAction SilentlyContinue`,
    "        break",
    "    } catch {",
    "        Start-Sleep -Milliseconds 200",
    "    }",
    "}",
    "",
    `Write-Output "Copied: $copied" >> "${path.join(os.tmpdir(), 'updater-log.txt')}"`,
    `Remove-Item -Path '${scriptPath}' -Force -ErrorAction SilentlyContinue`
].join('\r\n');

fs.writeFileSync(scriptPath, psContent, 'utf8');

const child = cp.spawn('powershell.exe', [
    '-NoProfile',
    '-ExecutionPolicy', 'Bypass',
    '-WindowStyle', 'Hidden',
    '-File', scriptPath
], {
    detached: true,
    stdio: 'ignore',
    windowsHide: true,
});
child.unref();

console.log('Spawned detached process successfully');
setTimeout(() => {
    const logPath = path.join(os.tmpdir(), 'updater-log.txt');
    if (fs.existsSync(logPath)) {
        console.log('LOG OUTPUT:', fs.readFileSync(logPath, 'utf8'));
        fs.unlinkSync(logPath);
    }
}, 2000);
