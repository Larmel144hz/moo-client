package com.mooclient.gui;

import com.mooclient.util.MooClientSettings;
import com.mooclient.util.MooLanguage;
import com.mooclient.waypoint.Waypoint;
import com.mooclient.waypoint.WaypointManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Lunar Client inspired Waypoint Manager Screen.
 * Left Side: List of active waypoints with visibility toggle, teleport and delete buttons.
 * Right Side: Create new waypoint with custom name, coordinates, dimension, color palette and beam toggle.
 */
public class MooWaypointScreen extends Screen {

    private static final Identifier COW_LOGO = Identifier.of("minecraft", "icons/icon_128x128.png");

    // Colors
    private static final int COLOR_PANEL_BG = 0xF4111116;
    private static final int COLOR_PANEL_BORDER = 0x44FFFFFF;
    private static final int COLOR_CARD_BG = 0x88181824;
    private static final int COLOR_CARD_HOVER = 0xCC222232;
    private static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_MUTED = 0xFFA0A0AB;
    private static final int COLOR_INPUT_BG = 0x990A0A10;
    private static final int COLOR_INPUT_BORDER = 0x44FFFFFF;

    // Palette presets for waypoints
    private static final int[] COLOR_PRESETS = new int[]{
            0xFF5555, // Red
            0x55FF55, // Lime Green
            0x55FFFF, // Cyan
            0xFFFF55, // Yellow
            0xFF55FF, // Purple / Magenta
            0xFFAA00, // Orange
            0x5555FF, // Blue
            0xFFFFFF  // White
    };

    // State for creating a new waypoint
    private String newName = "";
    private String newX = "0";
    private String newY = "64";
    private String newZ = "0";
    private String newDimension = "minecraft:overworld";
    private int selectedColorIndex = 2; // Cyan by default

    // Active focused input: 0 = None, 1 = Search, 2 = Name, 3 = X, 4 = Y, 5 = Z
    private int activeInput = 2;
    private String searchFilter = "";
    private double scrollY = 0;

    public MooWaypointScreen() {
        super(Text.literal("Moo Client Waypoints"));
    }

