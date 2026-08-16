function testWs(url) {
  return new Promise((resolve) => {
    console.log('Testing WS:', url);
    const ws = new WebSocket(url);
    const timer = setTimeout(() => {
      console.log('Timeout WS:', url);
      ws.close();
      resolve(false);
    }, 4000);

    ws.onopen = () => {
      console.log('CONNECTED WS:', url);
      ws.send(JSON.stringify({ type: 'ping' }));
    };
    ws.onmessage = (e) => {
      console.log('MSG from WS:', url, e.data.slice(0, 100));
      clearTimeout(timer);
      ws.close();
      resolve(true);
    };
    ws.onerror = (err) => {
      console.log('ERROR WS:', url, err);
      clearTimeout(timer);
      resolve(false);
    };
  });
}

(async () => {
  await testWs('wss://echo.websocket.events');
  await testWs('wss://socketsbay.com/wss/v2/1/demo/');
})();
