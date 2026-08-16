const net = require('net');

function createClient(name, onMessage) {
  return new Promise(resolve => {
    const socket = net.createConnection(1883, 'broker.hivemq.com', () => {
      // 1. Send CONNECT
      const clientId = 'Moo_' + name + '_' + Math.floor(Math.random() * 10000);
      const payload = Buffer.from(clientId);
      const variableHeader = Buffer.from([0x00, 0x04, 0x4D, 0x51, 0x54, 0x54, 0x04, 0x02, 0x00, 0x3C]);
      const remainLen = variableHeader.length + 2 + payload.length;
      const connectPacket = Buffer.concat([Buffer.from([0x10, remainLen]), variableHeader, Buffer.from([0x00, payload.length]), payload]);
      socket.write(connectPacket);
    });

    socket.on('data', data => {
      // CONNACK
      if (data[0] === 0x20) {
        // 2. Send SUBSCRIBE to "mooclient/presence"
        const topic = Buffer.from('mooclient/presence_v2');
        const subPayload = Buffer.concat([Buffer.from([0x00, topic.length]), topic, Buffer.from([0x00])]); // QoS 0
        const subVarHeader = Buffer.from([0x00, 0x01]); // Packet ID 1
        const subRemain = subVarHeader.length + subPayload.length;
        const subPacket = Buffer.concat([Buffer.from([0x82, subRemain]), subVarHeader, subPayload]);
        socket.write(subPacket);
        resolve({
          publish: (msg) => {
            const topic = Buffer.from('mooclient/presence_v2');
            const payload = Buffer.from(msg);
            const pubRemain = 2 + topic.length + payload.length;
            const pubPacket = Buffer.concat([Buffer.from([0x30, pubRemain, 0x00, topic.length]), topic, payload]);
            socket.write(pubPacket);
          },
          close: () => socket.end()
        });
      } else if ((data[0] & 0xF0) === 0x30) {
        // PUBLISH received
        // find payload
        const topicLen = (data[2] << 8) | data[3];
        const payload = data.slice(4 + topicLen).toString('utf8');
        onMessage(payload);
      }
    });
  });
}

(async () => {
  const clientA = await createClient('PlayerA', msg => console.log('[Client A received]:', msg));
  const clientB = await createClient('PlayerB', msg => console.log('[Client B received]:', msg));

  console.log('Both clients connected and subscribed to mooclient/presence_v2!');

  setTimeout(() => {
    console.log('Client B publishing presence...');
    clientB.publish(JSON.stringify({ u: 'kolega_janek', t: Date.now() }));
  }, 1000);

  setTimeout(() => {
    console.log('Client A publishing presence...');
    clientA.publish(JSON.stringify({ u: 'larmel', t: Date.now() }));
  }, 2000);

  setTimeout(() => {
    clientA.close();
    clientB.close();
    process.exit(0);
  }, 3500);
})();
