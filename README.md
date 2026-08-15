# 🐄 Moo Client — 1.21.4 Fabric

> **Moo Client** to zaawansowany, wydajny i piękny klient do gry Minecraft 1.21.4 (Fabric), inspirowany takimi klientami jak Lunar Client i Badlion Client.

---

## ✨ Funkcje i Mody (Right Shift Menu)

| Mod | Opis |
|---|---|
| **Gamma (Fullbright)** | Pełne widzenie w ciemności bez używania pochodni. |
| **FPS Counter** | Licznik klatek na sekundę z możliwością przeciągania na ekranie (Draggable HUD), stylami i prefiksem. |
| **Ping HUD** | Dedykowany licznik opóźnienia sieciowego (Ping) na ekranie w stylach: *Moo Client*, *Simple* i *Brackets*. |
| **Toggle Sprint** | Automatyczny, ciągły bieg gracza z wskaźnikiem na ekranie oraz konfigurowalnym klawiszem. |
| **Freelook** | Swobodny obrót kamery 360° w trzeciej osobie (klawisz `V`, tryby Hold / Toggle). |
| **Potion Effects** | Wyświetlanie aktywnych mikstur i czasu trwania w 3 unikalnych stylach: *Moo Client*, *Simple* i *Compact*. |
| **Nametags & Tablist Logo** | Kolorowy wskaźnik pingu (opóźnienia), usuwanie tła nicków, cień tekstu oraz **permanentne logo Moo Client przed nickiem i na tabliście** z błyskawicznym wykrywaniem graczy przez WebSocket w czasie rzeczywistym. |
| **Chat Module** | Przezroczyste tło czatu, nielimitowana historia linii (16384). |
| **Zoom** | Płynne, kinowe przybliżenie widoku (OptiFine/Lunar zoom) z wyborem mnożnika (2x - 6x) i obsługą **przycisku kółka myszy (`SCROLL`)**. |
| **Multi-Account Manager** | Zarządzanie wieloma kontami Microsoft / Offline i szybkie przełączanie 1-kliknięciem w launcherze. |
| **Momomo Video Background** | Animowane tło wideo Momomo w menu głównym klienta z możliwością przełączania na tryb Classic. |
| **Mod Manager (Modrinth)** | Wbudowany instalator i menedżer modów z bazy Modrinth oraz przeciąganie modów bezpośrednio z archiwów WinRAR. |
| **Hot-ASAR Auto-Updater** | Błyskawiczne aktualizacje delty w 1 sekundę w tle bez konieczności pobierania instalatorów. |

---

## 🚀 Jak pobrać i uruchomić

1. Pobierz oficjalny instalator **Moo Client** z [GitHub Releases](https://github.com/Larmel144hz/moo-client/releases/latest).
2. Zainstaluj i uruchom **Moo Client**.
3. Zaloguj się kontem Microsoft (lub graj Offline) i kliknij **GRAJ** — launcher automatycznie skonfiguruje środowisko, pobierze wszystkie wymagane pliki i uruchomi klienta!
4. W grze naciśnij **PRAWY SHIFT**, aby otworzyć menu modyfikacji i personalizacji HUD.

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
