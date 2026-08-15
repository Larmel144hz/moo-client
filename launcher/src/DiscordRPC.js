const net = require('net');

/**
 * Lightweight native Discord Rich Presence IPC client for Moo Client Launcher.
 * Zero external dependencies.
 */
class DiscordRPC {
    constructor(clientId = '1537761004983816222') {
        this.clientId = clientId;
        this.socket = null;
        this.connected = false;
        this.startTimestamp = Math.floor(Date.now() / 1000);
        this.currentDetails = 'Przegląda Launcher';
        this.currentState = 'Wersja 1.0.0';
    }

    init() {
        this.connect();
        // Keep-alive / reconnect loop
        setInterval(() => {
            if (!this.connected) {
                this.connect();
            }
        }, 10000);
    }

    connect() {
        for (let i = 0; i < 10; i++) {
            const pipePath = process.platform === 'win32'
                ? `\\\\.\\pipe\\discord-ipc-${i}`
                : process.env.XDG_RUNTIME_DIR
                ? `${process.env.XDG_RUNTIME_DIR}/discord-ipc-${i}`
                : `/tmp/discord-ipc-${i}`;

            try {
                this.socket = net.createConnection(pipePath, () => {
                    this.connected = true;
                    this.sendHandshake();
                    setTimeout(() => {
                        this.updateActivity(this.currentDetails, this.currentState);
                    }, 500);
                });

                this.socket.on('error', () => {
                    this.connected = false;
                    this.socket = null;
                });

                this.socket.on('close', () => {
                    this.connected = false;
                    this.socket = null;
                });

                if (this.socket) break;
            } catch (e) {}
        }
    }

    sendHandshake() {
        if (!this.socket) return;
        const payload = JSON.stringify({ v: 1, client_id: this.clientId });
        this.sendPacket(0, payload); // Opcode 0 = Handshake
    }

    updateActivity(details, state) {
        this.currentDetails = details;
        this.currentState = state;
        if (!this.connected || !this.socket) return;

        const payload = JSON.stringify({
            cmd: 'SET_ACTIVITY',
            args: {
                pid: process.pid,
                activity: {
                    details: details,
                    state: state,
                    timestamps: {
                        start: this.startTimestamp
                    },
                    assets: {
                        large_image: 'moo_logo',
                        large_text: 'Moo Client v1.0.0 (Fabric 1.21.4)'
                    }
                }
            },
            nonce: Date.now().toString()
        });

        this.sendPacket(1, payload); // Opcode 1 = Frame
    }

    sendPacket(opcode, payload) {
        if (!this.socket) return;
        const payloadBuffer = Buffer.from(payload, 'utf8');
        const header = Buffer.alloc(8);
        header.writeInt32LE(opcode, 0);
        header.writeInt32LE(payloadBuffer.length, 4);
        try {
            this.socket.write(Buffer.concat([header, payloadBuffer]));
        } catch (e) {}
    }
}

module.exports = new DiscordRPC();
