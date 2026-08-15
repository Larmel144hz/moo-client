/**
 * Moo Client Launcher — Renderer Logic
 * Language system (PL/EN), particles, navigation, play, settings & Microsoft Auth
 */

// =============================================
// i18n — Polish / English
// =============================================
const translations = {
    pl: {
        nav_home: 'Strona główna',
        nav_settings: 'Ustawienia',
        status_ready: 'Gotowy',
        btn_play: 'GRAJ',
        status_ready_play: 'Gotowy do gry',
        settings_title: 'Ustawienia',
        settings_subtitle: 'Skonfiguruj swoje doświadczenie Moo Client',
        setting_account: 'Konto Microsoft',
        setting_account_desc: 'Oficjalne logowanie Premium',
        btn_login_ms: 'Zaloguj przez Microsoft',
        btn_logging_in: 'Logowanie...',
        btn_logout: 'Wyloguj',
        setting_ram: 'Pamięć RAM',
        setting_ram_desc: 'Pamięć przydzielona do Minecraft',
        setting_java: 'Ścieżka Java',
        setting_java_desc: 'Własna instalacja Java (puste = auto)',
        setting_resolution: 'Rozdzielczość',
        setting_resolution_desc: 'Rozmiar okna gry',
        btn_browse: 'Przeglądaj',
        btn_save: 'Zapisz ustawienia',
        btn_saved: '✓ Zapisano!',
        launching: 'URUCHAMIANIE...',
        running: 'DZIAŁA',
        game_running: 'Gra została uruchomiona!',
        game_starting: 'Startowanie gry...',
        preparing_launch: 'Przygotowywanie do startu...',
        checking_updates: 'Sprawdzanie aktualizacji moda...',
        launching_mc: 'Uruchamianie Minecraft...',
        downloading_files: 'Pobieranie plików gry...',
        downloading_assets: 'Pobieranie zasobów...',
        game_closed: 'Gra została zamknięta',
        launch_failed: 'Nie udało się uruchomić',
        preparing: 'Przygotowywanie...',
        not_logged_in: 'Brak konta',
        nav_mods: 'Modyfikacje',
        mods_title: 'Modyfikacje',
        mods_subtitle: 'Przeglądaj i instaluj mody z Modrinth dla Fabric 1.21.4',
        btn_open_folder: 'Otwórz folder modów',
        search_mods_placeholder: 'Szukaj modów na Modrinth...',
        search_installed_placeholder: 'Filtruj zainstalowane mody...',
        tab_browse: 'Przeglądaj (Modrinth)',
        tab_installed: 'Zainstalowane',
        btn_install: 'Zainstaluj',
        btn_installing: 'Instalowanie...',
        btn_installed: '✓ Zainstalowano',
        btn_uninstall: 'Usuń',
        loading_mods: 'Ładowanie modyfikacji z Modrinth...',
        no_mods_found: 'Nie znaleziono modyfikacji.',
        no_installed_mods: 'Brak dodatkowo zainstalowanych modów.',
        core_mod: 'Główny mod',
        downloads_count: 'pobrań',
        dropzone_title: 'Upuść pliki .jar tutaj',
        dropzone_subtitle: 'Mody zostaną automatycznie dodane do gry',
        mods_drag_installed: 'Pomyślnie dodano mody!',
        btn_check_updates: 'Sprawdź aktualizacje',
        btn_update: 'Aktualizuj',
        btn_updating: 'Aktualizowanie...',
        btn_versions: 'Wersje',
        version_modal_title: 'Wybierz wersję modyfikacji',
        version_modal_subtitle: 'Wybierz wersję z Modrinth dla Fabric 1.21.4',
        no_updates_found: 'Wszystkie mody są aktualne! ✓',
        btn_pick_install: 'Zainstaluj tę wersję',
        update_up_to_date: 'Najnowsza',
        update_available_short: 'Aktualizacja!',
        update_checking: 'Sprawdzanie...',
        update_modal_title: 'Dostępna nowa wersja Moo Client!',
        update_changelog_title: 'Co nowego w tej wersji:',
        btn_update_now: 'Zaktualizuj teraz',
        btn_update_later: 'Przypomnij później',
        update_downloading: 'Pobieranie aktualizacji...',
        update_success_msg: 'Moo Client został pomyślnie zaktualizowany!',
        online_users_suffix: 'graczy online',
        bg_theme_label: 'TŁO:',
        accounts_title: 'Konta Premium',
        btn_add_account: 'Dodaj konto',
        account_active: 'Aktywne',
    },
    en: {
        nav_home: 'Home',
        nav_settings: 'Settings',
        nav_mods: 'Mods',
        status_ready: 'Ready',
        btn_play: 'PLAY',
        status_ready_play: 'Ready to play',
        settings_title: 'Settings',
        settings_subtitle: 'Configure your Moo Client experience',
        setting_account: 'Microsoft Account',
        setting_account_desc: 'Official Premium Login',
        btn_login_ms: 'Login with Microsoft',
        btn_logging_in: 'Logging in...',
        btn_logout: 'Logout',
        setting_ram: 'RAM Allocation',
        setting_ram_desc: 'Memory allocated to Minecraft',
        setting_java: 'Java Path',
        setting_java_desc: 'Custom Java installation (empty = auto)',
        setting_resolution: 'Resolution',
        setting_resolution_desc: 'Game window size',
        btn_browse: 'Browse',
        btn_save: 'Save Settings',
        btn_saved: '✓ Saved!',
        launching: 'LAUNCHING...',
        running: 'RUNNING',
        game_running: 'Game is running!',
        game_starting: 'Starting game...',
        preparing_launch: 'Preparing to launch...',
        checking_updates: 'Checking for mod updates...',
        launching_mc: 'Launching Minecraft...',
        downloading_files: 'Downloading game files...',
        downloading_assets: 'Downloading assets...',
        game_closed: 'Game was closed',
        launch_failed: 'Launch failed',
        preparing: 'Preparing...',
        not_logged_in: 'No account',
        require_login_status: 'Click to log in',
        require_login_alert: 'To play Moo Client, you must first log in with a Microsoft Premium account!',
        mods_title: 'Modifications',
        mods_subtitle: 'Browse and install mods from Modrinth for Fabric 1.21.4',
        btn_open_folder: 'Open Mods Folder',
        search_mods_placeholder: 'Search mods on Modrinth...',
        search_installed_placeholder: 'Filter installed mods...',
        tab_browse: 'Browse (Modrinth)',
        tab_installed: 'Installed',
        btn_install: 'Install',
        btn_installing: 'Installing...',
        btn_installed: '✓ Installed',
        btn_uninstall: 'Uninstall',
        loading_mods: 'Loading mods from Modrinth...',
        no_mods_found: 'No mods found.',
        no_installed_mods: 'No additional mods installed.',
        core_mod: 'Core Mod',
        downloads_count: 'downloads',
        dropzone_title: 'Drop .jar files here',
        dropzone_subtitle: 'Mods will be automatically added to the game',
        mods_drag_installed: 'Successfully added mods!',
        btn_check_updates: 'Check for Updates',
        btn_update: 'Update',
        btn_updating: 'Updating...',
        btn_versions: 'Versions',
        version_modal_title: 'Select Mod Version',
        version_modal_subtitle: 'Choose a version from Modrinth for Fabric 1.21.4',
        no_updates_found: 'All mods are up to date! ✓',
        btn_pick_install: 'Install this version',
        update_up_to_date: 'Up to date',
        update_available_short: 'Update Available!',
        update_checking: 'Checking...',
        update_modal_title: 'New Moo Client Version Available!',
        update_changelog_title: "What's new in this version:",
        btn_update_now: 'Update Now',
        btn_update_later: 'Later',
        update_downloading: 'Downloading update...',
        update_success_msg: 'Moo Client updated successfully!',
        btn_check_updates: 'Check for Updates',
        btn_update: 'Update',
        btn_updating: 'Updating...',
        btn_versions: 'Versions',
        version_modal_title: 'Select Mod Version',
        version_modal_subtitle: 'Select a version from Modrinth for Fabric 1.21.4',
        no_updates_found: 'All mods are up to date! ✓',
        btn_pick_install: 'Install this version',
        online_users_suffix: 'players online',
        bg_theme_label: 'BG:',
        accounts_title: 'Premium Accounts',
        btn_add_account: 'Add Account',
        account_active: 'Active',
    },
};

