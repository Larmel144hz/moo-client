const https = require('https');

function patch(id, username) {
  return new Promise(resolve => {
    const body = JSON.stringify({
      data: {
        [username]: Date.now()
      }
    });
    const req = https.request({
      hostname: 'api.restful-api.dev',
      path: `/objects/${id}`,
      method: 'PATCH',
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

(async () => {
  const HUB_ID = 'ff8081819ff5b11001a00b7365962e83';
  const res1 = await patch(HUB_ID, 'Gracz_Janek');
  console.log('PATCH Janek:', res1.data);
  const res2 = await patch(HUB_ID, 'Gracz_Kamil');
  console.log('PATCH Kamil:', res2.data);
})();
