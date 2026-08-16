const https = require('https');

async function test(name, url, method='GET', body=null) {
  const start = Date.now();
  return new Promise((resolve) => {
    const u = new URL(url);
    const req = https.request({
      hostname: u.hostname,
      path: u.pathname + u.search,
      method: method,
      timeout: 3000,
      headers: {
        'User-Agent': 'MooClient/1.0',
        'Content-Type': 'application/json'
      }
    }, res => {
      let data = '';
      res.on('data', c => data += c);
      res.on('end', () => {
        console.log(`[${name}] status=${res.statusCode}, time=${Date.now() - start}ms, dataLen=${data.length}, data=${data.slice(0, 100)}`);
        resolve(true);
      });
    });
    req.on('error', err => {
      console.log(`[${name}] error: ${err.message}`);
      resolve(false);
    });
    req.on('timeout', () => {
      req.destroy();
      console.log(`[${name}] timeout`);
      resolve(false);
    });
    if (body) req.write(body);
    req.end();
  });
}

(async () => {
  await test('Pie Socket HTTP', 'https://pie.dev/get');
  await test('KVDB Test', 'https://kvdb.io/bucket_test_moo/testkey', 'POST', '123');
  await test('KVDB Get', 'https://kvdb.io/bucket_test_moo/testkey', 'GET');
})();