let currentLang = 'pl';

function setLanguage(lang) {
    currentLang = lang;
    const dict = translations[lang];

    document.querySelectorAll('[data-i18n]').forEach((el) => {
        const key = el.getAttribute('data-i18n');
        if (dict[key]) {
            el.textContent = dict[key];
        }
    });

    document.querySelectorAll('[data-i18n-placeholder]').forEach((el) => {
        const key = el.getAttribute('data-i18n-placeholder');
        if (dict[key]) {
            el.placeholder = dict[key];
        }
    });

    document.querySelectorAll('.lang-btn').forEach((btn) => {
        btn.classList.toggle('active', btn.dataset.lang === lang);
    });

    localStorage.setItem('moo-lang', lang);
    updateOnlineUsersDisplay();
}

function t(key) {
    return translations[currentLang][key] || key;
}

// =============================================
// Real-Time Online Players Counter (Live Presence)
// =============================================
let onlineCount = 1;

function updateOnlineUsersDisplay(count) {
    if (typeof count === 'number' && !isNaN(count)) {
        onlineCount = Math.max(1, count);
    }
    const countEl = document.getElementById('online-users-count');
    if (!countEl) return;
    const suffix = t('online_users_suffix');
    countEl.textContent = `${onlineCount} ${suffix}`;
}

function initOnlineUsersCounter() {
    updateOnlineUsersDisplay(1);

    // Get current count from backend process
    window.mooAPI?.getOnlineUsersCount?.().then((count) => {
        if (typeof count === 'number') updateOnlineUsersDisplay(count);
    }).catch(() => {});

    // Listen for live broadcast updates
    window.mooAPI?.onOnlineUsersCount?.((count) => {
        updateOnlineUsersDisplay(count);
    });
}

// =============================================
// Background Theme Switcher (Classic vs Momomo Video)
// =============================================
function initBackgroundTheme() {
    const bgVideo = document.getElementById('bg-video');
    const bgOverlay = document.getElementById('bg-video-overlay');
    const particlesCanvas = document.getElementById('particles-canvas');
    const btnClassic = document.getElementById('btn-bg-classic');
    const btnVideo = document.getElementById('btn-bg-video');

    function applyBgTheme(theme) {
        localStorage.setItem('moo_bg_theme', theme);
        if (theme === 'video') {
            btnVideo?.classList.add('active');
            btnClassic?.classList.remove('active');
            if (bgVideo) {
                bgVideo.muted = true;
                bgVideo.playsInline = true;
                bgVideo.classList.remove('hidden');
                bgVideo.style.display = 'block';
                bgVideo.play().catch((err) => {
                    const playOnInteraction = () => {
                        bgVideo.play().catch(() => {});
                        window.removeEventListener('click', playOnInteraction);
                        window.removeEventListener('keydown', playOnInteraction);
                    };
                    window.addEventListener('click', playOnInteraction);
                    window.addEventListener('keydown', playOnInteraction);
                });
            }
            if (bgOverlay) {
                bgOverlay.classList.remove('hidden');
                bgOverlay.style.display = 'block';
            }
            if (particlesCanvas) {
                particlesCanvas.classList.add('hidden');
                particlesCanvas.style.display = 'none';
            }
        } else {
            btnClassic?.classList.add('active');
            btnVideo?.classList.remove('active');
            if (bgVideo) {
                bgVideo.pause();
                bgVideo.classList.add('hidden');
                bgVideo.style.display = 'none';
            }
            if (bgOverlay) {
                bgOverlay.classList.add('hidden');
                bgOverlay.style.display = 'none';
            }
            if (particlesCanvas) {
                particlesCanvas.classList.remove('hidden');
                particlesCanvas.style.display = 'block';
            }
        }
    }

    btnClassic?.addEventListener('click', () => applyBgTheme('classic'));
    btnVideo?.addEventListener('click', () => applyBgTheme('video'));

    const savedTheme = localStorage.getItem('moo_bg_theme') || 'video';
    applyBgTheme(savedTheme);
}

