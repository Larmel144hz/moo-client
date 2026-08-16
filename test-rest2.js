const https = require('https');

function get(url) {
  return new Promise(resolve => {
    https.get(url, { headers: { 'User-Agent': 'MooClient' } }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => {
        try { resolve(JSON.parse(d)); } catch(e) { resolve(d); }
      });
    }).on('error', () => resolve(null));
  });
}

(async () => {
  const all = await get('https://api.restful-api.dev/objects');
  console.log('Total objects from /objects:', Array.isArray(all) ? all.length : all);
})();
