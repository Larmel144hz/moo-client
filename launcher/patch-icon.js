const { rcedit } = require('rcedit');
const path = require('path');
const { execSync } = require('child_process');

const exePath = path.join(__dirname, 'dist', 'win-unpacked', 'Moo Client.exe');
const icoPath = path.join(__dirname, 'build', 'icon.ico');
const pfxPath = path.join(__dirname, 'build', 'mooteam.pfx');
const pfxPass = 'MooClient2026!';

const pkg = require('./package.json');
const ver = pkg.version || '1.0.2';

async function run() {
    // Step 1: Patch icon and version info
    await rcedit(exePath, {
        icon: icoPath,
        'product-version': ver,
        'file-version': ver,
        'version-string': {
            ProductName: 'Moo Client',
            FileDescription: 'Moo Client Launcher',
            CompanyName: 'MooTeam',
            OriginalFilename: 'Moo Client.exe'
        }
    });
    console.log('SUCCESS! Cow icon set on Moo Client.exe');

    // Step 2: Sign the exe with self-signed certificate using signtool
    try {
        const signtoolPaths = [
            'C:\\Program Files (x86)\\Windows Kits\\10\\bin\\10.0.26100.0\\x64\\signtool.exe',
            'C:\\Program Files (x86)\\Windows Kits\\10\\bin\\10.0.22621.0\\x64\\signtool.exe',
            'C:\\Program Files (x86)\\Windows Kits\\10\\bin\\10.0.22000.0\\x64\\signtool.exe',
            'C:\\Program Files (x86)\\Windows Kits\\10\\bin\\10.0.19041.0\\x64\\signtool.exe',
        ];

        let signtool = 'signtool';
        for (const p of signtoolPaths) {
            try {
                const fs = require('fs');
                if (fs.existsSync(p)) { signtool = `"${p}"`; break; }
            } catch(e) {}
        }

        const cmd = `${signtool} sign /f "${pfxPath}" /p "${pfxPass}" /fd sha256 /tr http://timestamp.digicert.com /td sha256 /d "Moo Client" "${exePath}"`;
        console.log('Signing Moo Client.exe...');
        execSync(cmd, { stdio: 'inherit' });
        console.log('SUCCESS! Moo Client.exe signed with MooTeam certificate!');
    } catch (e) {
        console.log('Note: signtool signing skipped (signtool not found or failed):', e.message);
        console.log('Will try PowerShell Set-AuthenticodeSignature fallback...');
        try {
            const psCmd = `powershell -Command "$cert = Get-ChildItem -Path Cert:\\CurrentUser\\My -CodeSigningCert | Where-Object { $_.Subject -like '*MooTeam*' } | Select-Object -First 1; if ($cert) { Set-AuthenticodeSignature -FilePath '${exePath}' -Certificate $cert -TimestampServer 'http://timestamp.digicert.com' -HashAlgorithm SHA256 } else { Write-Host 'No MooTeam certificate found' }"`;
            execSync(psCmd, { stdio: 'inherit' });
            console.log('SUCCESS! Moo Client.exe signed via PowerShell!');
        } catch (e2) {
            console.log('PowerShell signing also failed:', e2.message);
        }
    }
}

run().catch(console.error);
