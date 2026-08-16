const https = require('https');

function testRestful(path, method, data) {
  return new Promise(resolve => {
    const body = data ? JSON.stringify(data) : null;
    const req = https.request({
      hostname: 'api.restful-api.dev',
      path: path,
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'MooClient/1.0'
      }
    }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => {
        console.log(`[${method} ${path}] HTTP ${res.statusCode}: ${d.slice(0, 200)}`);
        resolve(JSON.parse(d || '{}'));
      });
    });
    req.on('error', e => {
      console.log(`[${method} ${path}] Error: ${e.message}`);
      resolve(null);
    });
    if (body) req.write(body);
    req.end();
  });
}

(async () => {
  const created = await testRestful('/objects', 'POST', {
    name: 'MooClient_Presence_Hub',
    data: { users: ['Larmel', 'Steve'] }
  });
  if (created && created.id) {
    await testRestful(`/objects/${created.id}`, 'GET');
  }
})();