// =============================================
// Particles Background
// =============================================
function initParticles() {
    const canvas = document.getElementById('particles-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let particles = [];
    let w, h;

    function resize() {
        w = canvas.width = window.innerWidth;
        h = canvas.height = window.innerHeight;
    }

    function createParticles() {
        particles = [];
        const count = Math.floor((w * h) / 12000);
        for (let i = 0; i < count; i++) {
            particles.push({
                x: Math.random() * w,
                y: Math.random() * h,
                r: Math.random() * 1.5 + 0.3,
                dx: (Math.random() - 0.5) * 0.3,
                dy: (Math.random() - 0.5) * 0.3,
                opacity: Math.random() * 0.4 + 0.1,
            });
        }
    }

    function draw() {
        ctx.clearRect(0, 0, w, h);
        for (const p of particles) {
            p.x += p.dx;
            p.y += p.dy;

            if (p.x < 0) p.x = w;
            if (p.x > w) p.x = 0;
            if (p.y < 0) p.y = h;
            if (p.y > h) p.y = 0;

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(255, 255, 255, ${p.opacity * 0.75})`;
            ctx.fill();
        }
        requestAnimationFrame(draw);
    }

    window.addEventListener('resize', () => { resize(); createParticles(); });
    resize();
    createParticles();
    draw();
}

// =============================================
// Navigation
// =============================================
const navItems = document.querySelectorAll('.nav-item');
const pages = document.querySelectorAll('.page');

navItems.forEach((item) => {
    item.addEventListener('click', () => {
        const target = item.dataset.page;
        navItems.forEach((n) => n.classList.remove('active'));
        item.classList.add('active');
        pages.forEach((p) => p.classList.remove('active'));
        document.getElementById(`page-${target}`)?.classList.add('active');

        if (target === 'mods') {
            if (currentModsTab === 'browse') {
                searchAndRenderMods(modsSearchInput?.value || '');
            } else {
                refreshInstalledMods().then(() => renderInstalledModsView(''));
            }
            checkAndApplyUpdatesSilently(false);
        }
    });
});

// =============================================
// Window Controls
// =============================================
document.getElementById('btn-minimize')?.addEventListener('click', () => window.mooAPI?.minimize());
document.getElementById('btn-maximize')?.addEventListener('click', () => window.mooAPI?.maximize());
document.getElementById('btn-close')?.addEventListener('click', () => window.mooAPI?.close());

// =============================================
// Language Toggle
// =============================================
document.querySelectorAll('.lang-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
        setLanguage(btn.dataset.lang);
    });
});

// =============================================
// Microsoft Account & Profile
// =============================================
const btnLoginMS = document.getElementById('btn-login-ms');
const btnLogoutAccount = document.getElementById('btn-logout-account');
const accountLoggedOut = document.getElementById('account-logged-out');
const accountLoggedIn = document.getElementById('account-logged-in');
const accountUsername = document.getElementById('account-username');
const accountAvatarImg = document.getElementById('account-avatar-img');
const playerNameEl = document.getElementById('player-name');
const playerAvatarImgs = document.querySelectorAll('.avatar-img');
const btnCardLogout = document.getElementById('btn-card-logout');
const playerCard = document.getElementById('player-card');
const playerCardWrapper = document.getElementById('player-card-wrapper');
const accountsPopup = document.getElementById('accounts-popup');
const accountsPopupList = document.getElementById('accounts-popup-list');
const accountsPopupCount = document.getElementById('accounts-popup-count');
const btnPopupAdd = document.getElementById('btn-popup-add');

let currentAccount = null;

function updateAccountUI(account) {
    currentAccount = account;
    if (account && account.name) {
        // Logged in state
        playerCard?.classList.add('clickable');
        btnCardLogout?.classList.remove('hidden');

        if (playerNameEl) playerNameEl.textContent = account.name;
        if (playerStatus) playerStatus.textContent = t('status_ready_play');

        const avatarUrl = `https://mc-heads.net/avatar/${account.name}/64`;
        playerAvatarImgs.forEach((img) => { img.src = avatarUrl; });
    } else {
        // Logged out state
        closeAccountsPopup();
        btnCardLogout?.classList.add('hidden');

        if (playerNameEl) playerNameEl.textContent = t('not_logged_in');
        if (playerStatus) playerStatus.textContent = t('require_login_status');
        playerAvatarImgs.forEach((img) => { img.src = 'logo.png'; });
    }
}

async function renderAccountsPopup() {
    if (!accountsPopupList) return;
    accountsPopupList.innerHTML = '';

    try {
        const data = await window.mooAPI?.getAccounts?.() || { activeUuid: null, accounts: [] };
        const accounts = data.accounts || [];

        if (accountsPopupCount) {
            accountsPopupCount.textContent = accounts.length === 1 ? '1 konto' : `${accounts.length} kont`;
        }

        if (accounts.length === 0) {
            accountsPopupList.innerHTML = `<div style="text-align:center; padding: 12px; font-size: 11px; color: var(--text-muted);">Brak zapisanych kont</div>`;
            return;
        }

        accounts.forEach((acc) => {
            const item = document.createElement('div');
            item.className = `account-item ${acc.isActive ? 'active' : ''}`;
            const avatarUrl = `https://mc-heads.net/avatar/${acc.name}/32`;

            item.innerHTML = `
                <div class="account-item-avatar">
                    <img src="${avatarUrl}" alt="${acc.name}" onerror="this.src='logo.png'">
                </div>
                <div class="account-item-details">
                    <span class="account-item-name">${acc.name}</span>
                    <span class="account-item-badge">${acc.isActive ? `<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg> ${t('account_active')}` : 'Premium'}</span>
                </div>
                ${acc.isActive ? `<svg class="account-item-check" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>` : ''}
                <button class="account-item-remove" title="Usuń to konto" type="button">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12"/></svg>
                </button>
            `;

            // Click to switch active account
            item.addEventListener('click', async (e) => {
                if (e.target.closest('.account-item-remove')) return;
                if (!acc.isActive) {
                    try {
                        const res = await window.mooAPI?.selectAccount(acc.uuid);
                        if (res?.success && res.account) {
                            updateAccountUI(res.account);
                            closeAccountsPopup();
                            showToast(`Przełączono na konto ${res.account.name}!`, 'success');
                        }
                    } catch (err) {
                        console.error('Select account error:', err);
                    }
                } else {
                    closeAccountsPopup();
                }
            });

            // Remove specific account
            const btnRemove = item.querySelector('.account-item-remove');
            btnRemove?.addEventListener('click', async (e) => {
                e.stopPropagation();
                try {
                    const res = await window.mooAPI?.removeAccount(acc.uuid);
                    if (res?.success) {
                        showToast(`Usunięto konto ${acc.name}`, 'info');
                        updateAccountUI(res.activeAccount);
                        await renderAccountsPopup();
                    }
                } catch (err) {
                    console.error('Remove account error:', err);
                }
            });

            accountsPopupList.appendChild(item);
        });

    } catch (e) {
        console.error('Error rendering accounts popup:', e);
    }
}

function openAccountsPopup() {
    if (!accountsPopup) return;
    accountsPopup.classList.remove('hidden');
    playerCardWrapper?.classList.add('open');
    renderAccountsPopup();
}

function closeAccountsPopup() {
    if (!accountsPopup) return;
    accountsPopup.classList.add('hidden');
    playerCardWrapper?.classList.remove('open');
}

function toggleAccountsPopup() {
    if (accountsPopup?.classList.contains('hidden')) {
        openAccountsPopup();
    } else {
        closeAccountsPopup();
    }
}

// Click outside closes accounts popup
document.addEventListener('click', (e) => {
    if (playerCardWrapper && !playerCardWrapper.contains(e.target)) {
        closeAccountsPopup();
    }
});

// Add new account from popup
btnPopupAdd?.addEventListener('click', async (e) => {
    e.stopPropagation();
    try {
        const res = await window.mooAPI?.loginMicrosoft();
        if (res?.success && res.account) {
            updateAccountUI(res.account);
            closeAccountsPopup();
            showToast(`Dodano konto ${res.account.name}!`, 'success');
        } else if (res?.error && res.error !== 'error.gui.closed') {
            showToast(`Błąd: ${res.error}`, 'error');
        }
    } catch (err) {
        console.error('Add account error:', err);
    }
});

btnCardLogout?.addEventListener('click', async (e) => {
    e.stopPropagation();
    try {
        await window.mooAPI?.logoutMicrosoft();
        updateAccountUI(null);
    } catch (err) {
        console.error('Logout failed:', err);
    }
});

// Click on player card: if logged out, logs in. If logged in, opens account switcher!
playerCard?.addEventListener('click', async () => {
    if (!currentAccount) {
        if (playerStatus) playerStatus.textContent = t('btn_logging_in');
        playerCard.style.pointerEvents = 'none';
        try {
            const res = await window.mooAPI?.loginMicrosoft();
            if (res?.success && res.account) {
                updateAccountUI(res.account);
                showToast(`Zalogowano jako ${res.account.name}!`, 'success');
            } else if (res?.error && res.error !== 'error.gui.closed') {
                showToast(`Błąd logowania: ${res.error}`, 'error');
            }
        } catch (e) {
            console.error('Login failed:', e);
        } finally {
            playerCard.style.pointerEvents = 'auto';
            updateAccountUI(currentAccount);
        }
    } else {
        toggleAccountsPopup();
    }
});

btnLoginMS?.addEventListener('click', async () => {
    const span = btnLoginMS.querySelector('span');
    const originalText = span ? span.textContent : '';
    if (span) span.textContent = t('btn_logging_in');
    btnLoginMS.disabled = true;

    try {
        const res = await window.mooAPI?.loginMicrosoft();
        if (res?.success && res.account) {
            updateAccountUI(res.account);
            showToast(`Zalogowano jako ${res.account.name}!`, 'success');
        } else if (res?.error && res.error !== 'error.gui.closed') {
            showToast(`Błąd logowania Microsoft: ${res.error}`, 'error');
        }
    } catch (e) {
        console.error('Microsoft login failed:', e);
    } finally {
        if (span) span.textContent = originalText || t('btn_login_ms');
        btnLoginMS.disabled = false;
        updateAccountUI(currentAccount);
    }
});

btnLogoutAccount?.addEventListener('click', async () => {
    try {
        await window.mooAPI?.logoutMicrosoft();
        updateAccountUI(null);
    } catch (e) {
        console.error('Logout failed:', e);
    }
});

async function loadAccount() {
    try {
        const account = await window.mooAPI?.getAccount();
        updateAccountUI(account);
    } catch (e) {
        console.error('Failed to load account:', e);
    }
}

// =============================================
// Play Button
// =============================================
const playBtn = document.getElementById('btn-play');
const progressSection = document.getElementById('progress-section');
const progressFill = document.getElementById('progress-fill');
const progressGlow = document.getElementById('progress-glow');
const progressStatus = document.getElementById('progress-status');
const progressPercent = document.getElementById('progress-percent');
const playerStatus = document.getElementById('player-status');

let isLaunching = false;
const versionSelect = document.getElementById('version-select');
const versionDropdown = document.getElementById('version-dropdown');
const dropdownTrigger = document.getElementById('version-dropdown-trigger');
const selectedVersionText = document.getElementById('selected-version-text');
const dropdownItems = document.querySelectorAll('.dropdown-item');

const modsVersionDropdown = document.getElementById('mods-version-dropdown');
const modsDropdownTrigger = document.getElementById('mods-version-dropdown-trigger');
const modsSelectedVersionText = document.getElementById('mods-selected-version-text');
const modsDropdownItems = document.querySelectorAll('#mods-version-dropdown .dropdown-item');

modsDropdownTrigger?.addEventListener('click', (e) => {
    e.stopPropagation();
    modsVersionDropdown?.classList.toggle('open');
});

modsDropdownItems.forEach((item) => {
    item.addEventListener('click', (e) => {
        e.stopPropagation();
        const val = item.dataset.value;
        const text = item.querySelector('span')?.textContent || val;
        if (versionSelect) versionSelect.value = val;
        if (selectedVersionText) selectedVersionText.textContent = text;
        if (modsSelectedVersionText) modsSelectedVersionText.textContent = text;
        dropdownItems.forEach((i) => i.classList.toggle('active', i.dataset.value === val));
        modsDropdownItems.forEach((i) => i.classList.toggle('active', i.dataset.value === val));
        modsVersionDropdown?.classList.remove('open');
    });
});

dropdownItems.forEach((item) => {
    item.addEventListener('click', (e) => {
        e.stopPropagation();
        const val = item.dataset.value;
        const text = item.querySelector('span')?.textContent || val;
        if (versionSelect) versionSelect.value = val;
        if (selectedVersionText) selectedVersionText.textContent = text;
        if (modsSelectedVersionText) modsSelectedVersionText.textContent = text;
        dropdownItems.forEach((i) => i.classList.toggle('active', i.dataset.value === val));
        modsDropdownItems.forEach((i) => i.classList.toggle('active', i.dataset.value === val));
        versionDropdown?.classList.remove('open');
    });
});

document.addEventListener('click', () => {
    versionDropdown?.classList.remove('open');
    modsVersionDropdown?.classList.remove('open');
});

playBtn?.addEventListener('click', async () => {
    if (!currentAccount || !currentAccount.name) {
        showInfoModal(
            'Wymagane logowanie',
            t('require_login_alert'),
            'warning'
        );
        document.getElementById('nav-settings')?.click();
        return;
    }

    if (isLaunching) return;
    isLaunching = true;

    const selectedVersion = versionSelect?.value || '1.21.4';

    playBtn.classList.add('launching');
    playBtn.querySelector('.play-text').textContent = t('launching');
    progressSection?.classList.remove('hidden');
    if (playerStatus) playerStatus.textContent = t('launching');

    try {
        const result = await window.mooAPI?.launchGame({ version: selectedVersion });
        if (result?.success) {
            if (playerStatus) playerStatus.textContent = t('game_running');
            playBtn.querySelector('.play-text').textContent = t('running');
        } else {
            if (playerStatus) playerStatus.textContent = `Error: ${result?.error}`;
            resetPlayButton();
        }
    } catch (error) {
        if (playerStatus) playerStatus.textContent = t('launch_failed');
        resetPlayButton();
    }
});

function resetPlayButton() {
    isLaunching = false;
    playBtn?.classList.remove('launching');
    if (playBtn) playBtn.querySelector('.play-text').textContent = t('btn_play');
    setTimeout(() => {
        if (!isLaunching) {
            progressSection?.classList.add('hidden');
        }
    }, 2000);
}

function isClosedOrError(status) {
    if (!status) return false;
    const s = status.toLowerCase();
    return s.includes('closed') || s.includes('zamknięta') || s.includes('zamknięto') || s.includes('error') || s.includes('błąd') || s.includes('failed') || s.includes('kod:');
}

function translateStatus(status) {
    if (!status) return '';
    const s = status.toLowerCase();
    if (s.includes('game is running') || s.includes('gra uruchomiona') || s.includes('gra została uruchomiona')) return t('game_running');
    if (s.includes('game starting') || s.includes('startowanie gry')) return t('game_starting');
    if (s.includes('preparing to launch') || s.includes('przygotowywanie do startu') || s.includes('przygotowywanie profilu')) return t('preparing_launch');
    if (s.includes('checking for mod updates') || s.includes('sprawdzanie aktualizacji')) return t('checking_updates');
    if (s.includes('launching minecraft') || s.includes('uruchamianie minecraft')) return t('launching_mc');
    if (s.includes('downloading game files') || s.includes('pobieranie plików')) return t('downloading_files');
    if (s.includes('downloading:')) return status.replace(/Downloading:/i, t('downloading_assets') + ':');
    if (s.includes('downloading') || s.includes('pobieranie')) return t('downloading_files');
    if (s.includes('game closed') || s.includes('closed') || s.includes('zamknięta') || s.includes('zamknięto')) return t('game_closed');
    return status;
}

// =============================================
// Progress & Status Listeners
// =============================================
if (window.mooAPI) {
    window.mooAPI.onLaunchStatus?.((status) => {
        const translated = translateStatus(status);
        if (progressStatus) progressStatus.textContent = translated;
        if (isClosedOrError(status)) {
            if (playerStatus) playerStatus.textContent = t('status_ready_play');
            resetPlayButton();
        } else {
            if (playerStatus) playerStatus.textContent = translated;
        }
    });

    window.mooAPI.onLaunchProgress?.((percent) => {
        if (progressFill) progressFill.style.width = `${percent}%`;
        if (progressGlow) progressGlow.style.width = `${percent}%`;
        if (progressPercent) progressPercent.textContent = `${percent}%`;
    });

    window.mooAPI.onUpdaterStatus?.((status) => {
        const el = document.querySelector('#update-status span');
        if (el) el.textContent = status;
    });
}

// =============================================
// Settings
// =============================================
const settingRam = document.getElementById('setting-ram');
const ramValue = document.getElementById('ram-value');
const settingJava = document.getElementById('setting-java');
const settingWidth = document.getElementById('setting-width');
const settingHeight = document.getElementById('setting-height');
const btnSave = document.getElementById('btn-save-settings');
const btnBrowseJava = document.getElementById('btn-browse-java');

settingRam?.addEventListener('input', () => {
    if (ramValue) ramValue.textContent = `${settingRam.value} GB`;
});

btnBrowseJava?.addEventListener('click', async () => {
    const path = await window.mooAPI?.selectJavaPath();
    if (path && settingJava) settingJava.value = path;
});

async function loadSettings() {
    try {
        const s = await window.mooAPI?.getSettings();
        if (s) {
            if (settingRam) settingRam.value = s.ram || '4';
            if (ramValue) ramValue.textContent = `${s.ram || 4} GB`;
            if (settingJava) settingJava.value = s.javaPath || '';
            if (settingWidth) settingWidth.value = s.resolution?.width || 1280;
            if (settingHeight) settingHeight.value = s.resolution?.height || 720;
        }
    } catch (e) { console.error('Failed to load settings:', e); }
}

btnSave?.addEventListener('click', async () => {
    const settings = {
        ram: settingRam?.value || '4',
        javaPath: settingJava?.value || '',
        resolution: {
            width: parseInt(settingWidth?.value) || 1280,
            height: parseInt(settingHeight?.value) || 720,
        },
    };

    try {
        await window.mooAPI?.saveSettings(settings);
        btnSave.classList.add('saved');
        btnSave.querySelector('span').textContent = t('btn_saved');
        setTimeout(() => {
            btnSave.classList.remove('saved');
            btnSave.querySelector('span').textContent = t('btn_save');
        }, 2000);
    } catch (e) { console.error('Failed to save:', e); }
});

// =============================================
// Mods Page & Modrinth Integration
// =============================================
const navMods = document.getElementById('nav-mods');
const modsSearchInput = document.getElementById('mods-search-input');
const tabBrowseMods = document.getElementById('tab-browse-mods');
const tabInstalledMods = document.getElementById('tab-installed-mods');
const modsGrid = document.getElementById('mods-grid');
const modsLoading = document.getElementById('mods-loading');
const modsEmpty = document.getElementById('mods-empty');
const installedCountEl = document.getElementById('installed-count');
const btnOpenModsFolder = document.getElementById('btn-open-mods-folder');

let currentModsTab = 'browse';
let installedModsCache = [];
let searchTimeout = null;

async function refreshInstalledMods() {
    try {
        installedModsCache = (await window.mooAPI?.getInstalledMods()) || [];
        if (installedCountEl) {
            installedCountEl.textContent = installedModsCache.length;
        }
        if (currentModsTab === 'installed') {
            renderInstalledModsView();
        }
    } catch (e) {
        console.error('Failed to get installed mods:', e);
    }
}

async function searchAndRenderMods(query = '') {
    if (!modsGrid) return;
    modsLoading?.classList.remove('hidden');
    modsEmpty?.classList.add('hidden');
    modsGrid.innerHTML = '';

    try {
        await refreshInstalledMods();
        const res = await window.mooAPI?.searchModrinth({ query, limit: 30 });
        modsLoading?.classList.add('hidden');

        if (res?.success && res.data?.hits?.length > 0) {
            const hits = res.data.hits;
            hits.forEach((mod) => {
                const isInstalled = installedModsCache.some(im => 
                    im.filename.toLowerCase().includes(mod.slug.toLowerCase()) || 
                    im.filename.toLowerCase().includes(mod.title.toLowerCase().replace(/\s+/g, '-'))
                );

                const card = document.createElement('div');
                card.className = 'mod-card';

                const iconUrl = mod.icon_url || 'logo.png';
                const formattedDownloads = (mod.downloads >= 1000000) 
                    ? (mod.downloads / 1000000).toFixed(1) + 'M' 
                    : (mod.downloads >= 1000) ? (mod.downloads / 1000).toFixed(0) + 'k' : mod.downloads;

                card.innerHTML = `
                    <div>
                        <div class="mod-card-top">
                            <img src="${iconUrl}" class="mod-icon" onerror="this.src='logo.png'" alt="${mod.title}">
                            <div class="mod-meta">
                                <span class="mod-title" title="${mod.title}">${mod.title}</span>
                                <span class="mod-author">by ${mod.author}</span>
                            </div>
                        </div>
                        <p class="mod-description" title="${mod.description}">${mod.description || 'Brak opisu.'}</p>
                    </div>
                    <div class="mod-card-footer">
                        <span class="mod-downloads">
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                            ${formattedDownloads} ${t('downloads_count')}
                        </span>
                        <div style="display: flex; gap: 6px; align-items: center;">
                            <button class="btn-versions-mod btn-browse-versions" data-project="${mod.project_id}" data-title="${mod.title}" title="${t('btn_versions')}">
                                🏷️
                            </button>
                            <button class="btn-install-mod ${isInstalled ? 'installed' : ''}" data-project="${mod.project_id}" data-title="${mod.title}">
                                ${isInstalled ? t('btn_installed') : t('btn_install')}
                            </button>
                        </div>
                    </div>
                `;

                const btnInstall = card.querySelector('.btn-install-mod');
                if (!isInstalled) {
                    btnInstall?.addEventListener('click', async () => {
                        btnInstall.textContent = t('btn_installing');
                        btnInstall.disabled = true;
                        try {
                            const installRes = await window.mooAPI?.installMod(mod.project_id);
                            if (installRes?.success) {
                                btnInstall.textContent = t('btn_installed');
                                btnInstall.classList.add('installed');
                                await refreshInstalledMods();
                                // Refresh all cards on current browse page in case dependencies were installed
                                document.querySelectorAll('.btn-install-mod').forEach((otherBtn) => {
                                    const pid = otherBtn.dataset.project;
                                    const pTitle = otherBtn.dataset.title;
                                    if (installedModsCache.some(im => 
                                        (im.name && im.name.toLowerCase() === pTitle?.toLowerCase()) ||
                                        (im.filename && im.filename.toLowerCase().includes(pid?.toLowerCase()))
                                    )) {
                                        otherBtn.textContent = t('btn_installed');
                                        otherBtn.classList.add('installed');
                                    }
                                });
                            } else {
                                showToast(`Nie udało się zainstalować moda: ${installRes?.error}`, 'error');
                                btnInstall.textContent = t('btn_install');
                                btnInstall.disabled = false;
                            }
                        } catch (err) {
                            showToast(`Błąd: ${err.message}`, 'error');
                            btnInstall.textContent = t('btn_install');
                            btnInstall.disabled = false;
                        }
                    });
                }

                const btnBrowseVersions = card.querySelector('.btn-browse-versions');
                btnBrowseVersions?.addEventListener('click', () => {
                    openVersionPickerModal(mod.title, mod.project_id, null, iconUrl, '');
                });

                modsGrid.appendChild(card);
            });
        } else {
            modsEmpty?.classList.remove('hidden');
        }
    } catch (e) {
        modsLoading?.classList.add('hidden');
        modsEmpty?.classList.remove('hidden');
        console.error('Mod search error:', e);
    }
}

let modUpdatesMap = {}; // filename -> updateInfo

function renderInstalledModsView(filterQuery = '') {
    if (!modsGrid) return;
    modsLoading?.classList.add('hidden');
    modsEmpty?.classList.add('hidden');
    modsGrid.innerHTML = '';

    const query = filterQuery.trim().toLowerCase();
    const filteredMods = installedModsCache.filter((mod) => {
        if (!query) return true;
        return (
            (mod.cleanName && mod.cleanName.toLowerCase().includes(query)) ||
            (mod.filename && mod.filename.toLowerCase().includes(query)) ||
            (mod.name && mod.name.toLowerCase().includes(query))
        );
    });

    if (filteredMods.length === 0) {
        modsEmpty?.classList.remove('hidden');
        return;
    }

    const container = document.createElement('div');
    container.style.width = '100%';
    container.style.gridColumn = '1 / -1';

    filteredMods.forEach((mod) => {
        const row = document.createElement('div');
        row.className = `installed-mod-row ${mod.enabled ? '' : 'disabled'}`;

        const sizeKb = Math.round(mod.size / 1024);
        const isCore = mod.isCore;

        const iconSrc = mod.icon || 'logo.png';
        const displayName = mod.name || mod.cleanName;
        const currentVer = mod.version ? `v${mod.version}` : '';

        const updateInfo = modUpdatesMap[mod.filename];

        row.innerHTML = `
            <div class="installed-mod-info">
                <img src="${iconSrc}" class="installed-mod-img" alt="${displayName}" onerror="this.src='logo.png'">
                <div>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span class="installed-mod-name">${displayName}</span>
                        ${currentVer ? `<span style="font-size: 11px; color: var(--text-muted); font-weight: 500;">${currentVer}</span>` : ''}
                        <span style="font-size: 11px; color: var(--text-muted);">(${sizeKb} KB)</span>
                    </div>
                </div>
            </div>
            <div class="installed-mod-actions">
                ${updateInfo ? `
                <button class="btn-update-mod" data-file="${mod.filename}" data-verid="${updateInfo.versionId}">
                    🚀 ${t('btn_update')} → ${updateInfo.latestVersion}
                </button>
                ` : ''}
                ${!isCore ? `
                <button class="btn-versions-mod" data-project="${mod.modId || displayName}" data-file="${mod.filename}" title="${t('btn_versions')}">
                    🏷️ ${t('btn_versions')}
                </button>
                <label class="mod-switch" title="Włącz / Wyłącz mod">
                    <input type="checkbox" class="mod-toggle-input" ${mod.enabled ? 'checked' : ''}>
                    <span class="mod-slider"></span>
                </label>
                <button class="btn-uninstall-mod" data-file="${mod.filename}">${t('btn_uninstall')}</button>
                ` : `<span class="installed-mod-badge core">${t('core_mod')}</span>`}
            </div>
        `;

        // Update Mod button handler
        const btnUpdate = row.querySelector('.btn-update-mod');
        btnUpdate?.addEventListener('click', async () => {
            btnUpdate.textContent = t('btn_updating');
            btnUpdate.disabled = true;
            try {
                const res = await window.mooAPI?.installModVersion(updateInfo.versionId, mod.filename);
                if (res?.success) {
                    delete modUpdatesMap[mod.filename];
                    showToast(`Zaktualizowano ${displayName} do wersji ${updateInfo.latestVersion}!`, 'success');
                    await refreshInstalledMods();
                    await checkAndApplyUpdatesSilently(false);
                    renderInstalledModsView(modsSearchInput?.value || '');
                } else {
                    showToast(`Błąd aktualizacji: ${res?.error}`, 'error');
                    btnUpdate.textContent = `🚀 ${t('btn_update')}`;
                    btnUpdate.disabled = false;
                }
            } catch (err) {
                showToast(`Błąd: ${err.message}`, 'error');
                btnUpdate.disabled = false;
            }
        });

        // Versions button handler
        const btnVersions = row.querySelector('.btn-versions-mod');
        btnVersions?.addEventListener('click', () => {
            openVersionPickerModal(displayName, mod.modId || displayName, mod.filename, iconSrc, mod.version);
        });

        // Toggle Switch handler (ON / OFF)
        const toggleInput = row.querySelector('.mod-toggle-input');
        toggleInput?.addEventListener('change', async () => {
            const isChecked = toggleInput.checked;
            try {
                const res = await window.mooAPI?.toggleMod(mod.filename, isChecked);
                if (res?.success) {
                    mod.filename = res.newFilename;
                    mod.enabled = isChecked;
                    row.classList.toggle('disabled', !isChecked);
                    showToast(`${displayName} został ${isChecked ? 'włączony' : 'wyłączony'}`, 'info');
                } else {
                    toggleInput.checked = !isChecked;
                    showToast(`Błąd przełączania moda: ${res?.error}`, 'error');
                }
            } catch (err) {
                toggleInput.checked = !isChecked;
                showToast(`Błąd: ${err.message}`, 'error');
            }
        });

        // Uninstall button handler
        const btnUninstall = row.querySelector('.btn-uninstall-mod');
        btnUninstall?.addEventListener('click', async () => {
            const confirmed = await showConfirmModal(
                'Usuń modyfikację',
                `Czy na pewno chcesz trwale usunąć mod "${mod.cleanName}"?`,
                t('btn_uninstall'),
                'Anuluj'
            );
            if (confirmed) {
                try {
                    const res = await window.mooAPI?.uninstallMod(mod.filename);
                    if (res?.success) {
                        row.remove();
                        showToast(`Usunięto ${displayName}`, 'info');
                        await refreshInstalledMods();
                        renderInstalledModsView(modsSearchInput?.value || '');
                    } else {
                        showToast(`Błąd: ${res?.error}`, 'error');
                    }
                } catch (err) {
                    showToast(`Błąd usuwania: ${err.message}`, 'error');
                }
            }
        });

        container.appendChild(row);
    });

    modsGrid.appendChild(container);
}

// =============================================
// Version Picker Dialog (Modrinth / Prism Style)
// =============================================
const versionModal = document.getElementById('version-picker-modal');
const versionDialogIcon = document.getElementById('version-dialog-icon');
const versionDialogTitle = document.getElementById('version-dialog-title');
const versionDialogModname = document.getElementById('version-dialog-modname');
const versionSearchInput = document.getElementById('version-search-input');
const versionModalList = document.getElementById('version-modal-list');
const toggleShowAllVersions = document.getElementById('toggle-show-all-versions');
const versionDetailNumber = document.getElementById('version-detail-number');
const versionDetailBadge = document.getElementById('version-detail-badge');
const versionDetailDate = document.getElementById('version-detail-date');
const versionDetailLoaders = document.getElementById('version-detail-loaders');
const versionChangelogText = document.getElementById('version-changelog-text');
const versionModalLoading = document.getElementById('version-modal-loading');
const btnCancelVersion = document.getElementById('btn-cancel-version');
const btnApplyVersion = document.getElementById('btn-apply-version');
const btnApplyVersionText = document.getElementById('btn-apply-version-text');
const btnCloseVersionModal = document.getElementById('btn-close-version-modal');

let currentVersionModalContext = null;
let currentModalVersions = [];
let selectedVersionObj = null;

function renderVersionList(filter = '') {
    if (!versionModalList) return;
    versionModalList.innerHTML = '';

    const query = filter.trim().toLowerCase();
    const filtered = currentModalVersions.filter(v => {
        if (!query) return true;
        return (v.version_number && v.version_number.toLowerCase().includes(query)) ||
               (v.name && v.name.toLowerCase().includes(query));
    });

    if (filtered.length === 0) {
        versionModalList.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 30px 10px; font-size: 12px;">Nie znaleziono wersji spełniających kryteria.</div>`;
        return;
    }

    filtered.forEach((ver, index) => {
        const item = document.createElement('div');
        item.className = 'version-list-item';
        if (selectedVersionObj && selectedVersionObj.id === ver.id) {
            item.classList.add('active');
        }

        const type = (ver.version_type || 'release').toLowerCase();
        const letter = type === 'beta' ? 'B' : type === 'alpha' ? 'A' : 'R';
        const isCurrent = currentVersionModalContext?.currentVersion && 
            (ver.version_number === currentVersionModalContext.currentVersion || 
             currentVersionModalContext.currentVersion.includes(ver.version_number));

        item.innerHTML = `
            <div class="version-item-left">
                <span class="version-type-circle ${type}">${letter}</span>
                <span class="version-list-number" title="${ver.version_number}">${ver.version_number}</span>
            </div>
            ${isCurrent ? `<span class="version-current-badge">Obecna</span>` : ''}
        `;

        item.addEventListener('click', () => {
            document.querySelectorAll('.version-list-item').forEach(el => el.classList.remove('active'));
            item.classList.add('active');
            selectVersionInModal(ver);
        });

        versionModalList.appendChild(item);

        // Auto-select first item if none selected
        if (index === 0 && !selectedVersionObj) {
            item.classList.add('active');
            selectVersionInModal(ver);
        }
    });
}

function selectVersionInModal(ver) {
    selectedVersionObj = ver;
    if (!ver) return;

    if (versionDetailNumber) versionDetailNumber.textContent = ver.version_number || ver.name;
    
    if (versionDetailBadge) {
        versionDetailBadge.classList.remove('hidden', 'release', 'beta', 'alpha');
        const type = (ver.version_type || 'release').toLowerCase();
        versionDetailBadge.classList.add(type);
        versionDetailBadge.textContent = ver.version_type || 'Release';
    }

    if (versionDetailDate) {
        versionDetailDate.textContent = ver.date_published 
            ? new Date(ver.date_published).toLocaleDateString(currentLang === 'pl' ? 'pl-PL' : 'en-US', { day: 'numeric', month: 'long', year: 'numeric' })
            : '';
    }

    if (versionDetailLoaders) {
        const gVers = ver.game_versions?.length ? ver.game_versions.join(', ') : '1.21.4';
        versionDetailLoaders.textContent = `Fabric • ${gVers}`;
    }

    if (versionChangelogText) {
        versionChangelogText.textContent = ver.changelog?.trim() || 'Brak opisu i listy zmian dla tej wersji.';
    }

    if (btnApplyVersion) {
        btnApplyVersion.disabled = false;
    }
    if (btnApplyVersionText) {
        btnApplyVersionText.textContent = `Zainstaluj ${ver.version_number}`;
    }
}

async function fetchAndPopulateVersions(showAll = false) {
    if (!currentVersionModalContext) return;
    versionModalLoading?.classList.remove('hidden');
    selectedVersionObj = null;

    try {
        const res = await window.mooAPI?.getModVersions(currentVersionModalContext.projectId, showAll);
        versionModalLoading?.classList.add('hidden');
        if (res?.success && res.versions && res.versions.length > 0) {
            currentModalVersions = res.versions;
            renderVersionList(versionSearchInput?.value || '');
        } else {
            currentModalVersions = [];
            if (versionModalList) {
                versionModalList.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 40px 10px; font-size: 12px;">Brak dostępnych wersji na Modrinth.</div>`;
            }
            if (btnApplyVersion) btnApplyVersion.disabled = true;
        }
    } catch (e) {
        versionModalLoading?.classList.add('hidden');
        if (versionModalList) {
            versionModalList.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 40px 10px; font-size: 12px;">Błąd pobierania: ${e.message}</div>`;
        }
    }
}

async function openVersionPickerModal(modTitle, projectId, oldFilename = null, modIcon = 'logo.png', currentVersion = '') {
    if (!versionModal) return;

    currentVersionModalContext = {
        title: modTitle,
        projectId: projectId,
        oldFilename: oldFilename,
        icon: modIcon,
        currentVersion: currentVersion
    };

    if (versionDialogTitle) versionDialogTitle.textContent = 'Zmień wersję';
    if (versionDialogModname) versionDialogModname.textContent = modTitle;
    if (versionDialogIcon) versionDialogIcon.src = modIcon || 'logo.png';
    if (versionSearchInput) versionSearchInput.value = '';
    if (toggleShowAllVersions) toggleShowAllVersions.checked = false;

    if (btnApplyVersion) {
        btnApplyVersion.disabled = true;
        btnApplyVersion.querySelector('span').textContent = 'Wybierz wersję';
    }

    versionModal.classList.remove('hidden');
    await fetchAndPopulateVersions(false);
}

versionSearchInput?.addEventListener('input', () => {
    renderVersionList(versionSearchInput.value);
});

toggleShowAllVersions?.addEventListener('change', () => {
    fetchAndPopulateVersions(toggleShowAllVersions.checked);
});

btnCancelVersion?.addEventListener('click', () => {
    versionModal?.classList.add('hidden');
});

btnCloseVersionModal?.addEventListener('click', () => {
    versionModal?.classList.add('hidden');
});

versionModal?.addEventListener('click', (e) => {
    if (e.target === versionModal) {
        versionModal.classList.add('hidden');
    }
});

btnApplyVersion?.addEventListener('click', async () => {
    if (!selectedVersionObj || !currentVersionModalContext) return;

    btnApplyVersion.disabled = true;
    const span = btnApplyVersion.querySelector('span');
    if (span) span.textContent = 'Instalowanie...';

    try {
        const res = await window.mooAPI?.installModVersion(selectedVersionObj.id, currentVersionModalContext.oldFilename);
        if (res?.success) {
            versionModal?.classList.add('hidden');
            showToast(`Pomyślnie zmieniono wersję na ${selectedVersionObj.version_number}!`, 'success');
            await refreshInstalledMods();
            await checkAndApplyUpdatesSilently(false);
            if (currentModsTab === 'installed') {
                renderInstalledModsView(modsSearchInput?.value || '');
            } else {
                searchAndRenderMods(modsSearchInput?.value || '');
            }
        } else {
            showToast(`Nie udało się zainstalować wersji: ${res?.error}`, 'error');
            btnApplyVersion.disabled = false;
            if (span) span.textContent = `Zainstaluj ${selectedVersionObj.version_number}`;
        }
    } catch (err) {
        showToast(`Błąd: ${err.message}`, 'error');
        btnApplyVersion.disabled = false;
        if (span) span.textContent = `Zainstaluj ${selectedVersionObj.version_number}`;
    }
});

// =============================================
// Automatic Mod Update Checker (Runs on Start & Tab Switch)
// =============================================
let hasAutoCheckedUpdates = false;

async function checkAndApplyUpdatesSilently(notifyUser = false) {
    try {
        const res = await window.mooAPI?.checkModUpdates();
        if (res?.success && res.updates) {
            modUpdatesMap = {};
            res.updates.forEach((u) => {
                modUpdatesMap[u.filename] = u;
            });

            // Update installed tab indicator
            if (tabInstalledMods) {
                const count = installedModsCache.length;
                if (res.updates.length > 0) {
                    tabInstalledMods.innerHTML = `${t('tab_installed')} (<span id="installed-count">${count}</span>) <span style="background: rgba(34, 197, 94, 0.2); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.4); padding: 1px 6px; border-radius: 10px; font-size: 11px; margin-left: 4px;">+${res.updates.length} 🚀</span>`;
                } else {
                    tabInstalledMods.innerHTML = `${t('tab_installed')} (<span id="installed-count">${count}</span>)`;
                }
            }

            if (currentModsTab === 'installed') {
                renderInstalledModsView(modsSearchInput?.value || '');
            }

            if (notifyUser && res.updates.length > 0) {
                showToast(`Znaleziono ${res.updates.length} aktualizacji dla Twoich modów!`, 'success');
            }
        }
    } catch (e) {
        console.error('Silent update check failed:', e);
    }
}

// Check Updates Header Button
const btnCheckUpdates = document.getElementById('btn-check-mod-updates');
btnCheckUpdates?.addEventListener('click', async () => {
    btnCheckUpdates.classList.add('checking');
    try {
        const res = await window.mooAPI?.checkModUpdates();
        btnCheckUpdates.classList.remove('checking');
        if (res?.success && res.updates && res.updates.length > 0) {
            modUpdatesMap = {};
            res.updates.forEach((u) => {
                modUpdatesMap[u.filename] = u;
            });
            renderInstalledModsView(modsSearchInput?.value || '');
            showInfoModal(
                'Dostępne aktualizacje',
                `Znaleziono ${res.updates.length} aktualizacji dla Twoich modów! Możesz zaktualizować je zielonym przyciskiem obok każdego moda na liście.`,
                'success'
            );
        } else {
            showToast(t('no_updates_found'), 'success');
        }
    } catch (err) {
        btnCheckUpdates.classList.remove('checking');
        showToast(`Błąd sprawdzania aktualizacji: ${err.message}`, 'error');
    }
});

// =============================================
// Toast & Modal Notification System
// =============================================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icon = 'ℹ️';
    if (type === 'success') icon = '✓';
    if (type === 'warning') icon = '⚠️';
    if (type === 'error') icon = '✕';

    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span class="toast-msg">${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => toast.remove(), 300);
    }, 3800);
}

