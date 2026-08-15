# 🐄 Moo Client — 1.21.4 Fabric

> **Moo Client** to zaawansowany, wydajny i piękny klient do gry Minecraft 1.21.4 (Fabric), inspirowany takimi klientami jak Lunar Client i Badlion Client.

---

## ✨ Funkcje i Mody (Right Shift Menu)

| Mod | Opis |
|---|---|
| **Gamma (Fullbright)** | Pełne widzenie w ciemności bez używania pochodni. |
| **FPS Counter** | Licznik klatek na sekundę z możliwością przeciągania na ekranie (Draggable HUD), stylami i prefiksem. |
| **Toggle Sprint** | Automatyczny, ciągły bieg gracza z wskaźnikiem na ekranie oraz konfigurowalnym klawiszem. |
| **Freelook** | Swobodny obrót kamery 360° w trzeciej osobie (klawisz `V`, tryby Hold / Toggle). |
| **Potion Effects** | Wyświetlanie aktywnych mikstur i czasu trwania w 3 unikalnych stylach: *Moo Client*, *Simple* i *Compact*. |
| **Nametags** | Kolorowy wskaźnik pingu (opóźnienia), usuwanie tła nicków, cień tekstu oraz **permanentne logo Moo Client przed nickiem** do rozpoznawania graczy klienta w grze wieloosobowej. |
| **Zoom** | Płynne, kinowe przybliżenie widoku (OptiFine/Lunar zoom) z wyborem mnożnika (2x - 6x) i obsługą **przycisku kółka myszy (`SCROLL`)**. |
| **Mod Manager (Modrinth)** | Wbudowany instalator i menedżer modów z bazy Modrinth z automatycznym pobieraniem zależności i sprawdzaniem aktualizacji. |
| **Auto-Updater** | Automatyczne pobieranie najnowszej wersji moda i launchera bezpośrednio z GitHub Releases. |

---

## 🚀 Jak uruchomić

### 1. Przez Moo Client Launcher:
1. Pobierz instalator launchera z [GitHub Releases](https://github.com/Larmel144hz/moo-client/releases).
2. Zainstaluj i uruchom **Moo Client**.
3. Launcher automatycznie pobierze Fabric 1.21.4 oraz najnowszą wersję modyfikacji i uruchomi grę!

### 2. Samodzielny mod (Fabric Mod):
1. Pobierz plik `moo-client-1.0.0.jar` z [Releases](https://github.com/Larmel144hz/moo-client/releases).
2. Wrzuć plik do swojego folderu `.minecraft/mods/` (wymaga Fabric Loader dla 1.21.4 oraz Fabric API).
3. Uruchom grę i naciśnij **PRAWY SHIFT**, aby otworzyć menu klienta!

---

## 🛠️ Budowanie ze źródeł

### Fabric Mod:
```bash
./gradlew build
```
Skompilowany plik `.jar` pojawi się w folderze `build/libs/`.

### Electron Launcher:
```bash
cd launcher
npm install
npm run build
```
Instalator `.exe` pojawi się w folderze `launcher/dist/`.

---

## 📄 Licencja
Projekt udostępniony na licencji MIT.
