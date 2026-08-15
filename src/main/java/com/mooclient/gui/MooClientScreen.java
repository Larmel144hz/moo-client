package com.mooclient.gui;

import com.mooclient.module.Module;
import com.mooclient.module.ModuleManager;
import com.mooclient.module.modules.FpsModule;
import com.mooclient.module.modules.FreelookModule;
import com.mooclient.module.modules.PotionEffectsModule;
import com.mooclient.module.modules.ToggleSprintModule;
import com.mooclient.util.MooLanguage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Lunar Client inspired in-game HUD, Draggable Widgets, Mods Grid & Options Screen for Moo Client.
 */
public class MooClientScreen extends Screen {

    private static final Identifier COW_LOGO = Identifier.of("minecraft", "icons/icon_128x128.png");

    private enum View {
        HUB,
        MODS,
        OPTIONS
    }

    private View currentView = View.HUB;
    private Module selectedModule = null;
    private boolean listeningForKeybind = false;
    private int listeningMacroIndex = -1;
    private int editingMacroIndex = -1;
    private double scrollY = 0;

    // Draggable HUD widget state
    private String draggingWidget = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    // Palette
    private static final int COLOR_HUB_OVERLAY = 0x55000000;
    private static final int COLOR_PANEL_BG = 0xF4111116;
    private static final int COLOR_PANEL_BORDER = 0x44FFFFFF;
    private static final int COLOR_CARD_BG = 0xAA181822;
    private static final int COLOR_CARD_HOVER = 0xDD22222E;
    private static final int COLOR_CARD_BORDER = 0x33FFFFFF;
    private static final int COLOR_CARD_BORDER_HOVER = 0x88FFFFFF;
    private static final int COLOR_OPTIONS_BG = 0x990A0A0F;
    private static final int COLOR_OPTIONS_HOVER = 0xCC1A1A26;
    private static final int COLOR_ENABLED = 0xFF2ECC71;
    private static final int COLOR_ENABLED_HOVER = 0xFF3CE282;
    private static final int COLOR_DISABLED = 0xFF353540;
    private static final int COLOR_DISABLED_HOVER = 0xFF454552;
    private static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_MUTED = 0xFFA0A0AB;

    public MooClientScreen() {
        super(Text.literal("Moo Client HUD"));
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
        if (currentView == View.HUB) {
            context.fillGradient(0, 0, this.width, this.height, COLOR_HUB_OVERLAY, COLOR_HUB_OVERLAY);
            renderLunarHub(context, mouseX, mouseY, delta);
            renderDraggableHudWidgets(context, mouseX, mouseY);
        } else if (currentView == View.MODS) {
            context.fillGradient(0, 0, this.width, this.height, 0x88000000, 0x88000000);
            renderModsWindow(context, mouseX, mouseY, delta);
        } else if (currentView == View.OPTIONS) {
            context.fillGradient(0, 0, this.width, this.height, 0x88000000, 0x88000000);
            renderOptionsWindow(context, mouseX, mouseY, delta);
        }

        // Language Switcher in top-right corner of screen
        renderLanguageSwitcher(context, this.width - 66, 12, mouseX, mouseY);
    }

    /**
     * Draggable HUD widgets with clean, minimalist preview
     */
    private void renderDraggableHudWidgets(DrawContext context, int mouseX, int mouseY) {
        if (this.client == null) return;

        // 1. Draggable FPS Widget
        if (FpsModule.isFpsEnabled()) {
            int fps = this.client.getCurrentFps();
            String fpsText = FpsModule.getStyle() == FpsModule.FpsStyle.BRACKETS ? "[" + fps + " FPS]" : (FpsModule.isShowPrefix() ? "FPS: " + fps : fps + " FPS");
            int textWidth = this.textRenderer.getWidth(fpsText);
            int boxW = textWidth + 7;
            int boxH = 12;
            FpsModule.width = boxW;
            FpsModule.height = boxH;

            int x = FpsModule.posX;
            int y = FpsModule.posY;

            boolean hovered = mouseX >= x - 3 && mouseX <= x - 3 + boxW && mouseY >= y - 2 && mouseY <= y - 2 + boxH;

            if (hovered || "FPS".equals(draggingWidget)) {
                drawBorder(context, x - 3, y - 2, boxW, boxH, 0x88FFFFFF);
            }
        }

        // 2. Draggable Sprint Widget Preview
        if (ToggleSprintModule.isSprintEnabled()) {
            ToggleSprintModule.SprintStyle style = ToggleSprintModule.getStyle();
            String sprintText;
            if (style == ToggleSprintModule.SprintStyle.BRACKETS) {
                sprintText = "[Sprinting]";
            } else if (style == ToggleSprintModule.SprintStyle.SIMPLE) {
                sprintText = "Sprinting";
            } else {
                sprintText = "Sprinting (Toggled)";
            }
            int textWidth = this.textRenderer.getWidth(sprintText);
            int boxW = textWidth + 7;
            int boxH = 12;
            ToggleSprintModule.width = boxW;
            ToggleSprintModule.height = boxH;

            int x = ToggleSprintModule.posX;
            int y = ToggleSprintModule.posY;

            boolean hovered = mouseX >= x - 3 && mouseX <= x - 3 + boxW && mouseY >= y - 2 && mouseY <= y - 2 + boxH;

            if (hovered || "SPRINT".equals(draggingWidget)) {
                drawBorder(context, x - 3, y - 2, boxW, boxH, 0x88FFFFFF);
            }
        }

        // 3. Draggable Potion Effects Widget Preview
        if (PotionEffectsModule.isModuleEnabled()) {
            int x = PotionEffectsModule.posX;
            int y = PotionEffectsModule.posY;
            int boxW = PotionEffectsModule.width;
            int boxH = PotionEffectsModule.height;

            boolean hovered = mouseX >= x - 3 && mouseX <= x - 3 + boxW && mouseY >= y - 2 && mouseY <= y - 2 + boxH;

            if (hovered || "POTIONS".equals(draggingWidget)) {
                drawBorder(context, x - 3, y - 2, boxW, boxH, 0x88FFFFFF);
            }
        }

        // 4. Draggable Ping Widget Preview
        if (com.mooclient.module.modules.PingModule.isPingEnabled()) {
            int ping = com.mooclient.module.modules.PingModule.getCurrentPing();
            String pingText = com.mooclient.module.modules.PingModule.getStyle() == com.mooclient.module.modules.PingModule.PingStyle.BRACKETS ? "[" + ping + " ms]" : (com.mooclient.module.modules.PingModule.isShowPrefix() ? "Ping: " + ping + " ms" : ping + " ms");
            int textWidth = this.textRenderer.getWidth(pingText);
            int boxW = textWidth + 7;
            int boxH = 12;
            com.mooclient.module.modules.PingModule.width = boxW;
            com.mooclient.module.modules.PingModule.height = boxH;

            int x = com.mooclient.module.modules.PingModule.posX;
            int y = com.mooclient.module.modules.PingModule.posY;

            boolean hovered = mouseX >= x - 3 && mouseX <= x - 3 + boxW && mouseY >= y - 2 && mouseY <= y - 2 + boxH;

            if (hovered || "PING".equals(draggingWidget)) {
                drawBorder(context, x - 3, y - 2, boxW, boxH, 0x88FFFFFF);
            }
        }
    }

