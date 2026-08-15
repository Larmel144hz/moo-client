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
                case "fps_desc" -> "Wyświetla licznik FPS na ekranie";
                case "sprint_desc" -> "Automatyczny ciągły bieg gracza";
                case "freelook_desc" -> "Swobodny widok 360° kamery";
                case "potions_desc" -> "Aktywne mikstury i czas trwania";
                case "nametags_desc" -> "Informacje i ping nad głową";
                case "zoom_desc" -> "Płynne przybliżenie widoku (Zoom)";
                case "esc_hint" -> "Naciśnij ESC lub PRAWY SHIFT, aby zamknąć";
                case "singleplayer" -> "TRYB JEDNOOSOBOWY";
                case "multiplayer" -> "TRYB WIELOOSOBOWY";
                case "settings" -> "USTAWIENIA";
                case "quit" -> "WYJDŹ Z GRY";
                case "fps_opt_title" -> "FPS";
                case "fps_opt_subtitle" -> "Wyświetlanie i konfiguracja licznika FPS na ekranie";
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
                case "style_label" -> "Styl wyglądu";
                case "bg_label" -> "Pokaż tło (Show Background)";
                case "shadow_label" -> "Cień tekstu (Text Shadow)";
                case "prefix_label" -> "Przedrostek 'FPS:' (Show Prefix)";
                case "compact_label" -> "Tryb kompaktowy (Compact Mode)";
                case "show_ping_label" -> "Pokaż ping (Show Ping)";
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
                case "gamma_desc" -> "Brightness in the dark";
                case "fps_desc" -> "Displays on-screen FPS counter";
                case "sprint_desc" -> "Automatic player sprint without holding key";
                case "freelook_desc" -> "360° Free camera perspective";
                case "potions_desc" -> "Active potions and duration timer";
                case "nametags_desc" -> "Player info and ping above head";
                case "zoom_desc" -> "Smooth camera zoom magnification";
                case "esc_hint" -> "Press ESC or RIGHT SHIFT to close";
                case "singleplayer" -> "SINGLEPLAYER";
                case "multiplayer" -> "MULTIPLAYER";
                case "settings" -> "SETTINGS";
                case "quit" -> "QUIT GAME";
                case "fps_opt_title" -> "FPS";
                case "fps_opt_subtitle" -> "Display and customize your FPS on the HUD.";
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
                case "style_label" -> "Appearance Style";
                case "bg_label" -> "Show Background";
                case "shadow_label" -> "Text Shadow";
                case "prefix_label" -> "Show 'FPS:' Prefix";
                case "compact_label" -> "Compact Mode";
                case "show_ping_label" -> "Show Ping";
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
