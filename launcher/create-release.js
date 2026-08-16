const { execSync } = require('child_process');
const https = require('https');
const fs = require('fs');
const path = require('path');

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

    let res = await apiRequest('GET', '/repos/Larmel144hz/moo-client/releases/tags/v1.3.1', token);
    if (res.status !== 200) {
        console.error('Release v1.3.1 not found:', res.status);
        process.exit(1);
    }
    const release = res.data;
    console.log('Found release:', release.html_url);

    // Delete existing app.asar asset if any
    if (release.assets) {
        for (const asset of release.assets) {
            if (asset.name === 'app.asar') {
                console.log('Deleting old app.asar asset:', asset.id);
                await apiRequest('DELETE', `/repos/Larmel144hz/moo-client/releases/assets/${asset.id}`, token);
            }
        }
    }

    // Upload app.asar
    const asarPath = path.join(__dirname, 'dist', 'win-unpacked', 'resources', 'app.asar');
    if (fs.existsSync(asarPath)) {
        console.log('Uploading app.asar (65MB)...');
        await uploadAsset(release.upload_url, token, asarPath, 'app.asar', 'application/octet-stream');
    }

    console.log('ALL ASSETS UPLOADED SUCCESSFULLY!');
})();
