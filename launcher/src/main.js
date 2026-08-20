const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron');
const path = require('path');
const fs = require('fs');
const os = require('os');
const https = require('https');
const http = require('http');
const discordRPC = require('./DiscordRPC');

function downloadFile(url, destPath, onProgress) {
    return new Promise((resolve, reject) => {
        const prevNoAsar = process.noAsar;
        process.noAsar = true;
        const client = url.startsWith('https') ? https : http;
        client.get(url, { headers: { 'User-Agent': 'MooClient-Launcher' } }, (res) => {
            if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
                process.noAsar = prevNoAsar;
                return downloadFile(res.headers.location, destPath, onProgress).then(resolve).catch(reject);
            }
            if (res.statusCode !== 200) {
                process.noAsar = prevNoAsar;
                return reject(new Error(`Pobieranie nie powiodło się: HTTP ${res.statusCode}`));
            }

            const totalBytes = parseInt(res.headers['content-length'] || '0', 10);
            let receivedBytes = 0;
            const fileStream = fs.createWriteStream(destPath);

            res.on('data', (chunk) => {
                receivedBytes += chunk.length;
                if (totalBytes > 0 && onProgress) {
                    const pct = Math.min(100, Math.round((receivedBytes / totalBytes) * 100));
                    onProgress(pct);
                }
            });

            res.pipe(fileStream);

            fileStream.on('finish', () => {
                fileStream.close(() => {
                    process.noAsar = prevNoAsar;
                    resolve(destPath);
                });
            });

            fileStream.on('error', (err) => {
                process.noAsar = prevNoAsar;
                fs.unlink(destPath, () => {});
                reject(err);
            });
        }).on('error', (err) => {
            process.noAsar = prevNoAsar;
            fs.unlink(destPath, () => {});
            reject(err);
        });
    });
}

let mainWindow;
let gameManager;
let modManager;

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1200,
        height: 720,
        minWidth: 900,
        minHeight: 600,
        frame: false,
        backgroundColor: '#09090b',
        resizable: true,
        center: true,
        icon: path.join(__dirname, 'renderer', 'logo.png'),
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            contextIsolation: true,
            nodeIntegration: false,
            webSecurity: false,
        },
        show: false,
    });

    mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));

    mainWindow.once('ready-to-show', () => {
        mainWindow.show();
        mainWindow.focus();
        mainWindow.setAlwaysOnTop(true);
        setTimeout(() => mainWindow.setAlwaysOnTop(false), 2000);
    });

    mainWindow.on('closed', () => {
        mainWindow = null;
    });
}



