const fs = require('fs');
const p = 'C:/Users/laaam/AppData/Local/Programs/moo-client-launcher/resources/app.asar';
if (fs.existsSync(p)) {
    const buf = fs.readFileSync(p);
    console.log('Size:', buf.length);
    console.log('Contains mooclient_presence_v4:', buf.indexOf('mooclient_presence_v4') !== -1);
    console.log('Contains api.restful-api.dev:', buf.indexOf('api.restful-api.dev') !== -1);
    console.log('Contains broker.hivemq.com:', buf.indexOf('broker.hivemq.com') !== -1);
} else {
    console.log('File does not exist');
}
