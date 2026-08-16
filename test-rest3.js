const https = require('https');

function postPlayer(username, uuid) {
  return new Promise(resolve => {
    const body = JSON.stringify({
      name: `moo_user_${username.toLowerCase()}`,
      data: {
        username: username,
        uuid: uuid,
        time: Date.now()
      }
    });
    const req = https.request({
      hostname: 'api.restful-api.dev',
      path: '/objects',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'MooClient/1.0'
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
  const p1 = await postPlayer('Mateusz', '12345');
  console.log('Registered player:', p1);
})();