// =============================================
// IPC Handlers (communication with renderer)
// =============================================
function setupIPC() {
    const GameManager = require('./GameManager');
    const ModManager = require('./ModManager');
    gameManager = new GameManager();
    modManager = new ModManager();

    // --- Window controls ---
    ipcMain.on('window-minimize', () => mainWindow?.minimize());
    ipcMain.on('window-maximize', () => {
        if (mainWindow?.isMaximized()) {
            mainWindow.unmaximize();
        } else {
            mainWindow?.maximize();
        }
    });
    ipcMain.on('window-close', () => mainWindow?.close());

    // --- Launch game ---
    ipcMain.handle('launch-game', async (event, options) => {
        try {
            sendToRenderer('launch-status', 'Sprawdzanie bibliotek i modów...');
            sendToRenderer('launch-progress', 5);

            // Step 1: Ensure Fabric API Core dependency is downloaded
            await modManager.ensureFabricApi((status, progress) => {
                sendToRenderer('launch-status', status);
                sendToRenderer('launch-progress', progress);
            });

            // Step 2: Check & update mod from GitHub
            const modUpdated = await modManager.checkAndUpdate((status, progress) => {
                sendToRenderer('launch-status', status);
                sendToRenderer('launch-progress', progress);
            });

            // Step 3: Launch Minecraft with Fabric + mod
            sendToRenderer('launch-status', 'Uruchamianie Minecrafta...');
            sendToRenderer('launch-progress', 80);
            discordRPC.updateActivity('Uruchamia grę...', 'Minecraft 1.21.4 (Fabric)');

            await gameManager.launch(options, (status, progress) => {
                sendToRenderer('launch-status', status);
                sendToRenderer('launch-progress', progress);
            });

            discordRPC.updateActivity('W grze', 'Moo Client 1.21.4');
            sendToRenderer('launch-status', 'Game is running!');
            sendToRenderer('launch-progress', 100);
            return { success: true };
        } catch (error) {
            sendToRenderer('launch-status', `Error: ${error.message}`);
            sendToRenderer('launch-progress', 0);
            return { success: false, error: error.message };
        }
    });

    // --- Get settings ---
    ipcMain.handle('get-settings', async () => {
        return gameManager.getSettings();
    });

    // --- Save settings ---
    ipcMain.handle('save-settings', async (event, settings) => {
        gameManager.saveSettings(settings);
        return { success: true };
    });

    // --- Get mod info ---
    ipcMain.handle('get-mod-info', async () => {
        return modManager.getLocalVersion();
    });

    // --- Select Java path ---
    ipcMain.handle('select-java-path', async () => {
        const result = await dialog.showOpenDialog(mainWindow, {
            properties: ['openFile'],
            filters: [{ name: 'Java', extensions: ['exe'] }],
            title: 'Select javaw.exe',
        });
        if (!result.canceled && result.filePaths.length > 0) {
            return result.filePaths[0];
        }
        return null;
    });

    // --- Account & Auth ---
    ipcMain.handle('login-microsoft', async () => {
        try {
            const account = await gameManager.loginMicrosoft();
            return { success: true, account };
        } catch (error) {
            return { success: false, error: error.message };
        }
    });

    ipcMain.handle('validate-session', async () => {
        try {
            const account = gameManager.getAccount();
            if (!account) return { success: false, error: 'Brak konta' };
            const isValid = await gameManager.validateSession(account);
            return { success: true, isValid, accountName: account.name };
        } catch (e) {
            return { success: false, isValid: false, error: e.message };
        }
    });

    ipcMain.handle('refresh-session', async () => {
        try {
            const account = gameManager.getAccount();
            if (!account) return { success: false, error: 'Brak konta' };
            const result = await gameManager.refreshAccount(account);
            return result;
        } catch (e) {
            return { success: false, error: e.message };
        }
    });

    ipcMain.handle('logout-microsoft', async () => {
        return gameManager.logout();
    });

    ipcMain.handle('get-account', async () => {
        return gameManager.getAccount();
    });

    ipcMain.handle('get-accounts', async () => {
        return gameManager.getAllAccounts();
    });

    ipcMain.handle('select-account', async (event, uuid) => {
        return gameManager.selectAccount(uuid);
    });

    ipcMain.handle('remove-account', async (event, uuid) => {
        return gameManager.removeAccount(uuid);
    });

    // --- Modrinth & Mod Management ---
    ipcMain.handle('search-modrinth', async (event, params) => {
        try {
            const data = await modManager.searchModrinth(params?.query || '', params?.limit || 24, params?.offset || 0, params?.index || 'downloads');
            return { success: true, data };
        } catch (e) {
            return { success: false, error: e.message };
        }
    });

    ipcMain.handle('install-mod', async (event, projectId) => {
        try {
            const result = await modManager.installModrinthMod(projectId);
            return result;
        } catch (e) {
            return { success: false, error: e.message };
        }
    });

    ipcMain.handle('get-installed-mods', async () => {
        return modManager.getInstalledMods();
    });

    ipcMain.handle('uninstall-mod', async (event, filename) => {
        return modManager.uninstallMod(filename);
    });

    ipcMain.handle('toggle-mod', async (event, { filename, enabled }) => {
        return modManager.toggleMod(filename, enabled);
    });

    ipcMain.handle('install-local-mods', async (event, filePaths) => {
        return await modManager.installLocalMods(filePaths);
    });

    ipcMain.handle('save-mod-file', async (event, { filename, buffer }) => {
        return modManager.saveModBuffer(filename, buffer);
    });

    ipcMain.handle('get-mod-versions', async (event, data) => {
        const projectId = typeof data === 'string' ? data : data?.projectId;
        const allVersions = typeof data === 'object' ? !!data?.allVersions : false;
        return modManager.getModVersions(projectId, '1.21.4', allVersions);
    });

    ipcMain.handle('install-mod-version', async (event, { versionId, oldFilename }) => {
        return modManager.installModVersion(versionId, oldFilename);
    });

    ipcMain.handle('check-mod-updates', async () => {
        return modManager.checkModUpdates();
    });

    ipcMain.handle('open-mods-folder', async () => {
        shell.openPath(modManager.modsDir);
        return { success: true };
    });

function getActualLauncherVersion() {
    try {
        const pkg = require('../package.json');
        if (pkg && pkg.version) return pkg.version;
    } catch(e) {}
    try {
        const pkgPath = path.join(__dirname, '..', 'package.json');
        if (fs.existsSync(pkgPath)) {
            const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
            if (pkg && pkg.version) return pkg.version;
        }
    } catch(e) {}
    return app.getVersion();
}

    // --- Moo Client Core Version & Update Check ---
    ipcMain.handle('check-client-update', async () => {
        const launcherVersion = getActualLauncherVersion();
        try {
            const remote = await modManager.getRemoteVersion();
            const localMod = modManager.getLocalVersion();
            const launcherNeedsUpdate = ModManager.isNewerVersion(remote.version, launcherVersion);
            const modNeedsUpdate = ModManager.isNewerVersion(remote.version, localMod.version);
            const hasUpdate = launcherNeedsUpdate || modNeedsUpdate;

            return {
                success: true,
                hasUpdate,
                currentVersion: launcherVersion,
                latestVersion: remote.version,
                changelog: remote.changelog || '',
                downloadUrl: remote.download_url
            };
        } catch (e) {
            return {
                success: false,
                hasUpdate: false,
                currentVersion: launcherVersion,
                error: e.message
            };
        }
    });

    ipcMain.handle('perform-client-update', async () => {
        try {
            const remote = await modManager.getRemoteVersion();
            const launcherVersion = getActualLauncherVersion();
            const localMod = modManager.getLocalVersion();
            const launcherNeedsUpdate = ModManager.isNewerVersion(remote.version, launcherVersion);
            const modNeedsUpdate = ModManager.isNewerVersion(remote.version, localMod.version);

            // 1. Fast Delta Update for Fabric Mod Jar (only ~200 KB!)
            if (modNeedsUpdate) {
                sendToRenderer('client-update-progress', { status: 'Pobieranie zaktualizowanego kodu klienta...', percent: 20 });
                await modManager.checkAndUpdate((status, percent) => {
                    sendToRenderer('client-update-progress', { status: `Kod klienta: ${status}`, percent: 20 + Math.round(percent * 0.3) });
                });
            }

            // 2. Hot-ASAR delta update — writes external robust batch script for Windows
            if (launcherNeedsUpdate) {
                sendToRenderer('client-update-progress', { status: 'Pobieranie nowej paczki launchera...', percent: 55 });
                const latestVer = remote.version;
                const asarUrl = `https://github.com/Moo-Client/moo-client/releases/download/v${latestVer}/app.asar`;
                const tempAsar = path.join(os.tmpdir(), `moo-update-${latestVer}.pkg`);
                const targetAsar = path.join(process.resourcesPath, 'app.asar');
                const targetExe = process.execPath;
                const currentPid = process.pid;

                await downloadFile(asarUrl, tempAsar, (percent) => {
                    sendToRenderer('client-update-progress', { 
                        status: `Pobieranie kodu launchera: ${percent}%`, 
                        percent: 55 + Math.round(percent * 0.4) 
                    });
                });

                if (!fs.existsSync(tempAsar) || fs.statSync(tempAsar).size < 1000000) {
                    if (fs.existsSync(tempAsar)) {
                        try { fs.unlinkSync(tempAsar); } catch (e) {}
                    }
                    throw new Error('Pobrany plik aktualizacji launchera jest uszkodzony lub niekompletny.');
                }

                sendToRenderer('client-update-progress', { status: 'Przygotowywanie aktualizacji i restart...', percent: 98 });

                // Write a bulletproof .bat updater script that works on all Windows systems without policy restrictions
                const scriptPath = path.join(os.tmpdir(), `moo-updater-${Date.now()}.bat`);
                const scriptContent = [
                    '@echo off',
                    'setlocal',
                    'ping 127.0.0.1 -n 2 >nul',
                    `taskkill /F /PID ${currentPid} >nul 2>&1`,
                    'taskkill /F /IM "Moo Client.exe" >nul 2>&1',
                    'taskkill /F /IM "moo-client.exe" >nul 2>&1',
                    ':retry',
                    `copy /Y "${tempAsar}" "${targetAsar}" >nul 2>&1`,
                    'if errorlevel 1 (',
                    '    ping 127.0.0.1 -n 2 >nul',
                    '    goto retry',
                    ')',
                    `del /F /Q "${tempAsar}" >nul 2>&1`,
                    `start "" "${targetExe}"`,
                    '(goto) 2>nul & del "%~f0"'
                ].join('\r\n');

                fs.writeFileSync(scriptPath, scriptContent, 'utf8');

                // Completely silent launcher runner via wscript (0 = hidden window)
                const vbsPath = path.join(os.tmpdir(), `moo-silent-${Date.now()}.vbs`);
                const vbsContent = [
                    'Set WshShell = CreateObject("WScript.Shell")',
                    'WshShell.Run """" & WScript.Arguments(0) & """", 0, False',
                    'Set fso = CreateObject("Scripting.FileSystemObject")',
                    'fso.DeleteFile WScript.ScriptFullName'
                ].join('\r\n');
                fs.writeFileSync(vbsPath, vbsContent, 'utf8');

                sendToRenderer('client-update-progress', { status: 'Ponowne uruchamianie...', percent: 100 });

                const { spawn } = require('child_process');
                const child = spawn('wscript.exe', [vbsPath, scriptPath], {
                    detached: true,
                    stdio: 'ignore',
                    windowsHide: true,
                });
                child.unref();

                setTimeout(() => { app.exit(0); }, 300);
                return { success: true, updated: true, restarting: true };
            }

            // 3. Mod-only update or dev mode update (no launcher restart needed)
            sendToRenderer('client-update-progress', { status: 'Zaktualizowano pomyślnie!', percent: 100 });
            const newLocal = modManager.getLocalVersion();
            return {
                success: true,
                updated: modNeedsUpdate,
                restarting: false,
                version: newLocal.version
            };
        } catch (e) {
            console.error('Error during client update:', e);
            return { success: false, error: e.message };
        }
    });

    // --- Direct Drag & Drop Save Mod File ---
    ipcMain.handle('save-mod-file', async (event, { filename, buffer }) => {
        try {
            if (!filename || !filename.toLowerCase().endsWith('.jar')) {
                return { success: false, error: 'Plik musi mieć rozszerzenie .jar' };
            }
            modManager.ensureDir(modManager.modsDir);
            const destPath = path.join(modManager.modsDir, filename);
            fs.writeFileSync(destPath, Buffer.from(buffer));
            console.log(`Saved dropped mod: ${filename}`);
            return { success: true, filename };
        } catch (e) {
            console.error('Error saving dropped mod:', e);
            return { success: false, error: e.message };
        }
    });

    // --- Live Online Players Counter ---
    ipcMain.handle('get-online-users-count', () => launcherOnlineCount);
}

