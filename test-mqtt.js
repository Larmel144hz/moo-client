const net = require('net');

function testMqtt() {
  return new Promise(resolve => {
    const socket = net.createConnection(1883, 'broker.hivemq.com', () => {
      console.log('CONNECTED TO HIVEMQ ON PORT 1883!');
      
      // MQTT 3.1.1 CONNECT packet
      // Protocol Name: MQTT, Level 4
      const clientId = 'MooClient_Test_' + Math.floor(Math.random() * 10000);
      const payload = Buffer.from(clientId);
      const variableHeader = Buffer.from([
        0x00, 0x04, 0x4D, 0x51, 0x54, 0x54, // "MQTT"
        0x04,                               // Protocol Level 4
        0x02,                               // Clean Session
        0x00, 0x3C                          // Keep Alive 60s
      ]);
      const remainLen = variableHeader.length + 2 + payload.length;
      const clientLen = Buffer.from([0x00, payload.length]);
      const connectPacket = Buffer.concat([
        Buffer.from([0x10, remainLen]),
        variableHeader,
        clientLen,
        payload
      ]);

      socket.write(connectPacket);
    });

    socket.on('data', data => {
      console.log('RECEIVED FROM HIVEMQ:', data);
      if (data[0] === 0x20 && data[1] === 0x02 && data[3] === 0x00) {
        console.log('CONNACK SUCCESS! HiveMQ connection is 100% WORKING!');
        socket.end();
        resolve(true);
      }
    });

    socket.on('error', err => {
      console.log('HiveMQ error:', err.message);
      resolve(false);
    });
  });
}

testMqtt();
