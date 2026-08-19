const { execSync } = require('child_process');
const https = require('https');
const fs = require('fs');
const path = require('path');

const VERSION = '1.4.7';

function getGitHubToken() {
    try {
        const input = 'protocol=https\nhost=github.com\n\n';
        const result = execSync('git credential fill', { input, encoding: 'utf8', timeout: 5000 });
        const match = result.match(/password=(.+)/);
        return match ? match[1].trim() : null;
    } catch (e) { return null; }
}

function apiRequest(method, apiPath, token, body) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'api.github.com',
            path: apiPath,
            method,
            headers: {
                'Authorization': `token ${token}`,
                'User-Agent': 'MooClient-Builder',
                'Accept': 'application/vnd.github.v3+json'
            }
        };
        if (body) options.headers['Content-Type'] = 'application/json';
        const req = https.request(options, res => {
            let data = '';
            res.on('data', c => data += c);
            res.on('end', () => resolve({ status: res.statusCode, data: JSON.parse(data || '{}') }));
        });
        req.on('error', reject);
        if (body) req.write(JSON.stringify(body));
        req.end();
    });
}

function uploadAsset(uploadUrl, token, filePath, fileName, contentType) {
    return new Promise((resolve, reject) => {
        const fileData = fs.readFileSync(filePath);
        const url = new URL(uploadUrl.replace('{?name,label}', '') + '?name=' + encodeURIComponent(fileName));
        const req = https.request({
            hostname: url.hostname,
            path: url.pathname + url.search,
            method: 'POST',
            headers: {
                'Authorization': `token ${token}`,
                'User-Agent': 'MooClient-Builder',
                'Content-Type': contentType,
                'Content-Length': fileData.length
            }
        }, res => {
            let data = '';
            res.on('data', c => data += c);
            res.on('end', () => {
                console.log(`Upload ${fileName}: HTTP ${res.statusCode}`);
                resolve({ status: res.statusCode });
            });
        });
        req.on('error', reject);
        req.write(fileData);
        req.end();
    });
}

(async () => {
    const token = getGitHubToken();
    if (!token) { console.error('No token'); process.exit(1); }

    let res = await apiRequest('GET', `/repos/Moo-Client/moo-client/releases/tags/v${VERSION}`, token);
    let release;

    if (res.status === 200) {
        release = res.data;
        console.log('Found existing release:', release.html_url);
    } else {
        res = await apiRequest('POST', '/repos/Moo-Client/moo-client/releases', token, {
            tag_name: `v${VERSION}`,
            name: `Moo Client v${VERSION}`,
            body: '🚀 **Moo Client v1.4.7 (Waypoints, See-Through Logo & Minimalist UI)**\n\n✓ Logo Moo Client widoczne również przez ściany i przeszkody (See-Through Layer)\n✓ Rock-solid matematyczna projekcja 3D->2D dla Waypointów bez drgań\n✓ Celownik renderowany na wierzchu przed punktami w świecie\n✓ Opcje tła i cienia tekstu dla Waypointów\n✓ Nowoczesny i minimalistyczny wygląd menu modów',
            draft: false,
            prerelease: false
        });
        release = res.data;
        console.log('Created release:', release.html_url);
    }

    // Delete old assets
    if (release.assets) {
        for (const asset of release.assets) {
            console.log('Deleting old asset:', asset.name);
            await apiRequest('DELETE', `/repos/Moo-Client/moo-client/releases/assets/${asset.id}`, token);
        }
    }

    // Upload jar
    const jarPath = path.join(__dirname, '..', 'build', 'libs', `moo-client-${VERSION}.jar`);
    if (fs.existsSync(jarPath)) {
        await uploadAsset(release.upload_url, token, jarPath, `moo-client-${VERSION}.jar`, 'application/java-archive');
    }

    // Upload asar
    const asarPath = path.join(__dirname, 'dist', 'win-unpacked', 'resources', 'app.asar');
    if (fs.existsSync(asarPath)) {
        console.log('Uploading app.asar (65MB)...');
        await uploadAsset(release.upload_url, token, asarPath, 'app.asar', 'application/octet-stream');
    }

    // Upload exe
    const exePath = path.join(__dirname, 'dist', `Moo Client Setup ${VERSION}.exe`);
    if (fs.existsSync(exePath)) {
        console.log('Uploading exe (136MB)...');
        await uploadAsset(release.upload_url, token, exePath, `Moo.Client.Setup.${VERSION}.exe`, 'application/octet-stream');
    }

    console.log('ALL v1.4.7 ASSETS UPLOADED AND REPLACED SUCCESSFULLY!');
})();