    /**
     * Top-right PL / EN Language Switcher
     */
    private void renderLanguageSwitcher(DrawContext context, int x, int y, int mouseX, int mouseY) {
        int pillW = 26;
        int pillH = 18;
        int gap = 2;

        int plX = x;
        int enX = x + pillW + gap;

        boolean isPl = MooLanguage.current == MooLanguage.PL;
        boolean plHover = mouseX >= plX && mouseX <= plX + pillW && mouseY >= y && mouseY <= y + pillH;
        boolean enHover = mouseX >= enX && mouseX <= enX + pillW && mouseY >= y && mouseY <= y + pillH;

        context.fill(x - 2, y - 2, x + (pillW * 2) + gap + 2, y + pillH + 2, 0x990E0E14);
        drawBorder(context, x - 2, y - 2, (pillW * 2) + gap + 4, pillH + 4, 0x33FFFFFF);

        int plBg = isPl ? 0x44FFFFFF : (plHover ? 0x22FFFFFF : 0x00000000);
        context.fill(plX, y, plX + pillW, y + pillH, plBg);
        if (isPl) drawBorder(context, plX, y, pillW, pillH, 0x66FFFFFF);
        int plTextColor = isPl ? COLOR_TEXT_WHITE : (plHover ? COLOR_TEXT_WHITE : 0x88AAAAAA);
        drawCenteredText(context, "PL", plX + pillW / 2, y + 5, plTextColor);

        int enBg = !isPl ? 0x44FFFFFF : (enHover ? 0x22FFFFFF : 0x00000000);
        context.fill(enX, y, enX + pillW, y + pillH, enBg);
        if (!isPl) drawBorder(context, enX, y, pillW, pillH, 0x66FFFFFF);
        int enTextColor = !isPl ? COLOR_TEXT_WHITE : (enHover ? COLOR_TEXT_WHITE : 0x88AAAAAA);
        drawCenteredText(context, "EN", enX + pillW / 2, y + 5, enTextColor);
    }

    /**
     * HUB VIEW: Centered Cow Logo, MOO CLIENT name, and single [ MODS ] button
     */
    private void renderLunarHub(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int btnW = 140;
        int btnH = 32;
        int btnX = centerX - btnW / 2;
        int btnY = centerY - btnH / 2;

        int titleY = btnY - 18;
        String title = "MOO CLIENT";
        int titleWidth = this.textRenderer.getWidth(title);

        int logoSize = 64;
        int logoY = titleY - logoSize - 8;
        context.drawTexture(RenderLayer::getGuiTextured, COW_LOGO, centerX - logoSize / 2, logoY, 0.0f, 0.0f, logoSize, logoSize, logoSize, logoSize);
        context.drawTextWithShadow(this.textRenderer, title, centerX - titleWidth / 2, titleY, COLOR_TEXT_WHITE);

        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        int bg = hovered ? 0x99202028 : 0x55000000;
        int border = hovered ? 0xEEFFFFFF : 0x55FFFFFF;
        context.fill(btnX, btnY, btnX + btnW, btnY + btnH, bg);
        drawBorder(context, btnX, btnY, btnW, btnH, border);
        drawCenteredText(context, "MODS", centerX, btnY + (btnH - 8) / 2, COLOR_TEXT_WHITE);
    }