const infoModal = document.getElementById('info-modal');
const infoModalTitle = document.getElementById('info-modal-title');
const infoModalMessage = document.getElementById('info-modal-message');
const infoModalIconWrap = document.getElementById('info-modal-icon-wrap');
const btnInfoModalOk = document.getElementById('btn-info-modal-ok');

function showInfoModal(title, message, type = 'info') {
    return new Promise((resolve) => {
        if (!infoModal) {
            showToast(message, type);
            resolve();
            return;
        }

        if (infoModalTitle) infoModalTitle.textContent = title;
        if (infoModalMessage) infoModalMessage.textContent = message;

        if (infoModalIconWrap) {
            if (type === 'success') {
                infoModalIconWrap.style.background = 'rgba(34, 197, 94, 0.12)';
                infoModalIconWrap.style.borderColor = 'rgba(34, 197, 94, 0.3)';
                infoModalIconWrap.innerHTML = `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`;
            } else if (type === 'error') {
                infoModalIconWrap.style.background = 'rgba(239, 68, 68, 0.12)';
                infoModalIconWrap.style.borderColor = 'rgba(239, 68, 68, 0.3)';
                infoModalIconWrap.innerHTML = `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`;
            } else {
                infoModalIconWrap.style.background = 'rgba(59, 130, 246, 0.12)';
                infoModalIconWrap.style.borderColor = 'rgba(59, 130, 246, 0.3)';
                infoModalIconWrap.innerHTML = `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#60a5fa" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>`;
            }
        }

        infoModal.classList.remove('hidden');

        const handleClose = () => {
            infoModal.classList.add('hidden');
            btnInfoModalOk?.removeEventListener('click', handleClose);
            resolve();
        };

        btnInfoModalOk?.addEventListener('click', handleClose);
    });
}

