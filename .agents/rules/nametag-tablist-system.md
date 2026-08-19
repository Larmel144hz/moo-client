# System renderowania Logo Moo Client (NameTag nad głową + Tablista)

Ten dokument opisuje architekturę i reguły działania systemu wyświetlania logo Moo Client obok nicków graczy nad głową oraz na Tabliście.

---

## 1. Architektura i Komponenty

### A. Renderowanie na Tabliście (`PlayerListHudMixin.java`)
* **Lokalizacja:** `src/main/java/com/mooclient/mixin/PlayerListHudMixin.java`
* **Mechanizm:** `@Redirect` na metodę `DrawContext.drawTextWithShadow` w klasie `PlayerListHud`.
* **Działanie:**
  1. Sprawdza `NametagsModule.isNametagsEnabled()` oraz `NametagsModule.isShowLogo()`.
  2. Sprawdza `MooUserManager.isMooUser(rawText, -1)`.
  3. Rysuje ikonę krówki (8x8 px) za pomocą `context.drawTexture(RenderLayer::getGuiTextured, MOO_LOGO, x, y, 0.0f, 0.0f, 8, 8, 8, 8)`.
  4. Przesuwa tekst nicku w prawo o 10 pikseli: `context.drawTextWithShadow(textRenderer, text, x + 10, y, color)`.

### B. Renderowanie NameTaga nad głową gracza (`NametagBackgroundMixin.java`)
* **Lokalizacja:** `src/main/java/com/mooclient/mixin/NametagBackgroundMixin.java`
* **Mechanizm:** Wstrzyknięcie do `EntityRenderer` (`renderLabelIfPresent`).
* **Działanie:**
  1. Przechwytuje `EntityRenderState` (`PlayerEntityRenderState`).
  2. Wyśrodkowuje tekst z uwzględnieniem szerokości badge'a: `@ModifyArg` na parametr `x` w `TextRenderer.draw` (`originalX + (badgeTotalWidth / 2.0f)`).
  3. Rysuje teksturowany quad logo (8.5x8.5 px) przy użyciu `vertexConsumers.getBuffer(RenderLayer.getText(MOO_LOGO))` i macierzy `matrices.peek().getPositionMatrix()`.
  4. Opcjonalnie renderuje Ping nad nickiem (`PingPosition.ABOVE`).

### C. Wykrywanie i Cache graczy (`MooUserManager.java`)
* **Lokalizacja:** `src/main/java/com/mooclient/util/MooUserManager.java`
* **Zasady:**
  * **Gracz lokalny:** Jest **ZAWSZE** rozpoznawany natychmiast (`client.player.getId() == entityId`, `session.getUsername()`).
  * **Cache $O(1)$:** `LOOKUP_CACHE` (`ConcurrentHashMap<String, Boolean>`) eliminuje powtarzające się alokacje pamięci i kompilacje Regex w każdej klatce renderowania (60–240 FPS).
  * **Czyszczenie tekstu (`cleanName`):** Usuwa kody kolorów `§` oraz tagi formatujące.
  * **Dopasowanie tokenów:** Rozpoznaje nicki graczy nawet jeśli posiadają prefiksy/rangi na serwerach (np. `[VIP] Gracz`, `Admin | Nick`).

### D. Synchronizacja sieciowa w czasie rzeczywistym (`MooNetworkHandler.java`)
* **Lokalizacja:** `src/main/java/com/mooclient/network/MooNetworkHandler.java`
* **Protokół:** Natywny, ultra-lekki klient MQTT (HiveMQ `broker.hivemq.com:1883`) na osobnym wątku w tle (`ASYNC_EXECUTOR`).
* **Kanał:** `mooclient/presence_v3`
* **Cechy:**
  * Zero lagów renderowania (0 ms blokowania wątku głównego).
  * Dwukierunkowa, natychmiastowa wymiana obecności (nick + UUID) między wszystkimi użytkownikami klienta na świecie.
  * Automatyczne wznawianie połączenia (*watchdog*).

---

## 2. Krytyczne Zasady dla Przyszłych Modyfikacji
1. **Nigdy nie wykonywać zapytań I/O ani blokujących operacji na wątku renderowania** (`ClientTickEvents.END_CLIENT_TICK` / Mixiny).
2. **Zachować $O(1)$ fast-path dla gracza lokalnego**, aby własne logo nigdy nie znikało.
3. **Plik moda umieszczać standardowo w folderze `.mooclient/mods/`** (zgodnie z decyzją v1.4.4+).
