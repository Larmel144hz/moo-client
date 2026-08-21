const { contextBridge, ipcRenderer, webUtils } = require('electron');

/**
 * Preload script — exposes a safe API to the renderer process.
 * This is the bridge between Electron's main process and the UI.
 */
contextBridge.exposeInMainWorld('mooAPI', {
    // Window controls
    minimize: () => ipcRenderer.send('window-minimize'),
    maximize: () => ipcRenderer.send('window-maximize'),
    close: () => ipcRenderer.send('window-close'),

    // Game
    launchGame: (options) => ipcRenderer.invoke('launch-game', options),

    // Account & Authentication (Microsoft Premium)
    loginMicrosoft: () => ipcRenderer.invoke('login-microsoft'),
    logoutMicrosoft: () => ipcRenderer.invoke('logout-microsoft'),
    validateSession: () => ipcRenderer.invoke('validate-session'),
    refreshSession: () => ipcRenderer.invoke('refresh-session'),
    getAccount: () => ipcRenderer.invoke('get-account'),
    getAccounts: () => ipcRenderer.invoke('get-accounts'),
    selectAccount: (uuid) => ipcRenderer.invoke('select-account', uuid),
    removeAccount: (uuid) => ipcRenderer.invoke('remove-account', uuid),

    // Settings
    getSettings: () => ipcRenderer.invoke('get-settings'),
    saveSettings: (settings) => ipcRenderer.invoke('save-settings', settings),
    selectJavaPath: () => ipcRenderer.invoke('select-java-path'),

    // Mod info & Modrinth Integration
    getModInfo: () => ipcRenderer.invoke('get-mod-info'),
    searchModrinth: (params) => ipcRenderer.invoke('search-modrinth', params),
    installMod: (projectId) => ipcRenderer.invoke('install-mod', projectId),
    getInstalledMods: () => ipcRenderer.invoke('get-installed-mods'),
    uninstallMod: (filename) => ipcRenderer.invoke('uninstall-mod', filename),
    toggleMod: (filename, enabled) => ipcRenderer.invoke('toggle-mod', { filename, enabled }),
    installLocalMods: (filePaths) => ipcRenderer.invoke('install-local-mods', filePaths),
    saveModFile: (filename, buffer) => ipcRenderer.invoke('save-mod-file', { filename, buffer }),
    getModVersions: (projectId, allVersions = false) => ipcRenderer.invoke('get-mod-versions', { projectId, allVersions }),
    installModVersion: (versionId, oldFilename) => ipcRenderer.invoke('install-mod-version', { versionId, oldFilename }),
    checkModUpdates: () => ipcRenderer.invoke('check-mod-updates'),
    getFilePath: (file) => {
        try {
            if (webUtils && typeof webUtils.getPathForFile === 'function') {
                return webUtils.getPathForFile(file);
            }
        } catch (e) {}
        return file && file.path ? file.path : '';
    },
    openModsFolder: () => ipcRenderer.invoke('open-mods-folder'),

    // Moo Client Core Update
    checkClientUpdate: () => ipcRenderer.invoke('check-client-update'),
    performClientUpdate: () => ipcRenderer.invoke('perform-client-update'),

    // Event listeners
    onLaunchStatus: (callback) => ipcRenderer.on('launch-status', (_, data) => callback(data)),
    onLaunchProgress: (callback) => ipcRenderer.on('launch-progress', (_, data) => callback(data)),
    onUpdaterStatus: (callback) => ipcRenderer.on('updater-status', (_, data) => callback(data)),
    onUpdaterProgress: (callback) => ipcRenderer.on('updater-progress', (_, data) => callback(data)),
    onClientUpdateProgress: (callback) => ipcRenderer.on('client-update-progress', (_, data) => callback(data)),
});
