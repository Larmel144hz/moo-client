# AGENTS.md — Moo Client Coding & Architecture Guidelines

## Core System Architecture

### 1. Logo & NameTag System (Over-Head & Tab List)
* **Tab List:** [`PlayerListHudMixin.java`](file:///c:/Users/laaam/.antigravity-ide/Moo%20Client/src/main/java/com/mooclient/mixin/PlayerListHudMixin.java) uses `@Redirect` on `DrawContext.drawTextWithShadow` to draw the 8x8 cow logo texture and shifts the player's name by `x + 10`.
* **Over-Head NameTag:** [`NametagBackgroundMixin.java`](file:///c:/Users/laaam/.antigravity-ide/Moo%20Client/src/main/java/com/mooclient/mixin/NametagBackgroundMixin.java) injects into `EntityRenderer`, centers the label with `originalX + (badgeTotalWidth / 2.0f)`, and renders the textured quad `RenderLayer.getText(MOO_LOGO)` before the nickname.
* **Player Verification & Caching:** [`MooUserManager.java`](file:///c:/Users/laaam/.antigravity-ide/Moo%20Client/src/main/java/com/mooclient/util/MooUserManager.java) uses an $O(1)$ concurrent cache (`LOOKUP_CACHE`), guarantees local player recognition, strips color codes, and matches tokens with server rank prefixes.
* **Global Presence & Network:** [`MooNetworkHandler.java`](file:///c:/Users/laaam/.antigravity-ide/Moo%20Client/src/main/java/com/mooclient/network/MooNetworkHandler.java) communicates via a low-priority background daemon thread connected to HiveMQ MQTT (`broker.hivemq.com:1883`, topic `mooclient/presence_v3`). Never perform blocking I/O on the main render thread.

### 2. Mod Placement & Distribution
* The Fabric mod `.jar` is placed in `.mooclient/mods/` as a standard user mod.
* Releases are deployed via `launcher/create-release.js` to GitHub with tag `v<version>`.
