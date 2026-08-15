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
        this.accountsPath = path.join(this.gameDir, 'accounts.json');
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

    getAccountsData() {
        try {
            if (fs.existsSync(this.accountsPath)) {
                const data = JSON.parse(fs.readFileSync(this.accountsPath, 'utf8'));
                if (data && Array.isArray(data.accounts)) return data;
            }
            // Migration from legacy single account.json
            if (fs.existsSync(this.accountPath)) {
                const legacy = JSON.parse(fs.readFileSync(this.accountPath, 'utf8'));
                if (legacy && legacy.name) {
                    const data = {
                        activeUuid: legacy.uuid || legacy.name,
                        accounts: [legacy]
                    };
                    this.saveAccountsData(data);
                    return data;
                }
            }
        } catch (e) {
            console.error('Error reading accounts.json:', e);
        }
        return { activeUuid: null, accounts: [] };
    }

    saveAccountsData(data) {
        try {
            this.ensureDir(this.gameDir);
            fs.writeFileSync(this.accountsPath, JSON.stringify(data, null, 2));

            // Sync active account to account.json for compatibility
            const active = data.accounts.find(a => a.uuid === data.activeUuid) || (data.accounts.length > 0 ? data.accounts[0] : null);
            if (active) {
                fs.writeFileSync(this.accountPath, JSON.stringify(active, null, 2));
            } else if (fs.existsSync(this.accountPath)) {
                fs.unlinkSync(this.accountPath);
            }
        } catch (e) {
            console.error('Error saving accounts.json:', e);
        }
    }

    getAllAccounts() {
        const data = this.getAccountsData();
        return {
            activeUuid: data.activeUuid,
            accounts: data.accounts.map(acc => ({
                name: acc.name,
                uuid: acc.uuid,
                type: acc.type || 'microsoft',
                isActive: acc.uuid === data.activeUuid
            }))
        };
    }

    getAccount() {
        const data = this.getAccountsData();
        if (!data.accounts || data.accounts.length === 0) return null;
        return data.accounts.find(a => a.uuid === data.activeUuid) || data.accounts[0] || null;
    }

    saveAccount(account) {
        const data = this.getAccountsData();
        if (!account) {
            if (data.activeUuid) {
                data.accounts = data.accounts.filter(a => a.uuid !== data.activeUuid);
                data.activeUuid = data.accounts.length > 0 ? data.accounts[0].uuid : null;
            } else {
                data.accounts = [];
                data.activeUuid = null;
            }
        } else {
            const existingIdx = data.accounts.findIndex(a => a.uuid === account.uuid || (a.name && account.name && a.name.toLowerCase() === account.name.toLowerCase()));
            if (existingIdx >= 0) {
                data.accounts[existingIdx] = account;
            } else {
                data.accounts.push(account);
            }
            data.activeUuid = account.uuid;
        }
        this.saveAccountsData(data);
    }

    selectAccount(uuid) {
        const data = this.getAccountsData();
        const found = data.accounts.find(a => a.uuid === uuid);
        if (found) {
            data.activeUuid = found.uuid;
            this.saveAccountsData(data);
            return { success: true, account: found };
        }
        return { success: false, error: 'Konto nie zostało znalezione' };
    }

    removeAccount(uuid) {
        const data = this.getAccountsData();
        data.accounts = data.accounts.filter(a => a.uuid !== uuid);
        if (data.activeUuid === uuid) {
            data.activeUuid = data.accounts.length > 0 ? data.accounts[0].uuid : null;
        }
        this.saveAccountsData(data);
        return { success: true, activeAccount: this.getAccount() };
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

    resolveJavawPath(customPath) {
        if (customPath && fs.existsSync(customPath)) {
            if (process.platform === 'win32' && customPath.toLowerCase().endsWith('java.exe')) {
                const javaw = customPath.slice(0, -8) + 'javaw.exe';
                if (fs.existsSync(javaw)) return javaw;
            }
            return customPath;
        }

        if (process.platform === 'win32') {
            // Check common Java 21 JDK installation directories
            const programFiles = process.env.ProgramFiles || 'C:\\Program Files';
            const candidates = [
                path.join(programFiles, 'Java', 'jdk-21', 'bin', 'javaw.exe'),
                path.join(programFiles, 'Eclipse Adoptium', 'jdk-21', 'bin', 'javaw.exe'),
                path.join(programFiles, 'Microsoft', 'jdk-21', 'bin', 'javaw.exe'),
                path.join(programFiles, 'Zulu', 'zulu-21', 'bin', 'javaw.exe'),
                'C:\\Program Files (x86)\\Minecraft Launcher\\runtime\\java-runtime-gamma\\windows-x64\\java-runtime-gamma\\bin\\javaw.exe'
            ];

            for (const c of candidates) {
                if (fs.existsSync(c)) return c;
            }

            // Fallback to javaw in PATH (silent, no console)
            return 'javaw';
        }

        return 'java';
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

        const javaExecutable = this.resolveJavawPath(settings.javaPath);

        const launchOpts = {
            authorization: auth,
            root: this.gameDir,
            javaPath: javaExecutable,
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
            overrides: {
                detached: true
            }
        };

        launcher.on('debug', (e) => console.log('[MC Debug]', e));
        launcher.on('data', (e) => console.log('[MC Data]', e));
        launcher.on('progress', (e) => {
            const percent = Math.round((e.task / e.total) * 100);
            onProgress(`Pobieranie plików: ${e.type}`, percent);
        });
        launcher.on('download-status', (e) => {
            const percent = Math.round((e.current / e.total) * 100);
            onProgress(`Pobieranie zasobów gry...`, percent);
        });
        launcher.on('arguments', () => onProgress('Uruchamianie silnika gry...', 95));
        launcher.on('close', (code) => onProgress(`Gra zamknięta (kod: ${code})`, 0));

        onProgress('Uruchamianie Minecrafta...', 50);
        await launcher.launch(launchOpts);
        onProgress('Gra została uruchomiona!', 100);
    }
}

module.exports = GameManager;
