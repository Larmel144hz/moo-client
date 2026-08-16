const { app } = require('electron');
const msmc = require('msmc');
const fs = require('fs');
const path = require('path');
const os = require('os');

app.whenReady().then(async () => {
    const authManager = new msmc.Auth("select_account");
    try {
        const xbox = await authManager.launch("electron", {
            width: 520,
            height: 680,
            resizable: true,
            center: true,
            alwaysOnTop: true,
            backgroundColor: '#09090b',
            title: "Sign in to Minecraft"
        });
        const token = await xbox.getMinecraft();
        if (token && token.validate && token.validate()) {
            const mclcAuth = token.mclc();
            const account = {
                name: token.profile.name,
                uuid: token.profile.id,
                mclc: mclcAuth,
                type: 'microsoft'
            };

            const homeDir = path.join(os.homedir(), '.mooclient');
            if (!fs.existsSync(homeDir)) fs.mkdirSync(homeDir, { recursive: true });
            const accountsPath = path.join(homeDir, 'accounts.json');
            const accountPath = path.join(homeDir, 'account.json');

            let data = { activeUuid: account.uuid, accounts: [] };
            if (fs.existsSync(accountsPath)) {
                try {
                    const parsed = JSON.parse(fs.readFileSync(accountsPath, 'utf8'));
                    if (parsed && Array.isArray(parsed.accounts)) data = parsed;
                } catch (e) {}
            }
            if (!Array.isArray(data.accounts)) data.accounts = [];

            const idx = data.accounts.findIndex(a => a.uuid === account.uuid || (a.name && account.name && a.name.toLowerCase() === account.name.toLowerCase()));
            if (idx >= 0) {
                data.accounts[idx] = account;
            } else {
                data.accounts.push(account);
            }
            data.activeUuid = account.uuid;

            fs.writeFileSync(accountsPath, JSON.stringify(data, null, 2), 'utf8');
            fs.writeFileSync(accountPath, JSON.stringify(account, null, 2), 'utf8');
            console.log('SUCCESS_AUTH:' + account.name);
        }
    } catch (e) {
        console.error('AUTH_ERROR:', e.message);
    }
    app.quit();
});
