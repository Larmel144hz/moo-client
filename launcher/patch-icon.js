const { rcedit } = require('rcedit');
const path = require('path');

const exePath = path.join(__dirname, 'dist', 'win-unpacked', 'Moo Client.exe');
const icoPath = path.join(__dirname, 'build', 'icon.ico');

const pkg = require('./package.json');
const ver = pkg.version || '1.0.1';

rcedit(exePath, {
    icon: icoPath,
    'product-version': ver,
    'file-version': ver,
    'version-string': {
        ProductName: 'Moo Client',
        FileDescription: 'Moo Client Launcher',
        CompanyName: 'MooTeam',
        OriginalFilename: 'Moo Client.exe'
    }
}).then(() => {
    console.log('SUCCESS! Cow icon set on Moo Client.exe');
}).catch(e => {
    console.error('Error:', e);
});
