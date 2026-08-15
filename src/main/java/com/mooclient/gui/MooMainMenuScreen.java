package com.mooclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom modern monochrome Main Menu for Moo Client.
 */
public class MooMainMenuScreen extends Screen {

    private static final Identifier COW_LOGO = Identifier.of("minecraft", "icons/icon_128x128.png");

    // Colors matching the launcher theme
    private static final int COLOR_BG_OVERLAY = 0xCC09090D;
    private static final int COLOR_CARD_BG = 0x80141418;
    private static final int COLOR_CARD_HOVER = 0xD0202026;
    private static final int COLOR_BORDER = 0x40FFFFFF;
    private static final int COLOR_BORDER_HOVER = 0xAAFFFFFF;
    private static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int COLOR_TEXT_MUTED = 0xFFA0A0AB;

    private static class MenuButton {
        final String label;
        final String icon;
        final Runnable action;
        int x, y, width, height;
        float hoverAnim = 0.0f;

        MenuButton(String icon, String label, Runnable action) {
            this.icon = icon;
            this.label = label;
            this.action = action;
        }

        boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private final List<MenuButton> buttons = new ArrayList<>();
    private float openAnim = 0.0f;

    public MooMainMenuScreen() {
        super(Text.literal("Moo Client Main Menu"));
    }

    @Override
    protected void init() {
        super.init();
        buttons.clear();

        buttons.add(new MenuButton("▶", "TRYB JEDNOOSOBOWY", () -> {
            if (this.client != null) this.client.setScreen(new SelectWorldScreen(this));
        }));

        buttons.add(new MenuButton("◈", "TRYB WIELOOSOBOWY", () -> {
            if (this.client != null) this.client.setScreen(new MultiplayerScreen(this));
        }));

        buttons.add(new MenuButton("⚙", "USTAWIENIA", () -> {
            if (this.client != null) this.client.setScreen(new OptionsScreen(this, this.client.options));
        }));

        buttons.add(new MenuButton("✕", "WYJDŹ Z GRY", () -> {
            if (this.client != null) this.client.scheduleStop();
        }));

        int btnWidth = 220;
        int btnHeight = 28;
        int spacing = 7;
        int startY = this.height / 2 - 5;
        int startX = (this.width - btnWidth) / 2;

        for (int i = 0; i < buttons.size(); i++) {
            MenuButton b = buttons.get(i);
            b.width = btnWidth;
            b.height = btnHeight;
            b.x = startX;
            b.y = startY + i * (btnHeight + spacing);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnim = Math.min(1.0f, openAnim + delta * 0.08f);

        // Render vanilla rotating background panorama if available
        super.render(context, mouseX, mouseY, delta);

        // Dark modern vignette gradient overlay
        context.fillGradient(0, 0, this.width, this.height, 0xEE09090C, 0xDD0D0D12);

        int centerX = this.width / 2;

        // Render Logo (Cow Icon)
        int logoSize = 52;
        int logoY = this.height / 2 - 88;
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, COW_LOGO, centerX - logoSize / 2, logoY, 0.0f, 0.0f, logoSize, logoSize, logoSize, logoSize);

        // Title: MOO CLIENT
        String title = "MOO CLIENT";
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawTextWithShadow(this.textRenderer, title, centerX - titleWidth / 2, logoY + logoSize + 6, COLOR_TEXT_PRIMARY);

        // Render Menu Buttons
        for (MenuButton b : buttons) {
            boolean hovered = b.isHovered(mouseX, mouseY);
            if (hovered) {
                b.hoverAnim = Math.min(1.0f, b.hoverAnim + delta * 0.2f);
            } else {
                b.hoverAnim = Math.max(0.0f, b.hoverAnim - delta * 0.2f);
            }

            int bgCol = interpolateColor(COLOR_CARD_BG, COLOR_CARD_HOVER, b.hoverAnim);
            int borderCol = interpolateColor(COLOR_BORDER, COLOR_BORDER_HOVER, b.hoverAnim);
            int textCol = interpolateColor(COLOR_TEXT_MUTED, COLOR_TEXT_PRIMARY, b.hoverAnim);

            // Button Box
            context.fill(b.x, b.y, b.x + b.width, b.y + b.height, bgCol);
            drawBorder(context, b.x, b.y, b.width, b.height, borderCol);

            // Icon + Label
            String fullLabel = b.icon + "  " + b.label;
            int textX = b.x + (b.width - this.textRenderer.getWidth(fullLabel)) / 2;
            int textY = b.y + (b.height - 8) / 2;
            context.drawTextWithShadow(this.textRenderer, fullLabel, textX, textY, textCol);
        }

        // Player Card (Top-Left Corner)
        if (this.client != null && this.client.getSession() != null) {
            String username = this.client.getSession().getUsername();
            int nameWidth = this.textRenderer.getWidth(username);
            int cardW = Math.max(140, nameWidth + 60);
            int cardH = 34;
            int cardX = 14;
            int cardY = 12;

            // Card background & glass border
            context.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0x99101015);
            drawBorder(context, cardX, cardY, cardW, cardH, 0x33FFFFFF);

            // Player Avatar (Skin head)
            try {
                net.minecraft.client.util.SkinTextures skin = this.client.getSkinProvider().getSkinTextures(this.client.getGameProfile());
                net.minecraft.client.gui.PlayerSkinDrawer.draw(context, skin, cardX + 5, cardY + 5, 24);
            } catch (Throwable ignored) {}

            // Player Name & Status
            context.drawTextWithShadow(this.textRenderer, username, cardX + 34, cardY + 6, COLOR_TEXT_PRIMARY);
            context.drawTextWithShadow(this.textRenderer, "Zalogowano §a✓", cardX + 34, cardY + 18, COLOR_TEXT_MUTED);

            // Signal / Ping Bars (Green)
            int pingX = cardX + cardW - 18;
            int pingY = cardY + cardH - 8;
            int pingColor = 0xFF00D68F;
            context.fill(pingX, pingY - 3, pingX + 2, pingY, pingColor);
            context.fill(pingX + 3, pingY - 6, pingX + 5, pingY, pingColor);
            context.fill(pingX + 6, pingY - 9, pingX + 8, pingY, pingColor);
            context.fill(pingX + 9, pingY - 12, pingX + 11, pingY, pingColor);
        }

        // Footer: Left
        context.drawTextWithShadow(this.textRenderer, "Moo Client v" + com.mooclient.MooClient.VERSION, 14, this.height - 18, 0x88FFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            for (MenuButton b : buttons) {
                if (b.isHovered((int) mouseX, (int) mouseY)) {
                    if (this.client != null) {
                        this.client.getSoundManager().play(
                            net.minecraft.client.sound.PositionedSoundInstance.master(
                                net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f
                            )
                        );
                    }
                    b.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private int interpolateColor(int c1, int c2, float ratio) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