infoModal?.addEventListener('click', (e) => {
    if (e.target === infoModal) {
        infoModal.classList.add('hidden');
    }
});

// =============================================
// Custom Modal Dialog
// =============================================
const customModal = document.getElementById('custom-modal');
const modalTitle = document.getElementById('modal-title');
const modalMessage = document.getElementById('modal-message');
const btnModalCancel = document.getElementById('btn-modal-cancel');
const btnModalConfirm = document.getElementById('btn-modal-confirm');

let modalResolve = null;

function showConfirmModal(title, message, confirmText = 'Usuń', cancelText = 'Anuluj') {
    return new Promise((resolve) => {
        if (!customModal) {
            resolve(true);
            return;
        }

        if (modalTitle) modalTitle.textContent = title;
        if (modalMessage) modalMessage.textContent = message;
        if (btnModalConfirm) btnModalConfirm.textContent = confirmText;
        if (btnModalCancel) btnModalCancel.textContent = cancelText;

        customModal.classList.remove('hidden');
        modalResolve = resolve;
    });
}

btnModalCancel?.addEventListener('click', () => {
    customModal?.classList.add('hidden');
    if (modalResolve) {
        modalResolve(false);
        modalResolve = null;
    }
});

btnModalConfirm?.addEventListener('click', () => {
    customModal?.classList.add('hidden');
    if (modalResolve) {
        modalResolve(true);
        modalResolve = null;
    }
});

