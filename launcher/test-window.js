const { app, BrowserWindow } = require('electron');

app.whenReady().then(() => {
    const win = new BrowserWindow({
        width: 800,
        height: 600,
        backgroundColor: '#ff0000',
        alwaysOnTop: true,
        title: 'MOO CLIENT TEST',
    });
    win.loadURL('data:text/html,<h1 style="color:white;background:red;padding:50px;font-size:60px">MOO CLIENT WORKS!</h1>');
    win.show();
    win.focus();
});