    /**
     * MODS WINDOW: 3-column scrollable grid of mod cards with OPTIONS bar
     */
    private void renderModsWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelW = 560;
        int panelH = 260;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, COLOR_PANEL_BG);
        drawBorder(context, panelX, panelY, panelW, panelH, COLOR_PANEL_BORDER);

        int headerH = 42;
        int backX = panelX + 14;
        int backY = panelY + 12;
        int backW = 74;
        int backH = 22;
        boolean backHover = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH;
        int backTextColor = backHover ? COLOR_TEXT_WHITE : 0xFFA0A0AB;
        context.drawTextWithShadow(this.textRenderer, MooLanguage.get("back"), backX, backY + 3, backTextColor);

        String headerTitle = "MOO CLIENT";
        int titleW = this.textRenderer.getWidth(headerTitle);
        context.drawTextWithShadow(this.textRenderer, headerTitle, panelX + (panelW - titleW) / 2, panelY + 15, COLOR_TEXT_WHITE);

        context.fill(panelX + 14, panelY + headerH, panelX + panelW - 14, panelY + headerH + 1, 0x22FFFFFF);

        List<Module> modules = ModuleManager.getInstance().getModules();
        int cols = 3;
        int cardW = 160;
        int cardH = 150;
        int cardGap = 16;

        int totalGridW = cols * cardW + (cols - 1) * cardGap;
        int startX = panelX + (panelW - totalGridW) / 2;
        int startY = panelY + headerH + 16;

        int totalRows = (modules.size() + cols - 1) / cols;
        int totalContentH = totalRows * cardH + (totalRows - 1) * cardGap;
        int visibleAreaH = panelH - headerH - 24;
        int maxScroll = Math.max(0, totalContentH - visibleAreaH + 8);
        scrollY = Math.max(0, Math.min(maxScroll, scrollY));

        // Scissor clipping for scrollable area
        context.enableScissor(panelX + 4, panelY + headerH + 2, panelX + panelW - 4, panelY + panelH - 4);

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int col = i % cols;
            int row = i / cols;
            int cardX = startX + col * (cardW + cardGap);
            int cardY = startY + row * (cardH + cardGap) - (int) scrollY;

            boolean cardHover = mouseX >= cardX && mouseX <= cardX + cardW && mouseY >= cardY && mouseY <= cardY + cardH
                    && mouseY >= panelY + headerH + 2 && mouseY <= panelY + panelH - 4;

            int bg = cardHover ? COLOR_CARD_HOVER : COLOR_CARD_BG;
            int border = cardHover ? COLOR_CARD_BORDER_HOVER : COLOR_CARD_BORDER;
            context.fill(cardX, cardY, cardX + cardW, cardY + cardH, bg);
            drawBorder(context, cardX, cardY, cardW, cardH, border);

            // Icon
            String icon;
            if (module.getName().equalsIgnoreCase("Gamma")) {
                icon = "☀";
            } else if (module.getName().equalsIgnoreCase("FPS")) {
                icon = "⚡";
            } else if (module.getName().equalsIgnoreCase("Sprint")) {
                icon = "🏃";
            } else if (module.getName().equalsIgnoreCase("Freelook")) {
                icon = "👁";
            } else if (module.getName().equalsIgnoreCase("Potion Effects")) {
                icon = "🧪";
            } else if (module.getName().equalsIgnoreCase("Nametags")) {
                icon = "🏷";
            } else if (module.getName().equalsIgnoreCase("Zoom")) {
                icon = "🔍";
            } else if (module.getName().equalsIgnoreCase("Chat")) {
                icon = "💬";
            } else if (module.getName().equalsIgnoreCase("Ping")) {
                icon = "📡";
            } else {
                icon = "⌨";
            }
            drawCenteredText(context, icon, cardX + cardW / 2, cardY + 16, COLOR_TEXT_WHITE);
            drawCenteredText(context, module.getName(), cardX + cardW / 2, cardY + 38, COLOR_TEXT_WHITE);

            // Description
            String desc;
            if (module.getName().equalsIgnoreCase("Gamma")) {
                desc = MooLanguage.get("gamma_desc");
            } else if (module.getName().equalsIgnoreCase("FPS")) {
                desc = MooLanguage.get("fps_desc");
            } else if (module.getName().equalsIgnoreCase("Sprint")) {
                desc = MooLanguage.get("sprint_desc");
            } else if (module.getName().equalsIgnoreCase("Freelook")) {
                desc = MooLanguage.get("freelook_desc");
            } else if (module.getName().equalsIgnoreCase("Potion Effects")) {
                desc = MooLanguage.get("potions_desc");
            } else if (module.getName().equalsIgnoreCase("Nametags")) {
                desc = MooLanguage.get("nametags_desc");
            } else if (module.getName().equalsIgnoreCase("Zoom")) {
                desc = MooLanguage.get("zoom_desc");
            } else if (module.getName().equalsIgnoreCase("Chat")) {
                desc = MooLanguage.get("chat_desc");
            } else if (module.getName().equalsIgnoreCase("Ping")) {
                desc = MooLanguage.get("ping_desc");
            } else {
                desc = MooLanguage.get("macro_desc");
            }
            context.drawTextWithShadow(this.textRenderer, desc, cardX + (cardW - this.textRenderer.getWidth(desc)) / 2, cardY + 54, COLOR_TEXT_MUTED);

            // OPTIONS Bar
            int optH = 20;
            int optY = cardY + cardH - 52;
            int optX = cardX + 8;
            int optW = cardW - 16;
            boolean optHover = mouseX >= optX && mouseX <= optX + optW && mouseY >= optY && mouseY <= optY + optH
                    && mouseY >= panelY + headerH + 2 && mouseY <= panelY + panelH - 4;
            context.fill(optX, optY, optX + optW, optY + optH, optHover ? COLOR_OPTIONS_HOVER : COLOR_OPTIONS_BG);
            drawBorder(context, optX, optY, optW, optH, optHover ? 0x88FFFFFF : 0x22FFFFFF);
            context.drawTextWithShadow(this.textRenderer, "OPTIONS", optX + 6, optY + 6, COLOR_TEXT_WHITE);
            context.drawTextWithShadow(this.textRenderer, "⚙", optX + optW - 14, optY + 6, COLOR_TEXT_WHITE);

            // ENABLED / DISABLED Button
            int btnH = 22;
            int btnY = cardY + cardH - 28;
            int btnX = cardX + 8;
            int btnW = cardW - 16;

            boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH
                    && mouseY >= panelY + headerH + 2 && mouseY <= panelY + panelH - 4;
            int statusBg = module.isEnabled() ? (btnHover ? COLOR_ENABLED_HOVER : COLOR_ENABLED) : (btnHover ? COLOR_DISABLED_HOVER : COLOR_DISABLED);

            context.fill(btnX, btnY, btnX + btnW, btnY + btnH, statusBg);
            drawBorder(context, btnX, btnY, btnW, btnH, 0x44FFFFFF);

            String statusText = module.isEnabled() ? "ENABLED" : "DISABLED";
            int statusTextColor = module.isEnabled() ? 0xFF082212 : 0xFFA0A0AB;
            drawCenteredText(context, statusText, cardX + cardW / 2, btnY + 7, statusTextColor);
        }

        context.disableScissor();

        // Draw vertical scrollbar if needed
        if (maxScroll > 0) {
            int scrollTrackX = panelX + panelW - 8;
            int scrollTrackY = panelY + headerH + 10;
            int scrollTrackH = visibleAreaH;
            int thumbH = Math.max(22, (int) ((float) visibleAreaH / (visibleAreaH + maxScroll) * scrollTrackH));
            int thumbY = scrollTrackY + (int) ((scrollY / (float) maxScroll) * (scrollTrackH - thumbH));
            context.fill(scrollTrackX, scrollTrackY, scrollTrackX + 3, scrollTrackY + scrollTrackH, 0x33000000);
            context.fill(scrollTrackX, thumbY, scrollTrackX + 3, thumbY + thumbH, 0x88FFFFFF);
        }
    }

    /**
     * MOD OPTIONS SCREEN: Lunar Client styled options window
     */
    private void renderOptionsWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelW = 480;
        int panelH = 270;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, COLOR_PANEL_BG);
        drawBorder(context, panelX, panelY, panelW, panelH, COLOR_PANEL_BORDER);

        int headerH = 46;
        int backX = panelX + 14;
        int backY = panelY + 12;
        int backW = 74;
        int backH = 22;
        boolean backHover = mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH;
        int backTextColor = backHover ? COLOR_TEXT_WHITE : 0xFFA0A0AB;
        context.drawTextWithShadow(this.textRenderer, MooLanguage.get("back"), backX, backY + 3, backTextColor);

        String modName = selectedModule != null ? selectedModule.getName() : "FPS";
        String optTitle;
        String optSubtitle;
        if (modName.equalsIgnoreCase("FPS")) {
            optTitle = MooLanguage.get("fps_opt_title");
            optSubtitle = MooLanguage.get("fps_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Ping")) {
            optTitle = MooLanguage.get("ping_opt_title");
            optSubtitle = MooLanguage.get("ping_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Sprint")) {
            optTitle = MooLanguage.get("sprint_opt_title");
            optSubtitle = MooLanguage.get("sprint_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Freelook")) {
            optTitle = MooLanguage.get("freelook_opt_title");
            optSubtitle = MooLanguage.get("freelook_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Potion Effects")) {
            optTitle = MooLanguage.get("potions_opt_title");
            optSubtitle = MooLanguage.get("potions_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Nametags")) {
            optTitle = MooLanguage.get("nametags_opt_title");
            optSubtitle = MooLanguage.get("nametags_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Zoom")) {
            optTitle = MooLanguage.get("zoom_opt_title");
            optSubtitle = MooLanguage.get("zoom_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Chat")) {
            optTitle = MooLanguage.get("chat_opt_title");
            optSubtitle = MooLanguage.get("chat_opt_subtitle");
        } else if (modName.equalsIgnoreCase("Macro")) {
            optTitle = MooLanguage.get("macro_opt_title");
            optSubtitle = MooLanguage.get("macro_opt_subtitle");
        } else {
            optTitle = MooLanguage.get("gamma_opt_title");
            optSubtitle = MooLanguage.get("gamma_opt_subtitle");
        }

        context.drawTextWithShadow(this.textRenderer, optTitle, panelX + 100, panelY + 14, COLOR_TEXT_WHITE);
        context.drawTextWithShadow(this.textRenderer, optSubtitle, panelX + 100, panelY + 28, COLOR_TEXT_MUTED);

        context.fill(panelX + 14, panelY + headerH, panelX + panelW - 14, panelY + headerH + 1, 0x22FFFFFF);

        int rowY = panelY + headerH + 14;
        int rowH = 34;
        int rowW = panelW - 32;
        int rowX = panelX + 16;

        if (modName.equalsIgnoreCase("FPS")) {
            // Row 1: Appearance Style Tabs
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("style_label"));
            renderStyleSelector(context, rowX + rowW - 206, rowY + 6, mouseX, mouseY, FpsModule.getStyle().ordinal());

            // Row 2: Show Background
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("bg_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, FpsModule.isShowBackground());

            // Row 3: Text Shadow
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("shadow_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, FpsModule.isTextShadow());

            // Row 4: Show Prefix 'FPS:'
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("prefix_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, FpsModule.isShowPrefix());

        } else if (modName.equalsIgnoreCase("Ping")) {
            // Row 1: Appearance Style Tabs
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("style_label"));
            renderStyleSelector(context, rowX + rowW - 206, rowY + 6, mouseX, mouseY, com.mooclient.module.modules.PingModule.getStyle().ordinal());

            // Row 2: Show Background
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("bg_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.PingModule.isShowBackground());

            // Row 3: Text Shadow
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("shadow_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.PingModule.isTextShadow());

            // Row 4: Show Prefix 'Ping:'
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("prefix_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.PingModule.isShowPrefix());

        } else if (modName.equalsIgnoreCase("Sprint")) {
            // Row 1: Interactive Keybind Selector (Click to change keybind!)
            drawOptionRow(context, rowX, rowY, rowW, rowH, "Klawisz (Keybind)");
            String keyText = listeningForKeybind ? "> WCIŚNIJ KLAWISZ <" : "[ " + ToggleSprintModule.getKeyName() + " ]";
            int btnW = 140;
            int btnH = 22;
            int btnX = rowX + rowW - btnW - 10;
            int btnY = rowY + 6;
            boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            int btnBg = listeningForKeybind ? 0xEE334466 : (btnHover ? 0xCC252535 : 0x88181824);
            int btnBorder = listeningForKeybind ? 0xFF55FFFF : (btnHover ? 0xAAFFFFFF : 0x44FFFFFF);
            int textColor = listeningForKeybind ? 0xFFFFFF55 : 0xFF55FFFF;

            context.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
            drawBorder(context, btnX, btnY, btnW, btnH, btnBorder);
            drawCenteredText(context, keyText, btnX + btnW / 2, btnY + 7, textColor);

            // Row 2: Sprint Style Tabs
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("style_label"));
            renderStyleSelector(context, rowX + rowW - 206, rowY + 6, mouseX, mouseY, ToggleSprintModule.getStyle().ordinal());

            // Row 3: Show Background
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("bg_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, ToggleSprintModule.isShowBackground());

            // Row 4: Text Shadow
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("shadow_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, ToggleSprintModule.isTextShadow());

        } else if (modName.equalsIgnoreCase("Freelook")) {
            // Row 1: Keybind
            drawOptionRow(context, rowX, rowY, rowW, rowH, "Klawisz (Keybind)");
            String keyText = listeningForKeybind ? "> WCIŚNIJ KLAWISZ <" : "[ " + FreelookModule.getKeyName() + " ]";
            int btnW = 140;
            int btnH = 22;
            int btnX = rowX + rowW - btnW - 10;
            int btnY = rowY + 6;
            boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            int btnBg = listeningForKeybind ? 0xEE334466 : (btnHover ? 0xCC252535 : 0x88181824);
            int btnBorder = listeningForKeybind ? 0xFF55FFFF : (btnHover ? 0xAAFFFFFF : 0x44FFFFFF);
            int textColor = listeningForKeybind ? 0xFFFFFF55 : 0xFF55FFFF;

            context.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
            drawBorder(context, btnX, btnY, btnW, btnH, btnBorder);
            drawCenteredText(context, keyText, btnX + btnW / 2, btnY + 7, textColor);

            // Row 2: Activation Mode (Hold vs Toggle)
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("mode_label"));
            renderModeSelector(context, rowX + rowW - 206, rowY + 6, mouseX, mouseY, FreelookModule.getMode().ordinal());

            // Row 3: Invert Pitch
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("invert_pitch_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, FreelookModule.isInvertPitch());

        } else if (modName.equalsIgnoreCase("Potion Effects")) {
            // Row 1: Style Selector (Moo Client / Simple / Compact)
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("style_label"));
            renderPotionStyleSelector(context, rowX + rowW - 248, rowY + 6, mouseX, mouseY, PotionEffectsModule.getStyle().ordinal());

            // Row 2: Show Background
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("bg_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, PotionEffectsModule.isShowBackground());

            // Row 3: Text Shadow
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("shadow_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, PotionEffectsModule.isTextShadow());

        } else if (modName.equalsIgnoreCase("Nametags")) {
            // Row 1: Show Ping
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("show_ping_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.NametagsModule.isShowPing());

            // Row 2: Ping Position (Beside vs Above)
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("ping_pos_label"));
            renderPingPositionSelector(context, rowX + rowW - 206, rowY + 6, mouseX, mouseY, com.mooclient.module.modules.NametagsModule.getPingPosition().ordinal());

            // Row 3: Remove Background
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("remove_bg_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.NametagsModule.isRemoveBackground());

            // Row 4: Text Shadow
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("shadow_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.NametagsModule.isTextShadow());

        } else if (modName.equalsIgnoreCase("Zoom")) {
            // Row 1: Keybind
            drawOptionRow(context, rowX, rowY, rowW, rowH, "Klawisz przybliżenia (Key)");
            String btnText = (this.listeningForKeybind && selectedModule.getName().equalsIgnoreCase("Zoom")) ? "Naciśnij klawisz..." : "[" + com.mooclient.module.modules.ZoomModule.getKeyName() + "]";
            int btnW = 140;
            int btnH = 22;
            int btnX = rowX + rowW - btnW - 10;
            int btnY = rowY + 6;
            boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
            context.fill(btnX, btnY, btnX + btnW, btnY + btnH, this.listeningForKeybind ? 0xDD22C55E : (btnHover ? 0xCC252535 : 0x66141420));
            drawBorder(context, btnX, btnY, btnW, btnH, this.listeningForKeybind ? 0xFF4ADE80 : (btnHover ? 0xAAFFFFFF : 0x33FFFFFF));
            drawCenteredText(context, btnText, btnX + btnW / 2, btnY + 7, this.listeningForKeybind ? 0xFF0A2514 : (btnHover ? COLOR_TEXT_WHITE : 0xFFA0A0AB));

            // Row 2: Zoom Factor (2x, 3x, 4x, 5x, 6x)
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("factor_label"));
            renderFactorSelector(context, rowX + rowW - 248, rowY + 6, mouseX, mouseY, com.mooclient.module.modules.ZoomModule.getFactor().ordinal());

            // Row 3: Activation Mode (Hold vs Toggle)
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("mode_label"));
            renderModeSelector(context, rowX + rowW - 206, rowY + 6, mouseX, mouseY, com.mooclient.module.modules.ZoomModule.getMode().ordinal());

            // Row 4: Smooth Zoom
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("smooth_zoom_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.ZoomModule.isSmoothZoom());

        } else if (modName.equalsIgnoreCase("Chat")) {
            // Row 1: Transparent Background
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("chat_transparent_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.ChatModule.isTransparentBackground());

            // Row 2: Unlimited Chat
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("chat_unlimited_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.ChatModule.isUnlimitedChat());

            // Row 3: Smooth Chat Animation
            rowY += rowH + 6;
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("chat_smooth_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, com.mooclient.module.modules.ChatModule.isSmoothChat());

        } else if (modName.equalsIgnoreCase("Macro")) {
            java.util.List<com.mooclient.module.modules.MacroModule.MacroEntry> macroList = com.mooclient.module.modules.MacroModule.getMacros();
            int mRowH = 28;
            int curY = panelY + headerH + 10;

            for (int i = 0; i < Math.min(5, macroList.size()); i++) {
                com.mooclient.module.modules.MacroModule.MacroEntry m = macroList.get(i);
                context.fill(rowX, curY, rowX + rowW, curY + mRowH, 0x5515151E);
                drawBorder(context, rowX, curY, rowW, mRowH, 0x22FFFFFF);

                // Slot title
                String slotName = "Slot " + (i + 1);
                context.drawTextWithShadow(this.textRenderer, slotName, rowX + 8, curY + (mRowH - 8) / 2, m.isEnabled() ? COLOR_TEXT_WHITE : COLOR_TEXT_MUTED);

                // Command Box (Editable)
                int cmdBoxX = rowX + 54;
                int cmdBoxW = rowW - 54 - 110 - 44;
                int cmdBoxY = curY + 4;
                int cmdBoxH = mRowH - 8;
                boolean isEditingCmd = (this.editingMacroIndex == i);
                boolean cmdHover = mouseX >= cmdBoxX && mouseX <= cmdBoxX + cmdBoxW && mouseY >= cmdBoxY && mouseY <= cmdBoxY + cmdBoxH;

                context.fill(cmdBoxX, cmdBoxY, cmdBoxX + cmdBoxW, cmdBoxY + cmdBoxH, isEditingCmd ? 0xEE1E293B : (cmdHover ? 0xCC252535 : 0x88181824));
                drawBorder(context, cmdBoxX, cmdBoxY, cmdBoxW, cmdBoxH, isEditingCmd ? 0xFF38BDF8 : (cmdHover ? 0xAAFFFFFF : 0x33FFFFFF));

                String cmdDisplay = m.getCommand().isEmpty() ? "(kliknij by wpisać)" : m.getCommand();
                if (isEditingCmd) {
                    cmdDisplay = "> " + m.getCommand() + (System.currentTimeMillis() % 1000 > 500 ? "_" : "");
                }
                if (this.textRenderer.getWidth(cmdDisplay) > cmdBoxW - 8) {
                    cmdDisplay = this.textRenderer.trimToWidth(cmdDisplay, cmdBoxW - 14) + "..";
                }
                context.drawTextWithShadow(this.textRenderer, cmdDisplay, cmdBoxX + 6, cmdBoxY + (cmdBoxH - 8) / 2, isEditingCmd ? 0xFF38BDF8 : (m.isEnabled() ? 0xFF55FFFF : COLOR_TEXT_MUTED));

                // Keybind Button
                int kBtnX = cmdBoxX + cmdBoxW + 6;
                int kBtnW = 96;
                int kBtnY = curY + 4;
                int kBtnH = mRowH - 8;
                boolean isListeningKey = (this.listeningMacroIndex == i);
                boolean kBtnHover = mouseX >= kBtnX && mouseX <= kBtnX + kBtnW && mouseY >= kBtnY && mouseY <= kBtnY + kBtnH;

                context.fill(kBtnX, kBtnY, kBtnX + kBtnW, kBtnY + kBtnH, isListeningKey ? 0xEE334466 : (kBtnHover ? 0xCC252535 : 0x88181824));
                drawBorder(context, kBtnX, kBtnY, kBtnW, kBtnH, isListeningKey ? 0xFF55FFFF : (kBtnHover ? 0xAAFFFFFF : 0x33FFFFFF));

                String kText = isListeningKey ? "> KLAWISZ <" : "[ " + m.getKeyName() + " ]";
                drawCenteredText(context, kText, kBtnX + kBtnW / 2, kBtnY + (kBtnH - 8) / 2, isListeningKey ? 0xFFFFFF55 : (m.isEnabled() ? 0xFF55FFFF : COLOR_TEXT_MUTED));

                // Enable / Disable toggle
                int tX = rowX + rowW - 40;
                int tY = curY + 5;
                drawOptionToggle(context, tX, tY, mouseX, mouseY, m.isEnabled());

                curY += mRowH + 4;
            }

        } else {
            // Gamma Options
            drawOptionRow(context, rowX, rowY, rowW, rowH, MooLanguage.get("fullbright_label"));
            drawOptionToggle(context, rowX + rowW - 44, rowY + 8, mouseX, mouseY, selectedModule != null && selectedModule.isEnabled());
        }

        String hint = MooLanguage.get("esc_hint");
        drawCenteredText(context, hint, this.width / 2, this.height - 20, 0x66FFFFFF);
    }

    private void drawOptionRow(DrawContext context, int x, int y, int w, int h, String title) {
        context.fill(x, y, x + w, y + h, 0x5515151E);
        drawBorder(context, x, y, w, h, 0x22FFFFFF);
        context.drawTextWithShadow(this.textRenderer, title, x + 12, y + (h - 8) / 2, COLOR_TEXT_WHITE);
    }

    private void drawOptionToggle(DrawContext context, int x, int y, int mouseX, int mouseY, boolean enabled) {
        int w = 34;
        int h = 18;
        int bg = enabled ? COLOR_ENABLED : COLOR_DISABLED;
        context.fill(x, y, x + w, y + h, bg);
        drawBorder(context, x, y, w, h, 0x44FFFFFF);

        int knobSize = h - 4;
        int knobX = enabled ? x + w - knobSize - 2 : x + 2;
        int knobY = y + 2;
        int knobColor = enabled ? 0xFF082212 : 0xFFA0A0AB;
        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knobColor);
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void renderStyleSelector(DrawContext context, int startX, int y, int mouseX, int mouseY, int selectedOrdinal) {
        String[] labels = new String[]{"Moo Client", "Simple", "Brackets"};
        int[] widths = new int[]{74, 54, 64};
        int gap = 4;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < labels.length; i++) {
            int w = widths[i];
            boolean selected = (i == selectedOrdinal);
            boolean hover = mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h;

            int bg = selected ? 0xDD22C55E : (hover ? 0xCC252535 : 0x66141420);
            int border = selected ? 0xFF4ADE80 : (hover ? 0xAAFFFFFF : 0x33FFFFFF);
            int textColor = selected ? 0xFF0A2514 : (hover ? COLOR_TEXT_WHITE : 0xFFA0A0AB);

            context.fill(curX, y, curX + w, y + h, bg);
            drawBorder(context, curX, y, w, h, border);
            drawCenteredText(context, labels[i], curX + w / 2, y + 7, textColor);

            curX += w + gap;
        }
    }

    private int getStyleSelectorClick(int startX, int y, int mouseX, int mouseY) {
        int[] widths = new int[]{74, 54, 64};
        int gap = 4;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            if (mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h) {
                return i;
            }
            curX += w + gap;
        }
        return -1;
    }

    private void renderModeSelector(DrawContext context, int startX, int y, int mouseX, int mouseY, int selectedOrdinal) {
        String[] labels = new String[]{"Hold", "Toggle"};
        int[] widths = new int[]{100, 100};
        int gap = 6;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < labels.length; i++) {
            int w = widths[i];
            boolean selected = (i == selectedOrdinal);
            boolean hover = mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h;

            int bg = selected ? 0xDD22C55E : (hover ? 0xCC252535 : 0x66141420);
            int border = selected ? 0xFF4ADE80 : (hover ? 0xAAFFFFFF : 0x33FFFFFF);
            int textColor = selected ? 0xFF0A2514 : (hover ? COLOR_TEXT_WHITE : 0xFFA0A0AB);

            context.fill(curX, y, curX + w, y + h, bg);
            drawBorder(context, curX, y, w, h, border);
            drawCenteredText(context, labels[i], curX + w / 2, y + 7, textColor);

            curX += w + gap;
        }
    }

    private void renderPingPositionSelector(DrawContext context, int startX, int y, int mouseX, int mouseY, int selectedOrdinal) {
        String[] labels = new String[]{MooLanguage.get("ping_pos_beside"), MooLanguage.get("ping_pos_above")};
        int[] widths = new int[]{100, 100};
        int gap = 6;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < labels.length; i++) {
            int w = widths[i];
            boolean selected = (i == selectedOrdinal);
            boolean hover = mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h;

            int bg = selected ? 0xDD22C55E : (hover ? 0xCC252535 : 0x66141420);
            int border = selected ? 0xFF4ADE80 : (hover ? 0xAAFFFFFF : 0x33FFFFFF);
            int textColor = selected ? 0xFF0A2514 : (hover ? COLOR_TEXT_WHITE : 0xFFA0A0AB);

            context.fill(curX, y, curX + w, y + h, bg);
            drawBorder(context, curX, y, w, h, border);
            drawCenteredText(context, labels[i], curX + w / 2, y + 7, textColor);

            curX += w + gap;
        }
    }

    private void renderPotionStyleSelector(DrawContext context, int startX, int y, int mouseX, int mouseY, int selectedOrdinal) {
        String[] labels = new String[]{"Moo Client", "Simple", "Compact"};
        int[] widths = new int[]{90, 75, 75};
        int gap = 4;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < labels.length; i++) {
            int w = widths[i];
            boolean selected = (i == selectedOrdinal);
            boolean hover = mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h;

            int bg = selected ? 0xDD22C55E : (hover ? 0xCC252535 : 0x66141420);
            int border = selected ? 0xFF4ADE80 : (hover ? 0xAAFFFFFF : 0x33FFFFFF);
            int textColor = selected ? 0xFF0A2514 : (hover ? COLOR_TEXT_WHITE : 0xFFA0A0AB);

            context.fill(curX, y, curX + w, y + h, bg);
            drawBorder(context, curX, y, w, h, border);
            drawCenteredText(context, labels[i], curX + w / 2, y + 7, textColor);

            curX += w + gap;
        }
    }

    private int getPotionStyleClick(int startX, int y, int mouseX, int mouseY) {
        int[] widths = new int[]{90, 75, 75};
        int gap = 4;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            if (mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h) {
                return i;
            }
            curX += w + gap;
        }
        return -1;
    }

    private void renderFactorSelector(DrawContext context, int startX, int y, int mouseX, int mouseY, int selectedOrdinal) {
        String[] labels = new String[]{"2x", "3x", "4x", "5x", "6x"};
        int w = 44;
        int gap = 4;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < labels.length; i++) {
            boolean selected = (i == selectedOrdinal);
            boolean hover = mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h;

            int bg = selected ? 0xDD22C55E : (hover ? 0xCC252535 : 0x66141420);
            int border = selected ? 0xFF4ADE80 : (hover ? 0xAAFFFFFF : 0x33FFFFFF);
            int textColor = selected ? 0xFF0A2514 : (hover ? COLOR_TEXT_WHITE : 0xFFA0A0AB);

            context.fill(curX, y, curX + w, y + h, bg);
            drawBorder(context, curX, y, w, h, border);
            drawCenteredText(context, labels[i], curX + w / 2, y + 7, textColor);

            curX += w + gap;
        }
    }

    private int getFactorSelectorClick(int startX, int y, int mouseX, int mouseY) {
        int w = 44;
        int gap = 4;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < 5; i++) {
            if (mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h) {
                return i;
            }
            curX += w + gap;
        }
        return -1;
    }

    private int getModeSelectorClick(int startX, int y, int mouseX, int mouseY) {
        int[] widths = new int[]{100, 100};
        int gap = 6;
        int curX = startX;
        int h = 22;

        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            if (mouseX >= curX && mouseX <= curX + w && mouseY >= y && mouseY <= y + h) {
                return i;
            }
            curX += w + gap;
        }
        return -1;
    }

    private void drawCenteredText(DrawContext context, String text, int centerX, int y, int color) {
        int width = this.textRenderer.getWidth(text);
        context.drawTextWithShadow(this.textRenderer, text, centerX - width / 2, y, color);
    }

    private void playClickSound() {
        if (this.client != null) {
            this.client.getSoundManager().play(
                net.minecraft.client.sound.PositionedSoundInstance.master(
                    net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f
                )
            );
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentView == View.MODS) {
            List<Module> modules = ModuleManager.getInstance().getModules();
            int cols = 3;
            int cardH = 150;
            int cardGap = 16;
            int headerH = 42;
            int panelH = 260;
            int totalRows = (modules.size() + cols - 1) / cols;
            int totalContentH = totalRows * cardH + (totalRows - 1) * cardGap;
            int visibleAreaH = panelH - headerH - 24;
            int maxScroll = Math.max(0, totalContentH - visibleAreaH + 8);

            if (maxScroll > 0) {
                scrollY = Math.max(0, Math.min(maxScroll, scrollY - verticalAmount * 24.0));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // If listening for new keybind/mouse bind in options
        if (currentView == View.OPTIONS && listeningForKeybind) {
            String mouseName;
            if (button == 0) {
                mouseName = "LMB";
            } else if (button == 1) {
                mouseName = "RMB";
            } else if (button == 2) {
                mouseName = "SCROLL";
            } else if (button == 3) {
                mouseName = "MOUSE 4";
            } else if (button == 4) {
                mouseName = "MOUSE 5";
            } else {
                mouseName = "BUTTON " + (button + 1);
            }

            if (selectedModule != null && selectedModule.getName().equalsIgnoreCase("Freelook")) {
                FreelookModule.setKeybind(button, mouseName);
            } else if (selectedModule != null && selectedModule.getName().equalsIgnoreCase("Zoom")) {
                com.mooclient.module.modules.ZoomModule.setKeybind(button, mouseName, true);
            } else {
                ToggleSprintModule.setKeybind(button, mouseName);
            }

            listeningForKeybind = false;
            playClickSound();
            return true;
        }

        if (button == 0) { // Left click
            // 1. Language Switcher Click
            int langX = this.width - 66;
            int langY = 12;
            int pillW = 26;
            int pillH = 18;
            int langGap = 2;

            if (mouseX >= langX && mouseX <= langX + pillW && mouseY >= langY && mouseY <= langY + pillH) {
                playClickSound();
                MooLanguage.current = MooLanguage.PL;
                return true;
            }
            if (mouseX >= langX + pillW + langGap && mouseX <= langX + (pillW * 2) + langGap && mouseY >= langY && mouseY <= langY + pillH) {
                playClickSound();
                MooLanguage.current = MooLanguage.EN;
                return true;
            }

            // 2. Hub View Clicks & Draggable HUD Handling
            if (currentView == View.HUB) {
                if (FpsModule.isFpsEnabled()) {
                    int x = FpsModule.posX;
                    int y = FpsModule.posY;
                    int w = FpsModule.width;
                    int h = FpsModule.height;
                    if (mouseX >= x - 4 && mouseX <= x + w + 4 && mouseY >= y - 4 && mouseY <= y + h + 4) {
                        draggingWidget = "FPS";
                        dragOffsetX = (int) mouseX - x;
                        dragOffsetY = (int) mouseY - y;
                        return true;
                    }
                }

                if (ToggleSprintModule.isSprintEnabled()) {
                    int x = ToggleSprintModule.posX;
                    int y = ToggleSprintModule.posY;
                    int w = ToggleSprintModule.width;
                    int h = ToggleSprintModule.height;
                    if (mouseX >= x - 4 && mouseX <= x + w + 4 && mouseY >= y - 4 && mouseY <= y + h + 4) {
                        draggingWidget = "SPRINT";
                        dragOffsetX = (int) mouseX - x;
                        dragOffsetY = (int) mouseY - y;
                        return true;
                    }
                }

                if (PotionEffectsModule.isModuleEnabled()) {
                    int x = PotionEffectsModule.posX;
                    int y = PotionEffectsModule.posY;
                    int w = PotionEffectsModule.width;
                    int h = PotionEffectsModule.height;
                    if (mouseX >= x - 4 && mouseX <= x + w + 4 && mouseY >= y - 4 && mouseY <= y + h + 4) {
                        draggingWidget = "POTIONS";
                        dragOffsetX = (int) mouseX - x;
                        dragOffsetY = (int) mouseY - y;
                        return true;
                    }
                }

                if (com.mooclient.module.modules.PingModule.isPingEnabled()) {
                    int x = com.mooclient.module.modules.PingModule.posX;
                    int y = com.mooclient.module.modules.PingModule.posY;
                    int w = com.mooclient.module.modules.PingModule.width;
                    int h = com.mooclient.module.modules.PingModule.height;
                    if (mouseX >= x - 4 && mouseX <= x + w + 4 && mouseY >= y - 4 && mouseY <= y + h + 4) {
                        draggingWidget = "PING";
                        dragOffsetX = (int) mouseX - x;
                        dragOffsetY = (int) mouseY - y;
                        return true;
                    }
                }

                int centerX = this.width / 2;
                int centerY = this.height / 2;
                int btnW = 140;
                int btnH = 32;
                int btnX = centerX - btnW / 2;
                int btnY = centerY - btnH / 2;

                if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                    playClickSound();
                    this.currentView = View.MODS;
                    return true;
                }
            }
            // 3. Mods View Clicks
            else if (currentView == View.MODS) {
                int panelW = 560;
                int panelH = 260;
                int panelX = (this.width - panelW) / 2;
                int panelY = (this.height - panelH) / 2;

                // Back Button Click
                int backX = panelX + 14;
                int backY = panelY + 12;
                int backW = 74;
                int backH = 22;
                if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
                    playClickSound();
                    this.currentView = View.HUB;
                    return true;
                }

                List<Module> modules = ModuleManager.getInstance().getModules();
                int cols = 3;
                int cardW = 160;
                int cardH = 150;
                int cardGap = 16;
                int totalGridW = cols * cardW + (cols - 1) * cardGap;
                int startX = panelX + (panelW - totalGridW) / 2;
                int startY = panelY + 42 + 16;

                for (int i = 0; i < modules.size(); i++) {
                    Module module = modules.get(i);
                    int col = i % cols;
                    int row = i / cols;
                    int cardX = startX + col * (cardW + cardGap);
                    int cardY = startY + row * (cardH + cardGap) - (int) scrollY;

                    // Ensure click is inside visible area
                    if (mouseY < panelY + 42 + 2 || mouseY > panelY + panelH - 4) {
                        continue;
                    }

                    // ENABLED / DISABLED Button Click
                    int btnH = 22;
                    int btnY = cardY + cardH - 28;
                    int btnX = cardX + 8;
                    int btnW = cardW - 16;
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        playClickSound();
                        module.toggle();
                        return true;
                    }

                    // OPTIONS Bar Click OR Card Body Click
                    int optH = 20;
                    int optY = cardY + cardH - 52;
                    int optX = cardX + 8;
                    int optW = cardW - 16;
                    boolean optClicked = mouseX >= optX && mouseX <= optX + optW && mouseY >= optY && mouseY <= optY + optH;
                    boolean cardBodyClicked = mouseX >= cardX && mouseX <= cardX + cardW && mouseY >= cardY && mouseY <= optY;

                    if (optClicked || cardBodyClicked) {
                        playClickSound();
                        this.selectedModule = module;
                        this.listeningForKeybind = false;
                        this.currentView = View.OPTIONS;
                        return true;
                    }
                }
            }
            // 4. Options View Clicks
            else if (currentView == View.OPTIONS) {
                int panelW = 480;
                int panelH = 270;
                int panelX = (this.width - panelW) / 2;
                int panelY = (this.height - panelH) / 2;

                int backX = panelX + 14;
                int backY = panelY + 12;
                int backW = 74;
                int backH = 22;
                if (mouseX >= backX && mouseX <= backX + backW && mouseY >= backY && mouseY <= backY + backH) {
                    playClickSound();
                    this.listeningForKeybind = false;
                    this.currentView = View.MODS;
                    return true;
                }

                String modName = selectedModule != null ? selectedModule.getName() : "FPS";
                int headerH = 46;
                int rowY = panelY + headerH + 14;
                int rowH = 34;
                int rowW = panelW - 32;
                int rowX = panelX + 16;

                if (modName.equalsIgnoreCase("FPS")) {
                    int styleClick = getStyleSelectorClick(rowX + rowW - 206, rowY + 6, (int) mouseX, (int) mouseY);
                    if (styleClick >= 0) {
                        playClickSound();
                        FpsModule.setStyle(FpsModule.FpsStyle.values()[styleClick]);
                        return true;
                    }

                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        FpsModule.toggleShowBackground();
                        return true;
                    }

                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        FpsModule.toggleTextShadow();
                        return true;
                    }

                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        FpsModule.toggleShowPrefix();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Ping")) {
                    int styleClick = getStyleSelectorClick(rowX + rowW - 206, rowY + 6, (int) mouseX, (int) mouseY);
                    if (styleClick >= 0) {
                        playClickSound();
                        com.mooclient.module.modules.PingModule.setStyle(com.mooclient.module.modules.PingModule.PingStyle.values()[styleClick]);
                        return true;
                    }

                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.PingModule.toggleShowBackground();
                        return true;
                    }

                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.PingModule.toggleTextShadow();
                        return true;
                    }

                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.PingModule.toggleShowPrefix();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Sprint")) {
                    int btnW = 140;
                    int btnH = 22;
                    int btnX = rowX + rowW - btnW - 10;
                    int btnY = rowY + 6;

                    // Row 1: Click to toggle interactive Keybind listening mode
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        playClickSound();
                        this.listeningForKeybind = !this.listeningForKeybind;
                        return true;
                    }

                    // Row 2: Sprint Style Tab Selection
                    rowY += rowH + 6;
                    int styleClick = getStyleSelectorClick(rowX + rowW - 206, rowY + 6, (int) mouseX, (int) mouseY);
                    if (styleClick >= 0) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        ToggleSprintModule.setStyle(ToggleSprintModule.SprintStyle.values()[styleClick]);
                        return true;
                    }

                    // Row 3: Show Background
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        ToggleSprintModule.toggleShowBackground();
                        return true;
                    }

                    // Row 4: Text Shadow
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        ToggleSprintModule.toggleTextShadow();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Freelook")) {
                    int btnW = 140;
                    int btnH = 22;
                    int btnX = rowX + rowW - btnW - 10;
                    int btnY = rowY + 6;

                    // Row 1: Keybind
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        playClickSound();
                        this.listeningForKeybind = !this.listeningForKeybind;
                        return true;
                    }

                    // Row 2: Mode Selector
                    rowY += rowH + 6;
                    int modeClick = getModeSelectorClick(rowX + rowW - 206, rowY + 6, (int) mouseX, (int) mouseY);
                    if (modeClick >= 0) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        FreelookModule.setMode(FreelookModule.ActivationMode.values()[modeClick]);
                        return true;
                    }

                    // Row 3: Invert Pitch Toggle
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        FreelookModule.toggleInvertPitch();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Potion Effects")) {
                    // Row 1: Style Selector
                    int styleClick = getPotionStyleClick(rowX + rowW - 248, rowY + 6, (int) mouseX, (int) mouseY);
                    if (styleClick >= 0) {
                        playClickSound();
                        PotionEffectsModule.setStyle(PotionEffectsModule.PotionStyle.values()[styleClick]);
                        return true;
                    }

                    // Row 2: Show Background
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        PotionEffectsModule.toggleShowBackground();
                        return true;
                    }

                    // Row 3: Text Shadow
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        PotionEffectsModule.toggleTextShadow();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Nametags")) {
                    // Row 1: Show Ping
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.NametagsModule.toggleShowPing();
                        return true;
                    }

                    // Row 2: Ping Position (Beside vs Above)
                    rowY += rowH + 6;
                    int pingPosClick = getModeSelectorClick(rowX + rowW - 206, rowY + 6, (int) mouseX, (int) mouseY);
                    if (pingPosClick >= 0) {
                        playClickSound();
                        com.mooclient.module.modules.NametagsModule.setPingPosition(com.mooclient.module.modules.NametagsModule.PingPosition.values()[pingPosClick]);
                        return true;
                    }

                    // Row 3: Remove Background
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.NametagsModule.toggleRemoveBackground();
                        return true;
                    }

                    // Row 4: Text Shadow
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.NametagsModule.toggleTextShadow();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Zoom")) {
                    int btnW = 140;
                    int btnH = 22;
                    int btnX = rowX + rowW - btnW - 10;
                    int btnY = rowY + 6;

                    // Row 1: Keybind
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        playClickSound();
                        this.listeningForKeybind = !this.listeningForKeybind;
                        return true;
                    }

                    // Row 2: Zoom Factor (2x, 3x, 4x, 5x, 6x)
                    rowY += rowH + 6;
                    int factorClick = getFactorSelectorClick(rowX + rowW - 248, rowY + 6, (int) mouseX, (int) mouseY);
                    if (factorClick >= 0) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        com.mooclient.module.modules.ZoomModule.setFactor(com.mooclient.module.modules.ZoomModule.ZoomFactor.values()[factorClick]);
                        return true;
                    }

                    // Row 3: Activation Mode (Hold vs Toggle)
                    rowY += rowH + 6;
                    int modeClick = getModeSelectorClick(rowX + rowW - 206, rowY + 6, (int) mouseX, (int) mouseY);
                    if (modeClick >= 0) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        com.mooclient.module.modules.ZoomModule.setMode(com.mooclient.module.modules.ZoomModule.ActivationMode.values()[modeClick]);
                        return true;
                    }

                    // Row 4: Smooth Zoom Toggle
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        this.listeningForKeybind = false;
                        com.mooclient.module.modules.ZoomModule.toggleSmoothZoom();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Chat")) {
                    // Row 1: Transparent Background
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.ChatModule.toggleTransparentBackground();
                        com.mooclient.util.MooConfig.save();
                        return true;
                    }

                    // Row 2: Unlimited Chat
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.ChatModule.toggleUnlimitedChat();
                        com.mooclient.util.MooConfig.save();
                        return true;
                    }

                    // Row 3: Smooth Chat
                    rowY += rowH + 6;
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        com.mooclient.module.modules.ChatModule.toggleSmoothChat();
                        com.mooclient.util.MooConfig.save();
                        return true;
                    }
                } else if (modName.equalsIgnoreCase("Macro")) {
                    java.util.List<com.mooclient.module.modules.MacroModule.MacroEntry> macroList = com.mooclient.module.modules.MacroModule.getMacros();
                    int mRowH = 28;
                    int curY = panelY + headerH + 10;

                    // If listening for mouse button keybind
                    if (button != 0 && this.listeningMacroIndex >= 0 && this.listeningMacroIndex < macroList.size()) {
                        com.mooclient.module.modules.MacroModule.MacroEntry m = macroList.get(this.listeningMacroIndex);
                        m.setKeyCode(button);
                        m.setKeyName(button == 2 ? "SCROLL" : (button == 1 ? "RMB" : "MOUSE " + (button + 1)));
                        m.setMouseButton(true);
                        this.listeningMacroIndex = -1;
                        com.mooclient.util.MooConfig.save();
                        playClickSound();
                        return true;
                    }

                    for (int i = 0; i < Math.min(5, macroList.size()); i++) {
                        com.mooclient.module.modules.MacroModule.MacroEntry m = macroList.get(i);
                        int cmdBoxX = rowX + 54;
                        int cmdBoxW = rowW - 54 - 110 - 44;
                        int cmdBoxY = curY + 4;
                        int cmdBoxH = mRowH - 8;

                        int kBtnX = cmdBoxX + cmdBoxW + 6;
                        int kBtnW = 96;
                        int kBtnY = curY + 4;
                        int kBtnH = mRowH - 8;

                        int tX = rowX + rowW - 40;
                        int tY = curY + 5;

                        // Click Command Box
                        if (mouseX >= cmdBoxX && mouseX <= cmdBoxX + cmdBoxW && mouseY >= cmdBoxY && mouseY <= cmdBoxY + cmdBoxH) {
                            playClickSound();
                            this.editingMacroIndex = (this.editingMacroIndex == i ? -1 : i);
                            this.listeningMacroIndex = -1;
                            return true;
                        }

                        // Click Keybind Button
                        if (mouseX >= kBtnX && mouseX <= kBtnX + kBtnW && mouseY >= kBtnY && mouseY <= kBtnY + kBtnH) {
                            playClickSound();
                            this.listeningMacroIndex = (this.listeningMacroIndex == i ? -1 : i);
                            this.editingMacroIndex = -1;
                            return true;
                        }

                        // Click Enable Toggle
                        if (mouseX >= tX && mouseX <= tX + 34 && mouseY >= tY && mouseY <= tY + 18) {
                            playClickSound();
                            m.setEnabled(!m.isEnabled());
                            com.mooclient.util.MooConfig.save();
                            return true;
                        }

                        curY += mRowH + 4;
                    }
                } else {
                    if (mouseX >= rowX + rowW - 44 && mouseX <= rowX + rowW - 10 && mouseY >= rowY + 8 && mouseY <= rowY + 26) {
                        playClickSound();
                        if (selectedModule != null) selectedModule.toggle();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (currentView == View.OPTIONS && editingMacroIndex >= 0) {
            java.util.List<com.mooclient.module.modules.MacroModule.MacroEntry> macroList = com.mooclient.module.modules.MacroModule.getMacros();
            if (editingMacroIndex < macroList.size()) {
                com.mooclient.module.modules.MacroModule.MacroEntry m = macroList.get(editingMacroIndex);
                if (chr >= 32 && chr != 127) { // printable character
                    m.setCommand(m.getCommand() + chr);
                    com.mooclient.util.MooConfig.save();
                    return true;
                }
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (currentView == View.HUB && draggingWidget != null) {
            if ("FPS".equals(draggingWidget)) {
                FpsModule.posX = Math.max(2, Math.min(this.width - FpsModule.width - 2, (int) mouseX - dragOffsetX));
                FpsModule.posY = Math.max(2, Math.min(this.height - FpsModule.height - 2, (int) mouseY - dragOffsetY));
                return true;
            } else if ("SPRINT".equals(draggingWidget)) {
                ToggleSprintModule.posX = Math.max(2, Math.min(this.width - ToggleSprintModule.width - 2, (int) mouseX - dragOffsetX));
                ToggleSprintModule.posY = Math.max(2, Math.min(this.height - ToggleSprintModule.height - 2, (int) mouseY - dragOffsetY));
                return true;
            } else if ("POTIONS".equals(draggingWidget)) {
                PotionEffectsModule.posX = Math.max(2, Math.min(this.width - PotionEffectsModule.width - 2, (int) mouseX - dragOffsetX));
                PotionEffectsModule.posY = Math.max(2, Math.min(this.height - PotionEffectsModule.height - 2, (int) mouseY - dragOffsetY));
                return true;
            } else if ("PING".equals(draggingWidget)) {
                com.mooclient.module.modules.PingModule.posX = Math.max(2, Math.min(this.width - com.mooclient.module.modules.PingModule.width - 2, (int) mouseX - dragOffsetX));
                com.mooclient.module.modules.PingModule.posY = Math.max(2, Math.min(this.height - com.mooclient.module.modules.PingModule.height - 2, (int) mouseY - dragOffsetY));
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingWidget = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If editing macro command text
        if (currentView == View.OPTIONS && editingMacroIndex >= 0) {
            java.util.List<com.mooclient.module.modules.MacroModule.MacroEntry> macroList = com.mooclient.module.modules.MacroModule.getMacros();
            if (editingMacroIndex < macroList.size()) {
                com.mooclient.module.modules.MacroModule.MacroEntry m = macroList.get(editingMacroIndex);
                if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
                    String cmd = m.getCommand();
                    if (cmd != null && !cmd.isEmpty()) {
                        m.setCommand(cmd.substring(0, cmd.length() - 1));
                        com.mooclient.util.MooConfig.save();
                    }
                    return true;
                } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                    editingMacroIndex = -1;
                    com.mooclient.util.MooConfig.save();
                    playClickSound();
                    return true;
                }
            }
        }

        // If listening for macro keybind
        if (currentView == View.OPTIONS && listeningMacroIndex >= 0) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                listeningMacroIndex = -1;
                return true;
            }

            java.util.List<com.mooclient.module.modules.MacroModule.MacroEntry> macroList = com.mooclient.module.modules.MacroModule.getMacros();
            if (listeningMacroIndex < macroList.size()) {
                com.mooclient.module.modules.MacroModule.MacroEntry m = macroList.get(listeningMacroIndex);
                String kName;
                try {
                    kName = net.minecraft.client.util.InputUtil.fromKeyCode(keyCode, scanCode).getLocalizedText().getString().toUpperCase();
                } catch (Exception e) {
                    kName = "KEY " + keyCode;
                }
                if (kName == null || kName.isEmpty() || kName.startsWith("KEY.")) {
                    kName = "KEY " + keyCode;
                }
                m.setKeyCode(keyCode);
                m.setKeyName(kName);
                m.setMouseButton(false);
                com.mooclient.util.MooConfig.save();
                listeningMacroIndex = -1;
                playClickSound();
                return true;
            }
        }

        // If listening for new keybind in Sprint, Freelook, Zoom options
        if (currentView == View.OPTIONS && listeningForKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningForKeybind = false;
                return true;
            }

            String keyName;
            try {
                keyName = InputUtil.fromKeyCode(keyCode, scanCode).getLocalizedText().getString().toUpperCase();
            } catch (Exception e) {
                keyName = "KEY " + keyCode;
            }

            if (keyName == null || keyName.isEmpty() || keyName.startsWith("KEY.")) {
                keyName = "KEY " + keyCode;
            }

            if (selectedModule != null && selectedModule.getName().equalsIgnoreCase("Freelook")) {
                FreelookModule.setKeybind(keyCode, keyName);
            } else if (selectedModule != null && selectedModule.getName().equalsIgnoreCase("Zoom")) {
                com.mooclient.module.modules.ZoomModule.setKeybind(keyCode, keyName);
            } else {
                ToggleSprintModule.setKeybind(keyCode, keyName);
            }

            listeningForKeybind = false;
            playClickSound();
            return true;
        }

        if (keyCode == 344 && editingMacroIndex < 0 && listeningMacroIndex < 0 && !listeningForKeybind) { // GLFW_KEY_RIGHT_SHIFT
            this.close();
            return true;
        }
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            if (currentView == View.OPTIONS) {
                this.currentView = View.MODS;
                return true;
            } else if (currentView == View.MODS) {
                this.currentView = View.HUB;
                return true;
            } else {
                this.close();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        com.mooclient.util.MooConfig.save();
        super.close();
    }
}
