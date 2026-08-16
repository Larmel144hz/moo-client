const os = require('os');
const fs = require('fs');
const path = require('path');
const cp = require('child_process');

const targetAsar = path.join(process.env.LOCALAPPDATA, 'Programs', 'Moo Client', 'resources', 'app.asar');
const targetExe = path.join(process.env.LOCALAPPDATA, 'Programs', 'Moo Client', 'Moo Client.exe');
const tempAsar = path.join(os.tmpdir(), 'moo-update-test3.pkg');
const logFile = path.join(os.tmpdir(), 'bat-updater-log.txt');

fs.copyFileSync(targetAsar, tempAsar);

const scriptPath = path.join(os.tmpdir(), 'moo-updater-test.bat');
const scriptContent = [
    '@echo off',
    'setlocal',
    'ping 127.0.0.1 -n 2 >nul',
    `taskkill /F /IM "Moo Client.exe" >nul 2>&1`,
    ':retry',
    `copy /Y "${tempAsar}" "${targetAsar}" >nul 2>&1`,
    'if errorlevel 1 (',
    '    ping 127.0.0.1 -n 2 >nul',
    '    goto retry',
    ')',
    `echo SUCCESS_COPIED > "${logFile}"`,
    `del /F /Q "${tempAsar}" >nul 2>&1`,
    `del "%~f0"`
].join('\r\n');

fs.writeFileSync(scriptPath, scriptContent, 'utf8');

const child = cp.spawn('cmd.exe', ['/c', scriptPath], {
    detached: true,
    stdio: 'ignore',
    windowsHide: true,
});
child.unref();

console.log('Spawned batch script');
setTimeout(() => {
    if (fs.existsSync(logFile)) {
        console.log('BAT LOG:', fs.readFileSync(logFile, 'utf8').trim());
        fs.unlinkSync(logFile);
    } else {
        console.log('NO_BAT_LOG');
    }
}, 3000);