customModal?.addEventListener('click', (e) => {
    if (e.target === customModal) {
        customModal.classList.add('hidden');
        if (modalResolve) {
            modalResolve(false);
            modalResolve = null;
        }
    }
});

// Event Listeners for Mods
tabBrowseMods?.addEventListener('click', () => {
    currentModsTab = 'browse';
    tabBrowseMods.classList.add('active');
    tabInstalledMods.classList.remove('active');
    if (modsSearchInput) {
        modsSearchInput.placeholder = t('search_mods_placeholder');
        modsSearchInput.value = '';
    }
    searchAndRenderMods('');
});

tabInstalledMods?.addEventListener('click', async () => {
    currentModsTab = 'installed';
    tabInstalledMods.classList.add('active');
    tabBrowseMods.classList.remove('active');
    if (modsSearchInput) {
        modsSearchInput.placeholder = t('search_installed_placeholder');
        modsSearchInput.value = '';
    }
    await refreshInstalledMods();
    renderInstalledModsView('');
    checkAndApplyUpdatesSilently(false);
});

modsSearchInput?.addEventListener('input', () => {
    if (currentModsTab === 'browse') {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            searchAndRenderMods(modsSearchInput.value);
        }, 350);
    } else {
        renderInstalledModsView(modsSearchInput.value);
    }
});

