const https = require('https');

function put(id, data) {
  return new Promise(resolve => {
    const body = JSON.stringify({
      name: 'MooClient_Global_Hub',
      data: data
    });
    const req = https.request({
      hostname: 'api.restful-api.dev',
      path: `/objects/${id}`,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'MooClient'
      }
    }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve(JSON.parse(d || '{}')));
    });
    req.on('error', () => resolve(null));
    req.write(body);
    req.end();
  });
}

function get(id) {
  return new Promise(resolve => {
    https.get(`https://api.restful-api.dev/objects/${id}`, { headers: { 'User-Agent': 'MooClient' } }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve(JSON.parse(d || '{}')));
    }).on('error', () => resolve(null));
  });
}

(async () => {
  const HUB_ID = 'ff8081819ff5b11001a00b7365962e83';
  const updated = await put(HUB_ID, {
    "Larmel": Date.now(),
    "Player2": Date.now() - 2000
  });
  console.log('PUT result:', updated);
  const fetched = await get(HUB_ID);
  console.log('GET result:', fetched);
})();