    @Override
    protected void init() {
        super.init();
        this.newName = "";
        this.activeInput = 2;
        if (this.client != null && this.client.player != null) {
            this.newX = String.valueOf((int) Math.round(this.client.player.getX()));
            this.newY = String.valueOf((int) Math.round(this.client.player.getY()));
            this.newZ = String.valueOf((int) Math.round(this.client.player.getZ()));
            this.newDimension = WaypointManager.getCurrentDimension(this.client);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Disabled to prevent vanilla screen blur
    }

    @Override
    public void renderInGameBackground(DrawContext context) {
        // Disabled to prevent vanilla screen blur
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int dimColor = MooClientSettings.getBackgroundDimColor();
        context.fillGradient(0, 0, this.width, this.height, dimColor, dimColor);

        int panelW = 590;
        int panelH = 295;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Main Background Panel
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, COLOR_PANEL_BG);
        drawBorder(context, panelX, panelY, panelW, panelH, COLOR_PANEL_BORDER);

        // Header
        int headerH = 46;
        int backX = panelX + 14;
        int backY = panelY + 12;
        int backW = 74;
        int backH = 22;
        boolean backHover = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH;
        int backTextColor = backHover ? COLOR_TEXT_WHITE : COLOR_TEXT_MUTED;
        context.drawTextWithShadow(this.textRenderer, MooLanguage.get("back"), backX, backY + 3, backTextColor);

        // Header Logo & Title
        int logoSize = 22;
        context.drawTexture(RenderLayer::getGuiTextured, COW_LOGO, panelX + 96, panelY + 12, 0.0f, 0.0f, logoSize, logoSize, logoSize, logoSize);
        context.drawTextWithShadow(this.textRenderer, "PUNKTY NAWIGACYJNE • WAYPOINTS", panelX + 124, panelY + 12, COLOR_TEXT_WHITE);
        context.drawTextWithShadow(this.textRenderer, "Zarządzaj punktami i twórz nowe cele nawigacji", panelX + 124, panelY + 24, COLOR_TEXT_MUTED);

        // Divider Line
        context.fill(panelX + 14, panelY + headerH, panelX + panelW - 14, panelY + headerH + 1, 0x22FFFFFF);

        // Split Panels
        int leftW = 320;
        int leftH = panelH - headerH - 18;
        int leftX = panelX + 14;
        int leftY = panelY + headerH + 10;

        int rightW = panelW - leftW - 38;
        int rightH = leftH;
        int rightX = leftX + leftW + 12;
        int rightY = leftY;

        // Vertical divider between left and right panel
        context.fill(rightX - 6, leftY, rightX - 5, leftY + leftH, 0x22FFFFFF);

        // Render Left Panel (Waypoints List)
        renderWaypointsList(context, leftX, leftY, leftW, leftH, mouseX, mouseY);

        // Render Right Panel (Create Waypoint Form)
        renderCreateForm(context, rightX, rightY, rightW, rightH, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Renders Left Panel: Search bar + scrollable list of waypoints with quick action buttons.
     */
    private void renderWaypointsList(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        // Search bar
        int searchH = 20;
        int searchY = y;
        boolean searchFocused = (activeInput == 1);
        int searchBorder = searchFocused ? MooClientSettings.getAccentColor() : COLOR_INPUT_BORDER;

        context.fill(x, searchY, x + w, searchY + searchH, COLOR_INPUT_BG);
        drawBorder(context, x, searchY, w, searchH, searchBorder);

        String searchDisp = searchFilter.isEmpty() ? (searchFocused ? "" : "🔍 Szukaj punktu...") : searchFilter;
        int searchColor = searchFilter.isEmpty() && !searchFocused ? COLOR_TEXT_MUTED : COLOR_TEXT_WHITE;
        context.drawTextWithShadow(this.textRenderer, searchDisp, x + 8, searchY + 6, searchColor);

        // Waypoints List Area
        int listY = searchY + searchH + 8;
        int listH = h - searchH - 8;

        List<Waypoint> allWps = (this.client != null) ? WaypointManager.getInstance().getWaypointsForCurrentWorld(this.client) : WaypointManager.getInstance().getAllWaypoints();
        List<Waypoint> filteredWps;
        if (searchFilter == null || searchFilter.trim().isEmpty()) {
            filteredWps = allWps;
        } else {
            String query = searchFilter.trim().toLowerCase();
            filteredWps = allWps.stream().filter(wp -> wp.getName().toLowerCase().contains(query)).toList();
        }

        if (filteredWps.isEmpty()) {
            drawCenteredText(context, "Brak waypointów w tym świecie.", x + w / 2, listY + 40, COLOR_TEXT_MUTED);
            drawCenteredText(context, "Stwórz nowy punkt po prawej stronie!", x + w / 2, listY + 54, 0x88FFFFFF);
            return;
        }

        int cardH = 38;
        int cardGap = 6;
        int maxScroll = Math.max(0, filteredWps.size() * (cardH + cardGap) - listH);
        scrollY = Math.max(0, Math.min(maxScroll, scrollY));

        // Enable scissor for smooth scroll clipping
        context.enableScissor(x, listY, x + w, listY + listH);

        for (int i = 0; i < filteredWps.size(); i++) {
            Waypoint wp = filteredWps.get(i);
            int cardY = listY + i * (cardH + cardGap) - (int) scrollY;

            if (cardY + cardH < listY || cardY > listY + listH) continue;

            boolean cardHover = mouseX >= x && mouseX <= x + w && mouseY >= cardY && mouseY <= cardY + cardH;
            int cardBg = cardHover ? COLOR_CARD_HOVER : COLOR_CARD_BG;
            int cardBorder = wp.isVisible() ? (cardHover ? 0x88FFFFFF : 0x33FFFFFF) : 0x22555566;

            context.fill(x, cardY, x + w, cardY + cardH, cardBg);
            drawBorder(context, x, cardY, w, cardH, cardBorder);

            // Left color strip
            context.fill(x + 2, cardY + 2, x + 5, cardY + cardH - 2, wp.getColor() | 0xFF000000);

            // Distance calculation
            int dist = 0;
            if (this.client != null && this.client.player != null) {
                double dx = wp.getX() - this.client.player.getX();
                double dy = wp.getY() - this.client.player.getY();
                double dz = wp.getZ() - this.client.player.getZ();
                dist = (int) Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
            }

            // Name & Distance
            String nameText = wp.getName();
            int nameColor = wp.isVisible() ? COLOR_TEXT_WHITE : COLOR_TEXT_MUTED;
            context.drawTextWithShadow(this.textRenderer, nameText, x + 10, cardY + 6, nameColor);

            String distText = dist + "m";
            int distW = this.textRenderer.getWidth(distText);
            int distBadgeX = x + 12 + this.textRenderer.getWidth(nameText);
            if (distBadgeX + distW + 6 < x + w - 80) {
                context.fill(distBadgeX, cardY + 5, distBadgeX + distW + 6, cardY + 16, 0x44000000);
                context.drawTextWithShadow(this.textRenderer, distText, distBadgeX + 3, cardY + 6, MooClientSettings.getAccentColor());
            }

            // Coordinates & Dimension Subtitle
            String subText = wp.getFormattedCoords() + " • " + wp.getDimensionDisplayName();
            context.drawTextWithShadow(this.textRenderer, subText, x + 10, cardY + 20, COLOR_TEXT_MUTED);

            // Right Action Buttons:
            // 1. Toggle Visibility (Eye)
            int btnSize = 18;
            int visBtnX = x + w - 74;
            int visBtnY = cardY + 10;
            boolean visHover = mouseX >= visBtnX && mouseX <= visBtnX + btnSize && mouseY >= visBtnY && mouseY <= visBtnY + btnSize;
            int visBg = wp.isVisible() ? (visHover ? 0xCC1E3A2B : 0x880E2318) : (visHover ? 0xCC3A1E1E : 0x88230E0E);
            int visBorder = wp.isVisible() ? MooClientSettings.getAccentColor() : 0x66FF5555;
            context.fill(visBtnX, visBtnY, visBtnX + btnSize, visBtnY + btnSize, visBg);
            drawBorder(context, visBtnX, visBtnY, btnSize, btnSize, visBorder);
            drawCenteredText(context, wp.isVisible() ? "👁" : "🕶", visBtnX + btnSize / 2, visBtnY + 4, COLOR_TEXT_WHITE);

            // 2. Teleport Button (TP)
            int tpBtnX = x + w - 50;
            int tpBtnY = cardY + 10;
            boolean tpHover = mouseX >= tpBtnX && mouseX <= tpBtnX + btnSize && mouseY >= tpBtnY && mouseY <= tpBtnY + btnSize;
            int tpBg = tpHover ? 0xCC252535 : 0x66141420;
            context.fill(tpBtnX, tpBtnY, tpBtnX + btnSize, tpBtnY + btnSize, tpBg);
            drawBorder(context, tpBtnX, tpBtnY, btnSize, btnSize, tpHover ? 0xAAFFFFFF : 0x33FFFFFF);
            drawCenteredText(context, "🎯", tpBtnX + btnSize / 2, tpBtnY + 4, COLOR_TEXT_WHITE);

            // 3. Delete Button (Trash)
            int delBtnX = x + w - 26;
            int delBtnY = cardY + 10;
            boolean delHover = mouseX >= delBtnX && mouseX <= delBtnX + btnSize && mouseY >= delBtnY && mouseY <= delBtnY + btnSize;
            int delBg = delHover ? 0xCC441A1A : 0x66220A0A;
            context.fill(delBtnX, delBtnY, delBtnX + btnSize, delBtnY + btnSize, delBg);
            drawBorder(context, delBtnX, delBtnY, btnSize, btnSize, delHover ? 0xFFFF5555 : 0x44FF5555);
            drawCenteredText(context, "✕", delBtnX + btnSize / 2, delBtnY + 4, delHover ? 0xFFFF5555 : 0xFFA0A0AB);
        }

        context.disableScissor();
    }

    /**
     * Renders Right Panel: Form for creating a new waypoint.
     */
    private void renderCreateForm(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        context.drawTextWithShadow(this.textRenderer, "+ NOWY WAYPOINT", x, y + 2, MooClientSettings.getAccentColor());

        int curY = y + 18;

        // 1. Name Input
        context.drawTextWithShadow(this.textRenderer, "Nazwa punktu:", x, curY, COLOR_TEXT_WHITE);
        curY += 12;
        int inputH = 20;
        boolean nameFocused = (activeInput == 2);
        context.fill(x, curY, x + w, curY + inputH, COLOR_INPUT_BG);
        drawBorder(context, x, curY, w, inputH, nameFocused ? MooClientSettings.getAccentColor() : COLOR_INPUT_BORDER);
        String nameDisp = newName.isEmpty() ? (nameFocused ? "" : "Wpisz nazwę...") : newName;
        int nameCol = newName.isEmpty() && !nameFocused ? COLOR_TEXT_MUTED : COLOR_TEXT_WHITE;
        context.drawTextWithShadow(this.textRenderer, nameDisp + (nameFocused ? "_" : ""), x + 6, curY + 6, nameCol);

        curY += inputH + 8;

        // 2. Coordinates X, Y, Z + Quick "My Pos" Button
        context.drawTextWithShadow(this.textRenderer, "Współrzędne (X / Y / Z):", x, curY, COLOR_TEXT_WHITE);
        curY += 12;
        int coordW = (w - 8) / 3;

        // X Input
        boolean xFocused = (activeInput == 3);
        context.fill(x, curY, x + coordW, curY + inputH, COLOR_INPUT_BG);
        drawBorder(context, x, curY, coordW, inputH, xFocused ? MooClientSettings.getAccentColor() : COLOR_INPUT_BORDER);
        context.drawTextWithShadow(this.textRenderer, newX + (xFocused ? "_" : ""), x + 4, curY + 6, COLOR_TEXT_WHITE);

        // Y Input
        boolean yFocused = (activeInput == 4);
        int yBoxX = x + coordW + 4;
        context.fill(yBoxX, curY, yBoxX + coordW, curY + inputH, COLOR_INPUT_BG);
        drawBorder(context, yBoxX, curY, coordW, inputH, yFocused ? MooClientSettings.getAccentColor() : COLOR_INPUT_BORDER);
        context.drawTextWithShadow(this.textRenderer, newY + (yFocused ? "_" : ""), yBoxX + 4, curY + 6, COLOR_TEXT_WHITE);

        // Z Input
        boolean zFocused = (activeInput == 5);
        int zBoxX = yBoxX + coordW + 4;
        context.fill(zBoxX, curY, zBoxX + coordW, curY + inputH, COLOR_INPUT_BG);
        drawBorder(context, zBoxX, curY, coordW, inputH, zFocused ? MooClientSettings.getAccentColor() : COLOR_INPUT_BORDER);
        context.drawTextWithShadow(this.textRenderer, newZ + (zFocused ? "_" : ""), zBoxX + 4, curY + 6, COLOR_TEXT_WHITE);

        curY += inputH + 4;

        // Quick "Pobierz moją pozycję" button
        int myPosH = 16;
        boolean myPosHover = mouseX >= x && mouseX <= x + w && mouseY >= curY && mouseY <= curY + myPosH;
        context.fill(x, curY, x + w, curY + myPosH, myPosHover ? 0xCC252535 : 0x66141420);
        drawBorder(context, x, curY, w, myPosH, myPosHover ? 0x88FFFFFF : 0x22FFFFFF);
        drawCenteredText(context, "📍 Użyj mojej pozycji", x + w / 2, curY + 4, myPosHover ? COLOR_TEXT_WHITE : COLOR_TEXT_MUTED);

        curY += myPosH + 8;

        // 3. Dimension Selector Tabs
        context.drawTextWithShadow(this.textRenderer, "Wymiar (Dimension):", x, curY, COLOR_TEXT_WHITE);
        curY += 12;
        String[] dims = new String[]{"Overworld", "Nether", "End"};
        String[] dimKeys = new String[]{"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};
        int dimTabW = (w - 4) / 3;

        for (int i = 0; i < 3; i++) {
            int tx = x + i * (dimTabW + 2);
            boolean isSel = newDimension.equalsIgnoreCase(dimKeys[i]);
            boolean dHover = mouseX >= tx && mouseX <= tx + dimTabW && mouseY >= curY && mouseY <= curY + 18;

            int bg = isSel ? MooClientSettings.getAccentColor() : (dHover ? 0xCC252535 : 0x66141420);
            int border = isSel ? MooClientSettings.getAccentHoverColor() : (dHover ? 0x88FFFFFF : 0x33FFFFFF);
            int txtCol = isSel ? 0xFF0A2514 : (dHover ? COLOR_TEXT_WHITE : COLOR_TEXT_MUTED);

            context.fill(tx, curY, tx + dimTabW, curY + 18, bg);
            drawBorder(context, tx, curY, dimTabW, 18, border);
            drawCenteredText(context, dims[i], tx + dimTabW / 2, curY + 5, txtCol);
        }

        curY += 18 + 8;

        // 4. Color Palette
        context.drawTextWithShadow(this.textRenderer, "Kolor punktu:", x, curY, COLOR_TEXT_WHITE);
        curY += 12;
        int swatchW = (w - 14) / 4;
        int swatchH = 14;

        for (int i = 0; i < COLOR_PRESETS.length; i++) {
            int col = i % 4;
            int row = i / 4;
            int sx = x + col * (swatchW + 4);
            int sy = curY + row * (swatchH + 4);

            boolean isColSel = (selectedColorIndex == i);
            boolean sHover = mouseX >= sx && mouseX <= sx + swatchW && mouseY >= sy && mouseY <= sy + swatchH;

            context.fill(sx, sy, sx + swatchW, sy + swatchH, COLOR_PRESETS[i] | 0xFF000000);
            if (isColSel) {
                drawBorder(context, sx - 1, sy - 1, swatchW + 2, swatchH + 2, 0xFFFFFFFF);
            } else if (sHover) {
                drawBorder(context, sx, sy, swatchW, swatchH, 0x88FFFFFF);
            }
        }

        curY += (swatchH + 4) * 2 + 12;

        // 5. Submit Button: [ + STWÓRZ WAYPOINT ]
        int subBtnH = 24;
        boolean subHover = mouseX >= x && mouseX <= x + w && mouseY >= curY && mouseY <= curY + subBtnH;
        int subBg = subHover ? MooClientSettings.getAccentHoverColor() : MooClientSettings.getAccentColor();
        context.fill(x, curY, x + w, curY + subBtnH, subBg);
        drawBorder(context, x, curY, w, subBtnH, 0xFFFFFFFF);
        drawCenteredText(context, "+ STWÓRZ WAYPOINT", x + w / 2, curY + 7, 0xFF0A2514);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int panelW = 590;
            int panelH = 295;
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;
            int headerH = 46;

            // Back Button Click
            int backX = panelX + 14;
            int backY = panelY + 12;
            int backW = 74;
            int backH = 22;
            if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
                playClickSound();
                this.close();
                return true;
            }

            int leftW = 320;
            int leftH = panelH - headerH - 18;
            int leftX = panelX + 14;
            int leftY = panelY + headerH + 10;

            int rightW = panelW - leftW - 38;
            int rightH = leftH;
            int rightX = leftX + leftW + 12;
            int rightY = leftY;

            // 1. Search Bar Click
            int searchH = 20;
            if (mouseX >= leftX && mouseX <= leftX + leftW && mouseY >= leftY && mouseY <= leftY + searchH) {
                activeInput = 1;
                playClickSound();
                return true;
            }

            // 2. Left List Waypoint Action Clicks
            int listY = leftY + searchH + 8;
            int listH = leftH - searchH - 8;

            if (mouseX >= leftX && mouseX <= leftX + leftW && mouseY >= listY && mouseY <= listY + listH) {
                List<Waypoint> allWps = (this.client != null) ? WaypointManager.getInstance().getWaypointsForCurrentWorld(this.client) : WaypointManager.getInstance().getAllWaypoints();
                List<Waypoint> filteredWps;
                if (searchFilter == null || searchFilter.trim().isEmpty()) {
                    filteredWps = allWps;
                } else {
                    String query = searchFilter.trim().toLowerCase();
                    filteredWps = allWps.stream().filter(wp -> wp.getName().toLowerCase().contains(query)).toList();
                }

                int cardH = 38;
                int cardGap = 6;

                for (int i = 0; i < filteredWps.size(); i++) {
                    Waypoint wp = filteredWps.get(i);
                    int cardY = listY + i * (cardH + cardGap) - (int) scrollY;

                    if (cardY + cardH < listY || cardY > listY + listH) continue;

                    int btnSize = 18;
                    int visBtnX = leftX + leftW - 74;
                    int tpBtnX = leftX + leftW - 50;
                    int delBtnX = leftX + leftW - 26;
                    int btnY = cardY + 10;

                    // Toggle Visibility
                    if (mouseX >= visBtnX && mouseX <= visBtnX + btnSize && mouseY >= btnY && mouseY <= btnY + btnSize) {
                        playClickSound();
                        WaypointManager.getInstance().toggleWaypoint(wp.getId());
                        return true;
                    }

                    // Teleport Command
                    if (mouseX >= tpBtnX && mouseX <= tpBtnX + btnSize && mouseY >= btnY && mouseY <= btnY + btnSize) {
                        playClickSound();
                        if (this.client != null && this.client.player != null) {
                            String cmd = String.format("tp @s %.0f %.0f %.0f", wp.getX(), wp.getY(), wp.getZ());
                            if (this.client.getNetworkHandler() != null) {
                                this.client.getNetworkHandler().sendChatCommand(cmd);
                            }
                            this.close();
                        }
                        return true;
                    }

                    // Delete Waypoint
                    if (mouseX >= delBtnX && mouseX <= delBtnX + btnSize && mouseY >= btnY && mouseY <= btnY + btnSize) {
                        playClickSound();
                        WaypointManager.getInstance().removeWaypoint(wp.getId());
                        return true;
                    }
                }
            }

            // 3. Right Panel Form Inputs
            int curY = rightY + 18 + 12;
            int inputH = 20;

            // Click Name Field
            if (mouseX >= rightX && mouseX <= rightX + rightW && mouseY >= curY && mouseY <= curY + inputH) {
                activeInput = 2;
                playClickSound();
                return true;
            }

            curY += inputH + 8 + 12;
            int coordW = (rightW - 8) / 3;

            // Click X Field
            if (mouseX >= rightX && mouseX <= rightX + coordW && mouseY >= curY && mouseY <= curY + inputH) {
                activeInput = 3;
                playClickSound();
                return true;
            }

            // Click Y Field
            int yBoxX = rightX + coordW + 4;
            if (mouseX >= yBoxX && mouseX <= yBoxX + coordW && mouseY >= curY && mouseY <= curY + inputH) {
                activeInput = 4;
                playClickSound();
                return true;
            }

            // Click Z Field
            int zBoxX = yBoxX + coordW + 4;
            if (mouseX >= zBoxX && mouseX <= zBoxX + coordW && mouseY >= curY && mouseY <= curY + inputH) {
                activeInput = 5;
                playClickSound();
                return true;
            }

            curY += inputH + 4;

            // Click "Użyj mojej pozycji" button
            int myPosH = 16;
            if (mouseX >= rightX && mouseX <= rightX + rightW && mouseY >= curY && mouseY <= curY + myPosH) {
                playClickSound();
                if (this.client != null && this.client.player != null) {
                    this.newX = String.valueOf((int) Math.round(this.client.player.getX()));
                    this.newY = String.valueOf((int) Math.round(this.client.player.getY()));
                    this.newZ = String.valueOf((int) Math.round(this.client.player.getZ()));
                    this.newDimension = WaypointManager.getCurrentDimension(this.client);
                }
                return true;
            }

            curY += myPosH + 8 + 12;

            // Click Dimension Tabs
            String[] dimKeys = new String[]{"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"};
            int dimTabW = (rightW - 4) / 3;
            for (int i = 0; i < 3; i++) {
                int tx = rightX + i * (dimTabW + 2);
                if (mouseX >= tx && mouseX <= tx + dimTabW && mouseY >= curY && mouseY <= curY + 18) {
                    playClickSound();
                    this.newDimension = dimKeys[i];
                    return true;
                }
            }

            curY += 18 + 8 + 12;

            // Click Color Preset Swatches
            int swatchW = (rightW - 14) / 4;
            int swatchH = 14;
            for (int i = 0; i < COLOR_PRESETS.length; i++) {
                int col = i % 4;
                int row = i / 4;
                int sx = rightX + col * (swatchW + 4);
                int sy = curY + row * (swatchH + 4);

                if (mouseX >= sx && mouseX <= sx + swatchW && mouseY >= sy && mouseY <= sy + swatchH) {
                    playClickSound();
                    this.selectedColorIndex = i;
                    return true;
                }
            }

            curY += (swatchH + 4) * 2 + 12;

            // Click Create Waypoint Button
            int subBtnH = 24;
            if (mouseX >= rightX && mouseX <= rightX + rightW && mouseY >= curY && mouseY <= curY + subBtnH) {
                submitNewWaypoint();
                return true;
            }

            // Clicked outside inputs -> unfocus
            activeInput = 0;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void submitNewWaypoint() {
        String name = newName.trim().isEmpty() ? "Punkt #" + (WaypointManager.getInstance().getAllWaypoints().size() + 1) : newName.trim();
        double x = 0;
        double y = 64;
        double z = 0;

        try { x = Double.parseDouble(newX.trim()); } catch (Exception ignored) {}
        try { y = Double.parseDouble(newY.trim()); } catch (Exception ignored) {}
        try { z = Double.parseDouble(newZ.trim()); } catch (Exception ignored) {}

        int color = COLOR_PRESETS[selectedColorIndex];
        String server = (this.client != null) ? WaypointManager.getCurrentServerOrWorld(this.client) : "global";

        Waypoint wp = new Waypoint(name, x, y, z, newDimension, server, color, false);
        WaypointManager.getInstance().addWaypoint(wp);

        playClickSound();

        // Clear name input for the next waypoint so user can type cleanly
        this.newName = "";
        this.activeInput = 2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelW = 590;
        int panelH = 295;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        int leftW = 320;
        int leftX = panelX + 14;

        if (mouseX >= leftX && mouseX <= leftX + leftW) {
            scrollY -= verticalAmount * 18.0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr != 127) {
            if (activeInput == 1) {
                searchFilter += chr;
                scrollY = 0;
                return true;
            } else if (activeInput == 2) {
                newName += chr;
                return true;
            } else if (activeInput == 3) {
                if (Character.isDigit(chr) || chr == '-' || chr == '.') newX += chr;
                return true;
            } else if (activeInput == 4) {
                if (Character.isDigit(chr) || chr == '-' || chr == '.') newY += chr;
                return true;
            } else if (activeInput == 5) {
                if (Character.isDigit(chr) || chr == '-' || chr == '.') newZ += chr;
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeInput > 0) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (activeInput == 1 && !searchFilter.isEmpty()) {
                    searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                    scrollY = 0;
                    return true;
                } else if (activeInput == 2 && !newName.isEmpty()) {
                    newName = newName.substring(0, newName.length() - 1);
                    return true;
                } else if (activeInput == 3 && !newX.isEmpty()) {
                    newX = newX.substring(0, newX.length() - 1);
                    return true;
                } else if (activeInput == 4 && !newY.isEmpty()) {
                    newY = newY.substring(0, newY.length() - 1);
                    return true;
                } else if (activeInput == 5 && !newZ.isEmpty()) {
                    newZ = newZ.substring(0, newZ.length() - 1);
                    return true;
                }
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if (activeInput == 2 || activeInput == 3 || activeInput == 4 || activeInput == 5) {
                    submitNewWaypoint();
                } else {
                    activeInput = 0;
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_TAB) {
                activeInput = (activeInput % 5) + 1;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                activeInput = 0;
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawCenteredText(DrawContext context, String text, int centerX, int y, int color) {
        int width = this.textRenderer.getWidth(text);
        context.drawTextWithShadow(this.textRenderer, text, centerX - width / 2, y, color);
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void playClickSound() {
        if (this.client != null) {
            this.client.getSoundManager().play(
                    net.minecraft.client.sound.PositionedSoundInstance.master(
                            SoundEvents.UI_BUTTON_CLICK, 1.0f
                    )
            );
        }
    }
}
