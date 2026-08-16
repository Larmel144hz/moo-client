const https = require('https');

function testFirebase(url, method, data) {
  return new Promise(resolve => {
    const u = new URL(url);
    const body = data ? JSON.stringify(data) : null;
    const req = https.request({
      hostname: u.hostname,
      path: u.pathname + u.search,
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'MooClient/1.0'
      }
    }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => {
        console.log(`[${method} ${url}] HTTP ${res.statusCode}: ${d.slice(0, 100)}`);
        resolve(res.statusCode === 200);
      });
    });
    req.on('error', e => {
      console.log(`[${method} ${url}] Error: ${e.message}`);
      resolve(false);
    });
    if (body) req.write(body);
    req.end();
  });
}

(async () => {
  await testFirebase('https://mooclient-presence-default-rtdb.europe-west1.firebasedatabase.app/users.json', 'PUT', { test: Date.now() });
  await testFirebase('https://mooclient-presence-default-rtdb.europe-west1.firebasedatabase.app/users.json', 'GET');
})();