btnOpenModsFolder?.addEventListener('click', () => {
    window.mooAPI?.openModsFolder();
});

navMods?.addEventListener('click', () => {
    if (currentModsTab === 'browse') {
        searchAndRenderMods(modsSearchInput?.value || '');
    } else {
        refreshInstalledMods();
    }
});

// =============================================
// Drag & Drop Mod Installation (.jar)
// =============================================
const pageMods = document.getElementById('page-mods');
const modsDropzone = document.getElementById('mods-dropzone');
let dragCounter = 0;

['dragenter', 'dragover', 'dragleave', 'drop'].forEach((eventName) => {
    document.addEventListener(eventName, (e) => {
        e.preventDefault();
        e.stopPropagation();
    });
});

pageMods?.addEventListener('dragenter', () => {
    dragCounter++;
    if (modsDropzone) {
        modsDropzone.classList.remove('hidden');
        modsDropzone.classList.add('active');
    }
});

pageMods?.addEventListener('dragleave', () => {
    dragCounter--;
    if (dragCounter <= 0) {
        dragCounter = 0;
        modsDropzone?.classList.add('hidden');
        modsDropzone?.classList.remove('active');
    }
});

pageMods?.addEventListener('drop', async (e) => {
    dragCounter = 0;
    modsDropzone?.classList.add('hidden');
    modsDropzone?.classList.remove('active');

    const files = Array.from(e.dataTransfer?.files || []);
    const jarPaths = [];

    for (const file of files) {
        const path = window.mooAPI?.getFilePath ? window.mooAPI.getFilePath(file) : file.path;
        if (path && path.toLowerCase().endsWith('.jar')) {
            jarPaths.push(path);
        }
    }

    if (jarPaths.length > 0) {
        try {
            const res = await window.mooAPI?.installLocalMods(jarPaths);
            if (res?.success && res.count > 0) {
                // Automatically switch to installed tab and refresh list
                currentModsTab = 'installed';
                tabInstalledMods?.classList.add('active');
                tabBrowseMods?.classList.remove('active');
                if (modsSearchInput) {
                    modsSearchInput.placeholder = t('search_installed_placeholder');
                    modsSearchInput.value = '';
                }
                await refreshInstalledMods();
                renderInstalledModsView('');
                showToast(`Pomyślnie dodano ${res.count} modów!`, 'success');
            } else if (res && !res.success) {
                showToast(`Błąd dodawania modów: ${res.error}`, 'error');
            }
        } catch (err) {
            showToast(`Błąd: ${err.message}`, 'error');
        }
    }
});

