const https = require('https');

const HUB_ID = 'ff8081819ff5b11001a00b7365962e83';

function getHub() {
  return new Promise(resolve => {
    https.get(`https://api.restful-api.dev/objects/${HUB_ID}`, { headers: { 'User-Agent': 'MooClient' } }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => {
        try { resolve(JSON.parse(d).data || {}); } catch(e) { resolve({}); }
      });
    }).on('error', () => resolve({}));
  });
}

function updateHub(usersMap) {
  return new Promise(resolve => {
    const body = JSON.stringify({
      name: 'MooClient_Global_Hub',
      data: usersMap
    });
    const req = https.request({
      hostname: 'api.restful-api.dev',
      path: `/objects/${HUB_ID}`,
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'MooClient'
      }
    }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve(true));
    });
    req.on('error', () => resolve(false));
    req.write(body);
    req.end();
  });
}

async function heartbeat(myUsername) {
  const current = await getHub();
  console.log('Online Moo users from Hub:', Object.keys(current));
  
  // Register ourselves
  current[myUsername.toLowerCase()] = Date.now();
  
  // Clean up users older than 2 hours
  const now = Date.now();
  for (const k of Object.keys(current)) {
    if (now - current[k] > 2 * 3600 * 1000) {
      delete current[k];
    }
  }

  await updateHub(current);
  console.log('Heartbeat updated successfully. New online list:', Object.keys(current));
}

(async () => {
  await heartbeat('Larmel');
  await heartbeat('Gracz_Kamil');
})();
