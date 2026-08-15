const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron');
const { autoUpdater } = require('electron-updater');
const path = require('path');
const discordRPC = require('./DiscordRPC');

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
// Auto-Updater (Launcher updates from GitHub)
// =============================================
function setupAutoUpdater() {
    autoUpdater.autoDownload = true;
    autoUpdater.autoInstallOnAppQuit = true;

    autoUpdater.on('checking-for-update', () => {
        sendToRenderer('updater-status', 'Checking for launcher updates...');
    });

    autoUpdater.on('update-available', (info) => {
        sendToRenderer('updater-status', `Update available: v${info.version}`);
    });

    autoUpdater.on('update-not-available', () => {
        sendToRenderer('updater-status', 'Launcher is up to date');
    });

    autoUpdater.on('download-progress', (progress) => {
        sendToRenderer('updater-progress', Math.round(progress.percent));
    });

    autoUpdater.on('update-downloaded', (info) => {
        sendToRenderer('updater-status', `Update v${info.version} ready — restarting...`);
        setTimeout(() => autoUpdater.quitAndInstall(), 3000);
    });

    autoUpdater.on('error', (err) => {
        sendToRenderer('updater-status', 'Update check skipped');
        console.log('Auto-updater error (expected in dev):', err.message);
    });

    // Only check in production builds
    if (app.isPackaged) {
        autoUpdater.checkForUpdatesAndNotify();
    } else {
        sendToRenderer('updater-status', 'Dev mode — auto-update skipped');
    }
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
            sendToRenderer('launch-status', 'Checking for mod updates...');
            sendToRenderer('launch-progress', 5);

            // Step 1: Check & update mod from GitHub
            const modUpdated = await modManager.checkAndUpdate((status, progress) => {
                sendToRenderer('launch-status', status);
                sendToRenderer('launch-progress', progress);
            });

            // Step 2: Launch Minecraft with Fabric + mod
            sendToRenderer('launch-status', 'Launching Minecraft...');
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

    ipcMain.handle('logout-microsoft', async () => {
        return gameManager.logout();
    });

    ipcMain.handle('get-account', async () => {
        return gameManager.getAccount();
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
        return modManager.installLocalMods(filePaths);
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

    // --- Moo Client Core Version & Update Check ---
    ipcMain.handle('check-client-update', async () => {
        try {
            const local = modManager.getLocalVersion();
            const remote = await modManager.getRemoteVersion();
            const hasUpdate = (local.version !== remote.version && remote.version && remote.version !== 'none');
            return {
                success: true,
                hasUpdate,
                currentVersion: local.version || '1.0.0',
                latestVersion: remote.version,
                changelog: remote.changelog || '',
                downloadUrl: remote.download_url
            };
        } catch (e) {
            const local = modManager.getLocalVersion();
            return {
                success: false,
                hasUpdate: false,
                currentVersion: local.version || '1.0.0',
                error: e.message
            };
        }
    });

    ipcMain.handle('perform-client-update', async () => {
        try {
            const updated = await modManager.checkAndUpdate((status, percent) => {
                sendToRenderer('client-update-progress', { status, percent });
            });
            const newLocal = modManager.getLocalVersion();
            return { success: true, updated, version: newLocal.version };
        } catch (e) {
            return { success: false, error: e.message };
        }
    });
}

// Helper: send message to renderer
function sendToRenderer(channel, data) {
    if (mainWindow && !mainWindow.isDestroyed()) {
        mainWindow.webContents.send(channel, data);
    }
}

// =============================================
// App lifecycle
// =============================================
app.whenReady().then(() => {
    createWindow();
    setupIPC();
    setupAutoUpdater();
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
