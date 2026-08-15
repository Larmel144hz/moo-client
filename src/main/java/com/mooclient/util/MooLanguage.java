package com.mooclient.util;

/**
 * Client language manager supporting Polish (PL) and English (EN).
 */
public enum MooLanguage {
    PL,
    EN;

    public static MooLanguage current = PL;

    public static String get(String key) {
        if (current == PL) {
            return switch (key) {
                case "back" -> "← Powrót";
                case "mods_title" -> "MOO CLIENT • MODYFIKACJE";
                case "gamma_desc" -> "Jasność w ciemności";
                case "fps_desc" -> "Licznik FPS na ekranie";
                case "sprint_desc" -> "Automatyczny ciągły bieg";
                case "freelook_desc" -> "Swobodny widok 360°";
                case "potions_desc" -> "Aktywne mikstury i czas";
                case "nametags_desc" -> "Nick i kolorowy ping";
                case "zoom_desc" -> "Płynne przybliżenie (Zoom)";
                case "macro_desc" -> "Skróty i komendy";
                case "chat_desc" -> "Przezroczystość i historia";
                case "ping_desc" -> "Aktualny ping na ekranie";
                case "esc_hint" -> "Naciśnij ESC lub PRAWY SHIFT, aby zamknąć";
                case "singleplayer" -> "TRYB JEDNOOSOBOWY";
                case "multiplayer" -> "TRYB WIELOOSOBOWY";
                case "settings" -> "USTAWIENIA";
                case "quit" -> "WYJDŹ Z GRY";
                case "fps_opt_title" -> "FPS";
                case "fps_opt_subtitle" -> "Wyświetlanie i konfiguracja licznika FPS na ekranie";
                case "ping_opt_title" -> "PING";
                case "ping_opt_subtitle" -> "Wyświetlanie i konfiguracja licznika pingu (ms) na ekranie";
                case "sprint_opt_title" -> "SPRINT";
                case "sprint_opt_subtitle" -> "Konfiguracja wskaźnika automatycznego biegu na ekranie";
                case "freelook_opt_title" -> "FREELOOK";
                case "freelook_opt_subtitle" -> "Swobodny obrót kamery 360° bez obracania postaci";
                case "potions_opt_title" -> "POTION EFFECTS";
                case "potions_opt_subtitle" -> "Wyświetlanie aktywnych mikstur i odliczania czasu";
                case "nametags_opt_title" -> "NAMETAGS";
                case "nametags_opt_subtitle" -> "Wyświetlanie nicków i kolorowego pingu nad graczami";
                case "zoom_opt_title" -> "ZOOM";
                case "zoom_opt_subtitle" -> "Konfiguracja przybliżenia i stopnia powiększenia";
                case "macro_opt_title" -> "MACRO / AUTOTEXT";
                case "macro_opt_subtitle" -> "Wykonywanie komend i wiadomości pod wybranymi klawiszami";
                case "chat_opt_title" -> "CHAT";
                case "chat_opt_subtitle" -> "Przezroczystość tła, nielimitowany czat i płynna animacja";
                case "chat_transparent_label" -> "Przezroczyste tło (Transparent Background)";
                case "chat_unlimited_label" -> "Nielimitowany czat (Unlimited Chat)";
                case "chat_smooth_label" -> "Płynny czat (Smooth Chat)";
                case "style_label" -> "Styl wyglądu";
                case "bg_label" -> "Pokaż tło (Show Background)";
                case "shadow_label" -> "Cień tekstu (Text Shadow)";
                case "prefix_label" -> "Przedrostek 'FPS:' (Show Prefix)";
                case "compact_label" -> "Tryb kompaktowy (Compact Mode)";
                case "show_ping_label" -> "Pokaż ping (Show Ping)";
                case "ping_pos_label" -> "Pozycja pingu (Ping Position)";
                case "ping_pos_beside" -> "Obok nicku (Beside)";
                case "ping_pos_above" -> "Nad nickiem (Above)";
                case "show_logo_label" -> "Logo klienta przed nickiem (Client Logo)";
                case "remove_bg_label" -> "Usuń tło nicków (Remove Background)";
                case "factor_label" -> "Stopień powiększenia (Zoom Factor)";
                case "smooth_zoom_label" -> "Płynny zoom (Smooth Zoom)";
                case "gamma_opt_title" -> "GAMMA";
                case "gamma_opt_subtitle" -> "Widzenie w ciemności bez pochodni";
                case "fullbright_label" -> "Widzenie w ciemności (Fullbright)";
                case "mode_label" -> "Tryb aktywacji (Mode)";
                case "invert_pitch_label" -> "Odwróć oś Y (Invert Pitch)";
                default -> key;
            };
        } else {
            return switch (key) {
                case "back" -> "← Back";
                case "mods_title" -> "MOO CLIENT • MODS";
                case "gamma_desc" -> "Fullbright & Cave Vision";
                case "fps_desc" -> "FPS Counter on HUD";
                case "sprint_desc" -> "Automatic Toggle Sprint";
                case "freelook_desc" -> "360° Free Camera View";
                case "potions_desc" -> "Active Potions & Timers";
                case "nametags_desc" -> "Nametag Badges & Ping";
                case "zoom_desc" -> "Smooth Camera Zoom";
                case "macro_desc" -> "Macro Command Keybinds";
                case "chat_desc" -> "Transparent Chat & History";
                case "ping_desc" -> "Ping Latency on HUD";
                case "esc_hint" -> "Press ESC or RIGHT SHIFT to close";
                case "singleplayer" -> "SINGLEPLAYER";
                case "multiplayer" -> "MULTIPLAYER";
                case "settings" -> "SETTINGS";
                case "quit" -> "QUIT GAME";
                case "fps_opt_title" -> "FPS";
                case "fps_opt_subtitle" -> "Display and customize your FPS on the HUD.";
                case "ping_opt_title" -> "PING";
                case "ping_opt_subtitle" -> "Display and customize your real-time latency on the HUD.";
                case "sprint_opt_title" -> "SPRINT";
                case "sprint_opt_subtitle" -> "Customize your Toggle Sprint indicator on HUD.";
                case "freelook_opt_title" -> "FREELOOK";
                case "freelook_opt_subtitle" -> "Free 360° camera rotation without moving character.";
                case "potions_opt_title" -> "POTION EFFECTS";
                case "potions_opt_subtitle" -> "Display active potions and countdown timer on HUD.";
                case "nametags_opt_title" -> "NAMETAGS";
                case "nametags_opt_subtitle" -> "Customize player nametags and colored ping latency.";
                case "zoom_opt_title" -> "ZOOM";
                case "zoom_opt_subtitle" -> "Customize zoom keybind and magnification factor.";
                case "macro_opt_title" -> "MACRO / AUTOTEXT";
                case "macro_opt_subtitle" -> "Execute commands & messages using custom keybinds.";
                case "chat_opt_title" -> "CHAT";
                case "chat_opt_subtitle" -> "Transparent background, unlimited history & smooth slide animation";
                case "chat_transparent_label" -> "Transparent Background";
                case "chat_unlimited_label" -> "Unlimited Chat History";
                case "chat_smooth_label" -> "Smooth Chat Animation";
                case "style_label" -> "Appearance Style";
                case "bg_label" -> "Show Background";
                case "shadow_label" -> "Text Shadow";
                case "prefix_label" -> "Show 'FPS:' Prefix";
                case "compact_label" -> "Compact Mode";
                case "show_ping_label" -> "Show Ping";
                case "ping_pos_label" -> "Ping Position";
                case "ping_pos_beside" -> "Beside Name";
                case "ping_pos_above" -> "Above Name";
                case "show_logo_label" -> "Client Logo Badge";
                case "remove_bg_label" -> "Remove Nametag Background";
                case "factor_label" -> "Zoom Factor";
                case "smooth_zoom_label" -> "Smooth Zoom";
                case "gamma_opt_title" -> "GAMMA";
                case "gamma_opt_subtitle" -> "See clearly in dark caves and night";
                case "fullbright_label" -> "Fullbright Vision";
                case "mode_label" -> "Activation Mode";
                case "invert_pitch_label" -> "Invert Y-Axis";
                default -> key;
            };
        }
    }
}
