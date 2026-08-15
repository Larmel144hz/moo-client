const { Client, Authenticator } = require('minecraft-launcher-core');
const msmc = require('msmc');
const path = require('path');
const fs = require('fs');
const os = require('os');
const https = require('https');

/**
 * GameManager — handles Minecraft installation, launching and Microsoft Authentication.
 */
class GameManager {
    constructor() {
        this.gameDir = path.join(os.homedir(), '.mooclient');
        this.settingsPath = path.join(this.gameDir, 'settings.json');
        this.accountPath = path.join(this.gameDir, 'account.json');
        this.modsDir = path.join(this.gameDir, 'mods');

        this.ensureDir(this.gameDir);
        this.ensureDir(this.modsDir);

        this.defaultSettings = {
            username: 'MooPlayer',
            ram: '4',
            javaPath: '',
            resolution: { width: 1280, height: 720 },
        };
    }

    ensureDir(dir) {
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir, { recursive: true });
        }
    }

    getSettings() {
        try {
            if (fs.existsSync(this.settingsPath)) {
                const data = fs.readFileSync(this.settingsPath, 'utf8');
                return { ...this.defaultSettings, ...JSON.parse(data) };
            }
        } catch (e) {
            console.error('Error reading settings:', e);
        }
        return { ...this.defaultSettings };
    }

    saveSettings(settings) {
        try {
            this.ensureDir(this.gameDir);
            fs.writeFileSync(this.settingsPath, JSON.stringify(settings, null, 2));
        } catch (e) {
            console.error('Error saving settings:', e);
        }
    }

    getAccount() {
        try {
            if (fs.existsSync(this.accountPath)) {
                const data = fs.readFileSync(this.accountPath, 'utf8');
                return JSON.parse(data);
            }
        } catch (e) {
            console.error('Error reading account:', e);
        }
        return null;
    }

    saveAccount(account) {
        try {
            this.ensureDir(this.gameDir);
            if (account) {
                fs.writeFileSync(this.accountPath, JSON.stringify(account, null, 2));
            } else if (fs.existsSync(this.accountPath)) {
                fs.unlinkSync(this.accountPath);
            }
        } catch (e) {
            console.error('Error saving account:', e);
        }
    }

    /**
     * Log in with official Microsoft Minecraft Account
     */
    async loginMicrosoft() {
        const iconPath = path.join(__dirname, 'renderer', 'logo.png');
        const authManager = new msmc.Auth("select_account");
        const xbox = await authManager.launch("electron", {
            width: 520,
            height: 680,
            resizable: true,
            center: true,
            alwaysOnTop: true,
            backgroundColor: '#09090b',
            title: "Moo Client — Logowanie Microsoft",
            icon: iconPath,
        });
        const token = await xbox.getMinecraft();

        if (!token.validate()) {
            throw new Error('Nie udało się zweryfikować konta Minecraft!');
        }

        const mclcAuth = token.mclc();
        const account = {
            name: token.profile.name,
            uuid: token.profile.id,
            mclc: mclcAuth,
            type: 'microsoft'
        };

        this.saveAccount(account);
        return account;
    }

    logout() {
        this.saveAccount(null);
        return { success: true };
    }

    async getLatestStableLoader() {
        return new Promise((resolve) => {
            https.get('https://meta.fabricmc.net/v2/versions/loader', { headers: { 'User-Agent': 'MooClient-Launcher/1.0.0' } }, (res) => {
                let data = '';
                res.on('data', c => data += c);
                res.on('end', () => {
                    try {
                        const list = JSON.parse(data);
                        const stable = list.find(v => v.stable) || list[0];
                        resolve(stable?.version || '0.19.3');
                    } catch (e) {
                        resolve('0.19.3');
                    }
                });
            }).on('error', () => resolve('0.19.3'));
        });
    }

    async ensureFabricVersion(gameVersion = '1.21.4') {
        const loaderVersion = await this.getLatestStableLoader();
        const customVersionName = `fabric-loader-${gameVersion}`;
        const versionDir = path.join(this.gameDir, 'versions', customVersionName);
        const versionJsonPath = path.join(versionDir, `${customVersionName}.json`);

        // If cached profile exists, verify it uses the latest loader version
        if (fs.existsSync(versionJsonPath)) {
            try {
                const existing = JSON.parse(fs.readFileSync(versionJsonPath, 'utf8'));
                const hasLoader = existing.libraries?.some(lib => lib.name?.includes(`net.fabricmc:fabric-loader:${loaderVersion}`));
                if (hasLoader) {
                    return customVersionName;
                }
            } catch (e) {}
        }

        fs.mkdirSync(versionDir, { recursive: true });
        const url = `https://meta.fabricmc.net/v2/versions/loader/${gameVersion}/${loaderVersion}/profile/json`;

        return new Promise((resolve, reject) => {
            https.get(url, { headers: { 'User-Agent': 'MooClient-Launcher/1.0.0' } }, (res) => {
                let data = '';
                res.on('data', chunk => data += chunk);
                res.on('end', () => {
                    try {
                        const json = JSON.parse(data);
                        json.id = customVersionName;
                        fs.writeFileSync(versionJsonPath, JSON.stringify(json, null, 2));
                        resolve(customVersionName);
                    } catch (e) {
                        reject(e);
                    }
                });
            }).on('error', reject);
        });
    }

    /**
     * Launch Minecraft with Fabric and the Moo Client mod.
     */
    async launch(options = {}, onProgress = () => {}) {
        const settings = this.getSettings();
        const ram = options.ram || settings.ram || '4';
        const versionNumber = options.version || '1.21.4';

        const launcher = new Client();

        // Authorization: strictly require Microsoft Account
        const account = this.getAccount();
        if (!account || !account.mclc) {
            throw new Error('Musisz być zalogowany kontem Microsoft Premium, aby zagrać!');
        }
        const auth = account.mclc;

        onProgress('Przygotowywanie profilu Fabric...', 30);
        const customFabric = await this.ensureFabricVersion(versionNumber);

        const launchOpts = {
            authorization: auth,
            root: this.gameDir,
            version: {
                number: versionNumber,
                type: 'release',
                custom: customFabric,
            },
            memory: {
                max: `${ram}G`,
                min: '2G',
            },
            window: {
                width: settings.resolution?.width || 1280,
                height: settings.resolution?.height || 720,
            },
        };

        if (settings.javaPath && fs.existsSync(settings.javaPath)) {
            launchOpts.javaPath = settings.javaPath;
        }

        launcher.on('debug', (e) => console.log('[MC Debug]', e));
        launcher.on('data', (e) => console.log('[MC Data]', e));
        launcher.on('progress', (e) => {
            const percent = Math.round((e.task / e.total) * 100);
            onProgress(`Downloading: ${e.type}`, percent);
        });
        launcher.on('download-status', (e) => {
            const percent = Math.round((e.current / e.total) * 100);
            onProgress(`Downloading game files...`, percent);
        });
        launcher.on('arguments', () => onProgress('Game starting...', 95));
        launcher.on('close', (code) => onProgress(`Game closed (code: ${code})`, 0));

        onProgress('Preparing to launch...', 50);
        await launcher.launch(launchOpts);
        onProgress('Game is running!', 100);
    }
}

module.exports = GameManager;