// =============================================
// Dual-Layer Hybrid Presence Manager (HTTPS SSE Stream + TLS MQTT)
// 100% firewall/ISP bypass, zero lag, instant discovery (<50ms)
// =============================================
let launcherOnlineCount = 1;
const net = require('net');
const tls = require('tls');
const crypto = require('crypto');
const launcherClientId = 'moo_launcher_' + crypto.randomBytes(6).toString('hex');
const activeUsers = new Map(); // id -> timestamp

const MQTT_BROKERS = [
    { host: 'broker.hivemq.com', port: 8883, tls: true },
    { host: 'broker.hivemq.com', port: 1883, tls: false },
    { host: 'broker.emqx.io', port: 8883, tls: true },
    { host: 'broker.emqx.io', port: 1883, tls: false }
];
let currentBrokerIndex = 0;
let mqttSocket = null;
let isMqttConnected = false;
let reconnectTimer = null;
let pingTimer = null;
let presenceTimer = null;
let ntfyStreamReq = null;

class MqttStreamParser {
    constructor(onPublish) {
        this.onPublish = onPublish;
        this.buffer = Buffer.alloc(0);
    }

    feed(chunk) {
        this.buffer = Buffer.concat([this.buffer, chunk]);
        while (this.buffer.length >= 2) {
            const firstByte = this.buffer[0];
            const packetType = (firstByte >> 4) & 0x0F;
            const qos = (firstByte >> 1) & 0x03;

            let multiplier = 1;
            let value = 0;
            let lenBytes = 0;
            let complete = false;

            for (let i = 1; i < this.buffer.length && i <= 4; i++) {
                const encodedByte = this.buffer[i];
                value += (encodedByte & 127) * multiplier;
                multiplier *= 128;
                lenBytes++;
                if ((encodedByte & 128) === 0) {
                    complete = true;
                    break;
                }
            }
            if (!complete) break;

            const headerLen = 1 + lenBytes;
            const totalPacketLen = headerLen + value;
            if (this.buffer.length < totalPacketLen) break;

            const packet = this.buffer.subarray(0, totalPacketLen);
            this.buffer = this.buffer.subarray(totalPacketLen);

            if (packetType === 3) { // PUBLISH
                let offset = headerLen;
                if (packet.length >= offset + 2) {
                    const topicLen = packet.readUInt16BE(offset);
                    offset += 2;
                    if (packet.length >= offset + topicLen) {
                        const topic = packet.subarray(offset, offset + topicLen).toString('utf8');
                        offset += topicLen;
                        if (qos > 0) offset += 2; // skip Packet Identifier
                        const payload = packet.subarray(offset).toString('utf8');
                        this.onPublish(topic, payload);
                    }
                }
            }
        }
    }
}

