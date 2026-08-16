const https = require('https');

function request(path, method, body) {
  return new Promise(resolve => {
    const data = body ? JSON.stringify(body) : null;
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
        console.log(`[${method} ${path}] status=${res.statusCode}, raw=${d.slice(0, 100)}`);
        try { resolve(JSON.parse(d)); } catch(e) { resolve(null); }
      });
    });
    req.on('error', (e) => {
      console.log(`[${method} ${path}] error:`, e.message);
      resolve(null);
    });
    if (data) req.write(data);
    req.end();
  });
}

(async () => {
  const pA = await request('/objects', 'POST', {
    name: 'mooclient_user_larmel',
    data: { u: 'larmel', t: Date.now() }
  });
  console.log('pA:', pA);
})();