// =============================================
// Moo Client Core Update Check & Modal
// =============================================
let currentClientUpdateInfo = null;

async function checkClientCoreUpdate(showToastIfUpToDate = false) {
    const pill = document.getElementById('update-status-pill');
    const label = document.getElementById('update-text');

    if (!pill || !label) return;

    pill.classList.add('checking');
    label.textContent = t('update_checking');

    try {
        const res = await window.mooAPI?.checkClientUpdate();
        pill.classList.remove('checking');

        if (res && res.hasUpdate) {
            currentClientUpdateInfo = res;
            pill.classList.add('has-update');
            label.textContent = `v${res.latestVersion} ${t('update_available_short')}`;
            pill.title = `${t('update_modal_title')} (v${res.currentVersion} ➔ v${res.latestVersion})`;

            // Prompt modal automatically if launched with update available
            openClientUpdateModal(res);
        } else {
            currentClientUpdateInfo = null;
            pill.classList.remove('has-update');
            const ver = res?.currentVersion || '1.0.0';
            label.textContent = `v${ver} (${t('update_up_to_date')})`;
            pill.title = `Moo Client v${ver} — ${t('update_up_to_date')}`;

            if (showToastIfUpToDate) {
                showToast(`Moo Client v${ver} — ${t('update_up_to_date')}! ✓`, 'success');
            }
        }
    } catch (err) {
        pill.classList.remove('checking');
        label.textContent = 'v1.0.0';
        console.error('Error checking client update:', err);
    }
}

function openClientUpdateModal(info) {
    if (!info) return;
    const modal = document.getElementById('modal-client-update');
    const versionTag = document.getElementById('update-modal-version-tag');
    const changelogText = document.getElementById('update-modal-changelog-text');
    const progressSection = document.getElementById('update-progress-section');
    const actions = document.getElementById('update-modal-actions');

    if (!modal) return;

    if (versionTag) {
        versionTag.textContent = `Moo Client v${info.currentVersion || '1.0.0'} ➔ v${info.latestVersion}`;
    }

    if (changelogText) {
        changelogText.textContent = info.changelog || 'Brak dodatkowego opisu zmian.';
    }

    if (progressSection) progressSection.style.display = 'none';
    if (actions) actions.style.display = 'flex';

    modal.classList.remove('hidden');
}

function closeClientUpdateModal() {
    document.getElementById('modal-client-update')?.classList.add('hidden');
}

async function performClientCoreUpdate() {
    const progressSection = document.getElementById('update-progress-section');
    const progressBar = document.getElementById('client-update-progress-bar');
    const progressMsg = document.getElementById('client-update-progress-msg');
    const progressPct = document.getElementById('client-update-progress-pct');
    const actions = document.getElementById('update-modal-actions');

    if (progressSection) progressSection.style.display = 'block';
    if (actions) actions.style.display = 'none';

    if (progressBar) progressBar.style.width = '10%';
    if (progressMsg) progressMsg.textContent = t('update_downloading');
    if (progressPct) progressPct.textContent = '10%';

    try {
        const res = await window.mooAPI?.performClientUpdate();
        if (res && res.success) {
            if (progressBar) progressBar.style.width = '100%';
            if (progressPct) progressPct.textContent = '100%';

            if (res.restarting) {
                if (progressMsg) progressMsg.textContent = 'Aktualizacja ukończona! Ponowne uruchamianie...';
                showToast('Aktualizacja ukończona! Uruchamianie nowej wersji...', 'success');
            } else {
                if (progressMsg) progressMsg.textContent = t('update_success_msg');
                showToast(t('update_success_msg'), 'success');

                setTimeout(() => {
                    closeClientUpdateModal();
                    checkClientCoreUpdate(false);
                }, 1200);
            }
        } else {
            showToast(`Błąd aktualizacji: ${res?.error || 'Nieznany błąd'}`, 'error');
            if (actions) actions.style.display = 'flex';
            if (progressSection) progressSection.style.display = 'none';
        }
    } catch (err) {
        showToast(`Błąd aktualizacji: ${err.message}`, 'error');
        if (actions) actions.style.display = 'flex';
        if (progressSection) progressSection.style.display = 'none';
    }
}

// Progress listener from main process
window.mooAPI?.onClientUpdateProgress?.((data) => {
    const progressBar = document.getElementById('client-update-progress-bar');
    const progressMsg = document.getElementById('client-update-progress-msg');
    const progressPct = document.getElementById('client-update-progress-pct');

    if (progressBar && data.percent !== undefined) {
        progressBar.style.width = `${data.percent}%`;
    }
    if (progressPct && data.percent !== undefined) {
        progressPct.textContent = `${data.percent}%`;
    }
    if (progressMsg && data.status) {
        progressMsg.textContent = data.status;
    }
});

// Event Listeners for Update Pill & Modal
document.getElementById('update-status-pill')?.addEventListener('click', (e) => {
    if (e.target.closest('#btn-manual-update-check')) {
        e.stopPropagation();
        checkClientCoreUpdate(true);
        return;
    }
    if (currentClientUpdateInfo) {
        openClientUpdateModal(currentClientUpdateInfo);
    } else {
        checkClientCoreUpdate(true);
    }
});

document.getElementById('btn-close-update-modal')?.addEventListener('click', closeClientUpdateModal);
document.getElementById('btn-update-later')?.addEventListener('click', closeClientUpdateModal);
document.getElementById('btn-update-now')?.addEventListener('click', performClientCoreUpdate);

// =============================================
// Initialize
// =============================================
document.addEventListener('DOMContentLoaded', () => {
    const savedLang = localStorage.getItem('moo-lang') || 'pl';
    setLanguage(savedLang);
    initParticles();
    initBackgroundTheme();
    initOnlineUsersCounter();
    loadAccount();
    loadSettings();
    checkClientCoreUpdate(false);
    refreshInstalledMods().then(() => {
        checkAndApplyUpdatesSilently(true);
    });
});