function encodeMqttLength(len) {
    const bytes = [];
    do {
        let byte = len % 128;
        len = Math.floor(len / 128);
        if (len > 0) byte |= 128;
        bytes.push(byte);
    } while (len > 0);
    return Buffer.from(bytes);
}

function setupLauncherPresence() {
    function getActiveAccountName() {
        try {
            const acc = gameManager.getAccount();
            if (acc && acc.name) return acc.name;
        } catch (e) {}
        return 'LauncherUser';
    }

    function recordUserPresence(userId, isNewCallback) {
        if (!userId) return;
        const isNew = !activeUsers.has(userId);
        activeUsers.set(userId, Date.now());
        updateCountAndBroadcast();
        if (isNew && userId !== launcherClientId) {
            if (isNewCallback) isNewCallback();
        }
    }

    // --- Channel 1: Universal HTTPS SSE Stream via ntfy.sh (Port 443 HTTPS) ---
    function startNtfyStream() {
        if (ntfyStreamReq) {
            try { ntfyStreamReq.destroy(); } catch (e) {}
            ntfyStreamReq = null;
        }

        try {
            const req = https.request({
                hostname: 'ntfy.sh',
                path: '/mooclient_presence_v4/json',
                method: 'GET',
                headers: { 'User-Agent': 'MooClient-Launcher' }
            }, (res) => {
                let buffer = '';
                res.on('data', (chunk) => {
                    buffer += chunk.toString();
                    const lines = buffer.split('\n');
                    buffer = lines.pop();
                    for (const line of lines) {
                        if (!line.trim()) continue;
                        try {
                            const event = JSON.parse(line);
                            if (event.event === 'message' && event.message) {
                                const data = JSON.parse(event.message);
                                const uid = data.id || (data.uuid && data.uuid.length > 5 ? data.uuid : null) || (data.u && data.u.length > 0 ? 'mc_' + data.u.toLowerCase() : null);
                                recordUserPresence(uid, () => {
                                    sendPresencePing();
                                });
                            }
                        } catch (e) {}
                    }
                });

                res.on('end', () => {
                    setTimeout(startNtfyStream, 2000);
                });
            });

            req.on('error', () => {
                setTimeout(startNtfyStream, 3000);
            });

            req.end();
            ntfyStreamReq = req;
        } catch (e) {
            setTimeout(startNtfyStream, 3000);
        }
    }

    function sendNtfyPing() {
        try {
            const payload = JSON.stringify({
                id: launcherClientId,
                u: getActiveAccountName(),
                t: Date.now()
            });

            const req = https.request({
                hostname: 'ntfy.sh',
                path: '/mooclient_presence_v4',
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            req.on('error', () => {});
            req.write(payload);
            req.end();
        } catch (e) {}
    }

    // --- Channel 2: Global TLS MQTT Stream (broker.hivemq.com:8883) ---
    function sendMqttPresence() {
        if (!mqttSocket || !isMqttConnected) return;
        try {
            const topic = 'mooclient/presence_launcher';
            const payload = JSON.stringify({
                id: launcherClientId,
                u: getActiveAccountName(),
                t: Date.now()
            });

            const topicBytes = Buffer.from(topic);
            const payloadBytes = Buffer.from(payload);
            const remain = encodeMqttLength(2 + topicBytes.length + payloadBytes.length);
            const pubPacket = Buffer.concat([
                Buffer.from([0x30]),
                remain,
                Buffer.from([(topicBytes.length >> 8) & 0xFF, topicBytes.length & 0xFF]),
                topicBytes,
                payloadBytes
            ]);
            mqttSocket.write(pubPacket);
        } catch (e) {}
    }

    function sendMqttPing() {
        if (!mqttSocket || !isMqttConnected) return;
        try {
            mqttSocket.write(Buffer.from([0xC0, 0x00])); // PINGREQ
        } catch (e) {}
    }

    function connectMqtt() {
        if (mqttSocket) {
            try { mqttSocket.destroy(); } catch (e) {}
            mqttSocket = null;
        }
        isMqttConnected = false;

        const broker = MQTT_BROKERS[currentBrokerIndex];

        try {
            const parser = new MqttStreamParser((topic, payload) => {
                try {
                    const data = JSON.parse(payload);
                    const userId = data.id || (data.uuid && data.uuid.length > 5 ? data.uuid : null) || (data.u && data.u.length > 0 ? 'mc_' + data.u.toLowerCase() : null);
                    recordUserPresence(userId, () => {
                        sendPresencePing();
                    });
                } catch (e) {}
            });

            const connectOptions = { host: broker.host, port: broker.port, timeout: 6000 };
            const socket = broker.tls
                ? tls.connect(broker.port, broker.host, { rejectUnauthorized: false, timeout: 6000 })
                : net.createConnection(connectOptions);

            mqttSocket = socket;

            socket.on('connect', () => {
                const clientBytes = Buffer.from(launcherClientId);
                const varHeader = Buffer.from([0x00, 0x04, 0x4D, 0x51, 0x54, 0x54, 0x04, 0x02, 0x00, 0x3C]); // Clean Session, 60s
                const rem = encodeMqttLength(varHeader.length + 2 + clientBytes.length);
                const packet = Buffer.concat([
                    Buffer.from([0x10]),
                    rem,
                    varHeader,
                    Buffer.from([(clientBytes.length >> 8) & 0xFF, clientBytes.length & 0xFF]),
                    clientBytes
                ]);
                socket.write(packet);
            });

            let connAcked = false;
            socket.on('data', (buf) => {
                if (!connAcked && buf[0] === 0x20) {
                    connAcked = true;
                    isMqttConnected = true;

                    const topicBytes = Buffer.from('mooclient/#');
                    const subRem = encodeMqttLength(2 + 2 + topicBytes.length + 1);
                    const subPacket = Buffer.concat([
                        Buffer.from([0x82]),
                        subRem,
                        Buffer.from([0x00, 0x01]),
                        Buffer.from([(topicBytes.length >> 8) & 0xFF, topicBytes.length & 0xFF]),
                        topicBytes,
                        Buffer.from([0x00]) // QoS 0
                    ]);
                    socket.write(subPacket);

                    sendMqttPresence();
                }

                parser.feed(buf);
            });

            socket.on('timeout', () => { socket.destroy(); });
            socket.on('error', () => {});
            socket.on('close', () => {
                isMqttConnected = false;
                currentBrokerIndex = (currentBrokerIndex + 1) % MQTT_BROKERS.length;
                scheduleReconnect();
            });
        } catch (e) {
            scheduleReconnect();
        }
    }

    function scheduleReconnect() {
        if (reconnectTimer) clearTimeout(reconnectTimer);
        reconnectTimer = setTimeout(connectMqtt, 3000);
    }

    function sendPresencePing() {
        sendNtfyPing();
        sendMqttPresence();
    }

    function updateCountAndBroadcast() {
        const now = Date.now();
        // Purge inactive entries older than 12 seconds
        for (const [id, lastSeen] of activeUsers.entries()) {
            if (now - lastSeen > 12000) {
                activeUsers.delete(id);
            }
        }
        // Always include self
        activeUsers.set(launcherClientId, now);

        const newCount = Math.max(1, activeUsers.size);
        if (newCount !== launcherOnlineCount) {
            launcherOnlineCount = newCount;
            sendToRenderer('online-users-count', launcherOnlineCount);
        }
    }

    // Start both channels
    startNtfyStream();
    connectMqtt();

    // Broadcast presence every 2.5 seconds
    sendPresencePing();
    presenceTimer = setInterval(() => {
        sendPresencePing();
        updateCountAndBroadcast();
    }, 2500);

    // MQTT Keepalive ping every 25 seconds
    pingTimer = setInterval(sendMqttPing, 25000);

    // Refresh & cleanup active users every 1.5 seconds
    setInterval(updateCountAndBroadcast, 1500);
}

// Helper: send message to renderer
function sendToRenderer(channel, data) {
    if (mainWindow && !mainWindow.isDestroyed()) {
        mainWindow.webContents.send(channel, data);
    }
}

// =============================================
// Local API Server for Game Integration
// =============================================
function startLocalApiServer() {
    const server = http.createServer(async (req, res) => {
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

        if (req.method === 'OPTIONS') {
            res.writeHead(200);
            res.end();
            return;
        }

        if (req.url === '/api/login-microsoft') {
            try {
                const account = await gameManager.loginMicrosoft();
                sendToRenderer('account-updated', account);
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, account }));
            } catch (error) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, error: error.message }));
            }
            return;
        }

        if (req.url === '/api/get-accounts') {
            const data = gameManager.getAllAccounts();
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify(data));
            return;
        }

        res.writeHead(404);
        res.end();
    });

    server.listen(49152, '127.0.0.1', () => {
        console.log('[MooLauncher] Local API server listening on 127.0.0.1:49152');
    }).on('error', (err) => {
        console.log('[MooLauncher] Local API server notice:', err.message);
    });
}

// =============================================
// Single Instance Lock (Only 1 launcher instance allowed)
// =============================================
const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
    app.quit();
} else {
    app.on('second-instance', (event, commandLine, workingDirectory) => {
        if (commandLine.some(arg => arg.includes('--login-microsoft'))) {
            gameManager.loginMicrosoft().then(acc => {
                sendToRenderer('account-updated', acc);
            }).catch(() => {});
            return;
        }

        // Someone tried to run a second instance, focus our main window
        if (mainWindow && !mainWindow.isDestroyed()) {
            if (mainWindow.isMinimized()) {
                mainWindow.restore();
            }
            mainWindow.show();
            mainWindow.focus();
        }
    });

    // =============================================
    // App lifecycle
    // =============================================
    app.whenReady().then(() => {
        createWindow();
        setupIPC();
        setupLauncherPresence();
        startLocalApiServer();
        discordRPC.init();
    });

    app.on('window-all-closed', () => {
        app.quit();
    });

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
}
