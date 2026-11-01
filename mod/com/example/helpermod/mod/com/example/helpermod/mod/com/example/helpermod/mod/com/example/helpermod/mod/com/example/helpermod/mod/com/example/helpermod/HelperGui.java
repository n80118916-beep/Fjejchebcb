package com.example.helpermod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class HelperGui extends GuiScreen {

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 340;

    private static final int ACCENT_DEFAULT = 0xFF3DAAFF;
    private static final int BG_PANEL       = 0xFF141822;
    private static final int BG_INNER       = 0xFF1A2029;
    private static final int BG_TAB         = 0xFF212833;
    private static final int BG_TAB_HOVER   = 0xFF2A3342;
    private static final int BG_TAB_ACTIVE  = 0xFF2A5070;
    private static final int BG_ROW         = 0xFF1E252F;
    private static final int BG_ROW_HOVER   = 0xFF232B37;
    private static final int BORDER_SUBTLE  = 0x20FFFFFF;

    private static final int HUD_TAB = 6;

    private static final String[] TABS = {
            "Главное", "Combot", "Render", "Настройки", "Инфо", "Автор", "HUD"
    };

    public static final String[] COMBAT_OPTIONS = { "Aura" };
    public static final String[] RENDER_OPTIONS = {
            "ESP", "NameTagger", "Hat", "WaterMark", "XP",
            "Fullbright", "Tracers", "NoHurtCam", "StorageESP"
    };

    public static boolean[] combatToggles = new boolean[COMBAT_OPTIONS.length];
    public static boolean[] renderToggles = new boolean[RENDER_OPTIONS.length];

    private int px, py, pw, ph;
    private int selectedTab = 0;
    private boolean auraExpanded = false;
    private int draggingSlider = -1;

    private boolean draggingWM = false;
    private boolean draggingXP = false;
    private int dragOffsetX, dragOffsetY;

    @Override
    public void initGui() {
        pw = PANEL_W;
        ph = PANEL_H;
        px = (this.width  - pw) / 2;
        py = (this.height - ph) / 2;

        buttonList.clear();
        buttonList.add(new RoundedButton(99, px + pw / 2 - 60, py + ph - 36, 120, 22, "Закрыть"));
    }

    private int getAccentColor() {
        switch (selectedTab) {
            case 1: return 0xFFFF5A5A;
            case 2: return 0xFFB06CFF;
            case 4: return 0xFF5ACC8E;
            case 5: return 0xFFFFC857;
            case 6: return 0xFF00DDCC;
            default: return ACCENT_DEFAULT;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long time = System.currentTimeMillis();
        float pulse = (float) (Math.sin(time / 350.0) * 0.5 + 0.5);

        int accent = getAccentColor();
        int accentRGB = accent & 0xFFFFFF;

        drawRect(0, 0, width, height, 0xB0000000);

        int haloA = (int) (30 + pulse * 30);
        drawRoundedRect(px - 14, py - 14, pw + 28, ph + 28, 18, (haloA << 24) | accentRGB);
        drawRoundedRect(px -  8, py -  8, pw + 16, ph + 16, 14, (haloA << 24) | accentRGB);

        for (int i = 1; i <= 3; i++) {
            drawRoundedRect(px - i, py + i + 2, pw + i * 2, ph + i * 2, 8 + i, 0x12000000);
        }

        drawRoundedRect(px, py, pw, ph, 8, BG_PANEL);
        drawRoundedRect(px + 1, py + 1, pw - 2, ph - 2, 7, BORDER_SUBTLE);

        int lineX = px + 20, lineY = py + 12, lineW = pw - 40;
        drawRoundedRect(lineX, lineY, lineW, 2, 1, accent);

        int shineW = 40;
        int shinePos = (int) ((time / 6) % (lineW + shineW)) - shineW;
        int sx1 = Math.max(lineX, lineX + shinePos);
        int sx2 = Math.min(lineX + lineW, lineX + shinePos + shineW);
        if (sx2 > sx1) drawRect(sx1, lineY, sx2, lineY + 2, 0xA0FFFFFF);

        fontRendererObj.drawStringWithShadow("§l§bHelper §f§lMod", px + 20, py + 22, 0xFFFFFF);
        fontRendererObj.drawString("§8v1.1  ·  §7Minecraft 1.7.10", px + 20, py + 36, 0x999999);

        drawRect(px + 20, py + 52, px + pw - 20, py + 53, 0x25FFFFFF);

        int tabX = px + 18, tabY = py + 62, tabW = 104, tabH = 26, tabGap = 4;
        for (int i = 0; i < TABS.length; i++) {
            int ty = tabY + i * (tabH + tabGap);
            boolean hover = mouseX >= tabX && mouseX < tabX + tabW && mouseY >= ty && mouseY < ty + tabH;
            boolean sel = (i == selectedTab);

            drawRoundedRect(tabX, ty, tabW, tabH, 4, sel ? BG_TAB_ACTIVE : (hover ? BG_TAB_HOVER : BG_TAB));
            if (sel) {
                drawRoundedRect(tabX - 1, ty + 2, 5, tabH - 4, 2, ((int) (50 + pulse * 60) << 24) | accentRGB);
                drawRoundedRect(tabX, ty + 3, 3, tabH - 6, 1, accent);
            }
            int dotColor = sel ? accent : (hover ? 0xFF7A8494 : 0xFF4A5260);
            drawRoundedRect(tabX + 10, ty + tabH / 2 - 3, 6, 6, 3, dotColor);

            int textColor = sel ? 0xFFFFFF : (hover ? 0xCCCCCC : 0x888888);
            fontRendererObj.drawString(TABS[i], tabX + 22, ty + 9, textColor);
        }

        int cx = px + 132, cy = py + 62, cw = pw - 152, ch = ph - 62 - 52;
        drawRoundedRect(cx, cy, cw, ch, 6, BG_INNER);
        drawGradientRect(cx + 6, cy + 6, cx + cw - 6, cy + ch - 6, 0x00000000, 0x18000000);
        drawRoundedRect(cx + 1, cy + 1, cw - 2, ch - 2, 5, 0x10FFFFFF);

        drawTabContent(cx, cy, cw, ch, mouseX, mouseY, accent);

        drawRect(px + 20, py + ph - 46, px + pw - 20, py + ph - 45, 0x25FFFFFF);
        fontRendererObj.drawString("§8© 2026", px + 20, py + ph - 32, 0x666666);
        fontRendererObj.drawString("§8v1.1.0", px + pw - 52, py + ph - 32, 0x666666);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawTabContent(int cx, int cy, int cw, int ch, int mouseX, int mouseY, int accent) {
        int x = cx + 14, y = cy + 14;
        switch (selectedTab) {
            case 1:  drawCombatTab(cx, cy, cw, mouseX, mouseY, accent); break;
            case 2:  drawToggleList(RENDER_OPTIONS, renderToggles, "Render", x, y, cw, mouseX, mouseY, accent); break;
            case 6:  drawHudTab(cx, cy, accent); break;
            default: drawInfoTab(x, y, accent); break;
        }
    }

    private void drawCombatTab(int cx, int cy, int cw, int mouseX, int mouseY, int accent) {
        int x = cx + 14;
        int y = cy + 14;
        fontRendererObj.drawStringWithShadow("§lCombot", x, y, 0xFFFFFF);
        drawRect(x, y + 12, x + fontRendererObj.getStringWidth("Combot") + 4, y + 13, accent);

        int rowW = cw - 28;
        int rowH = 22;
        int rowY = y + 24;

        boolean hover = mouseX >= x && mouseX < x + rowW && mouseY >= rowY && mouseY < rowY + rowH;
        drawRoundedRect(x, rowY, rowW, rowH, 4, hover ? BG_ROW_HOVER : BG_ROW);

        int labelColor = combatToggles[0] ? 0xFFFFFF : 0xAAAAAA;
        fontRendererObj.drawString("Aura", x + 8, rowY + 7, labelColor);
        fontRendererObj.drawString(auraExpanded ? "§8▼" : "§8▶", x + rowW - 48, rowY + 7, 0x999999);
        drawSwitch(x + rowW - 40, rowY + 4, combatToggles[0], hover, accent);

        if (auraExpanded) {
            int subX = x + 4;
            int subW = rowW - 8;
            int subY = rowY + rowH + 4;

            drawSlider(subX, subY, subW, "Дистанция",
                    AuraSettings.range, 2.0, 6.0, "%.1f",
                    mouseX, mouseY, accent);
            subY += 26;

            drawSlider(subX, subY, subW, "Задержка",
                    (double) AuraSettings.delay, 100, 1000, "%.0f мс",
                    mouseX, mouseY, accent);
            subY += 26;

            drawSlider(subX, subY, subW, "Скорость поворота",
                    AuraSettings.rotationSpeed, 5, 90, "%.0f°",
                    mouseX, mouseY, accent);
            subY += 26;

            boolean chov = mouseX >= subX && mouseX < subX + subW
                    && mouseY >= subY && mouseY < subY + 22;
            drawRoundedRect(subX, subY, subW, 22, 4, chov ? BG_ROW_HOVER : BG_ROW);
            fontRendererObj.drawString("Только криты",
                    subX + 8, subY + 7,
                    AuraSettings.onlyCrits ? 0xFFFFFF : 0xAAAAAA);
            drawSwitch(subX + subW - 40, subY + 4, AuraSettings.onlyCrits, chov, accent);

            subY += 26;
            boolean bh = mouseX >= subX && mouseX < subX + subW
                    && mouseY >= subY && mouseY < subY + 22;
            drawRoundedRect(subX, subY, subW, 22, 4, bh ? BG_ROW_HOVER : BG_ROW);
            fontRendererObj.drawString("Бить за спиной",
                    subX + 8, subY + 7,
                    AuraSettings.attackBehind ? 0xFFFFFF : 0xAAAAAA);
            drawSwitch(subX + subW - 40, subY + 4, AuraSettings.attackBehind, bh, accent);
        }
    }

    private void drawToggleList(String[] options, boolean[] states, String title,
                                int x, int y, int cw, int mouseX, int mouseY, int accent) {
        fontRendererObj.drawStringWithShadow("§l" + title, x, y, 0xFFFFFF);
        drawRect(x, y + 12, x + fontRendererObj.getStringWidth(title) + 4, y + 13, accent);

        int rowY = y + 24, rowW = cw - 28, rowH = 22, gap = 4;
        for (int i = 0; i < options.length; i++) {
            int ry = rowY + i * (rowH + gap);
            boolean hover = mouseX >= x && mouseX < x + rowW && mouseY >= ry && mouseY < ry + rowH;
            drawRoundedRect(x, ry, rowW, rowH, 4, hover ? BG_ROW_HOVER : BG_ROW);

            int labelColor = states[i] ? 0xFFFFFF : 0xAAAAAA;
            fontRendererObj.drawString(options[i], x + 8, ry + 7, labelColor);
            drawSwitch(x + rowW - 40, ry + 4, states[i], hover, accent);
        }
    }

    private void drawSwitch(int x, int y, boolean on, boolean hover, int accent) {
        int w = 32, h = 14;
        if (on) {
            drawRoundedRect(x - 2, y - 2, w + 4, h + 4, (h + 4) / 2, (40 << 24) | (accent & 0xFFFFFF));
            drawRoundedRect(x, y, w, h, h / 2, accent);
        } else {
            drawRoundedRect(x, y, w, h, h / 2, hover ? 0xFF3A424F : 0xFF2A3038);
        }
        int thumbSize = h - 4;
        int thumbX = on ? x + w - thumbSize - 2 : x + 2;
        drawRoundedRect(thumbX, y + 2, thumbSize, thumbSize, thumbSize / 2, on ? 0xFFFFFFFF : 0xFFB0B6C0);
    }

    private void drawSlider(int x, int y, int w, String label,
                            double value, double min, double max, String fmt,
                            int mouseX, int mouseY, int accent) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 22;
        drawRoundedRect(x, y, w, 22, 4, hover ? BG_ROW_HOVER : BG_ROW);

        String valStr = String.format(fmt, value);
        fontRendererObj.drawString(label, x + 8, y + 4, 0xCCCCCC);
        int valW = fontRendererObj.getStringWidth(valStr);
        fontRendererObj.drawString(valStr, x + w - valW - 8, y + 4, 0xFFFFFF);

        int barX = x + 8, barY = y + 16, barW = w - 16, barH = 3;
        drawRoundedRect(barX, barY, barW, barH, 1, 0xFF2A3038);

        double ratio = (value - min) / (max - min);
        ratio = Math.max(0, Math.min(1, ratio));
        int fillW = (int) (barW * ratio);
        if (fillW > 0) {
            drawRoundedRect(barX, barY, fillW, barH, 1, accent);
            drawRoundedRect(barX + fillW - 2, barY - 2, 4, barH + 4, 2, 0xFFFFFFFF);
        }
    }

    private void drawHudTab(int cx, int cy, int accent) {
        int x = cx + 14, y = cy + 14;
        fontRendererObj.drawStringWithShadow("§lHUD Редактор", x, y, 0xFFFFFF);
        drawRect(x, y + 12, x + fontRendererObj.getStringWidth("HUD Редактор") + 4, y + 13, accent);

        fontRendererObj.drawString("§7Перетащи карточки §fмышкой§7:", x, y + 26, 0xAAAAAA);
        fontRendererObj.drawString("§f• §bWaterMark §7(по умолчанию слева-сверху)", x + 6, y + 42, 0xFFFFFF);
        fontRendererObj.drawString("§f• §cXP §7(по умолчанию над хотбаром)", x + 6, y + 56, 0xFFFFFF);
        fontRendererObj.drawString("§8Позиции сохраняются в конфиг:", x, y + 86, 0x666666);
        fontRendererObj.drawString("§f  .cfg save §7<имя>", x, y + 100, 0xAAAAAA);
        fontRendererObj.drawString("§8Загрузить:", x, y + 118, 0x666666);
        fontRendererObj.drawString("§f  .cfg load §7<имя>", x, y + 132, 0xAAAAAA);
        fontRendererObj.drawString("§8Список всех конфигов:", x, y + 150, 0x666666);
        fontRendererObj.drawString("§f  .cfg list", x, y + 164, 0xAAAAAA);
    }

    private void drawInfoTab(int x, int y, int accent) {
        String title;
        String[] lines;
        switch (selectedTab) {
            case 0:
                title = "Главное меню";
                lines = new String[]{
                        "§7Добро пожаловать в §bHelper Mod§7!",
                        "§7Слева — разделы §cC§6o§em§ab§9a§dt§7, §dRender§7 и §bHUD§7.",
                        "",
                        "§8ПКМ по Aura открывает ползунки."
                };
                break;
            case 3:
                title = "Настройки";
                lines = new String[]{"§7Здесь будут настройки мода.", "§8(в разработке)"};
                break;
            case 4:
                title = "Информация";
                String user = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
                String coords = mc.thePlayer != null
                        ? (int) mc.thePlayer.posX + ", " + (int) mc.thePlayer.posY + ", " + (int) mc.thePlayer.posZ
                        : "—";
                lines = new String[]{
                        "§7Игрок: §f" + user,
                        "§7Координаты: §f" + coords,
                        "§7FPS: §f" + Minecraft.getDebugFPS(),
                        "§7Версия MC: §f1.7.10"
                };
                break;
            case 5:
            default:
                title = "Об авторе";
                lines = new String[]{"§7Сделано специально для тебя", "§8Спасибо за использование!"};
                break;
        }
        fontRendererObj.drawStringWithShadow("§l" + title, x, y, 0xFFFFFF);
        drawRect(x, y + 12, x + fontRendererObj.getStringWidth(title) + 4, y + 13, accent);
        int ly = y + 26;
        for (String line : lines) {
            fontRendererObj.drawString(line, x, ly, 0xAAAAAA);
            ly += 12;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == 0 && selectedTab == HUD_TAB) {
            if (mouseX >= HudPositions.wmX && mouseX <= HudPositions.wmX + 220
                    && mouseY >= HudPositions.wmY && mouseY <= HudPositions.wmY + 32) {
                draggingWM = true;
                dragOffsetX = mouseX - HudPositions.wmX;
                dragOffsetY = mouseY - HudPositions.wmY;
                return;
            }
            int[] xp = getXpDefaultPos();
            int xpX = HudPositions.xpX >= 0 ? HudPositions.xpX : xp[0];
            int xpY = HudPositions.xpY >= 0 ? HudPositions.xpY : xp[1];
            if (mouseX >= xpX && mouseX <= xpX + 200 && mouseY >= xpY && mouseY <= xpY + 32) {
                if (HudPositions.xpX < 0) {
                    HudPositions.xpX = xpX;
                    HudPositions.xpY = xpY;
                }
                draggingXP = true;
                dragOffsetX = mouseX - HudPositions.xpX;
                dragOffsetY = mouseY - HudPositions.xpY;
                return;
            }
        }

        if (button == 0) {
            int tabX = px + 18, tabY = py + 62, tabW = 104, tabH = 26, tabGap = 4;
            for (int i = 0; i < TABS.length; i++) {
                int ty = tabY + i * (tabH + tabGap);
                if (mouseX >= tabX && mouseX < tabX + tabW && mouseY >= ty && mouseY < ty + tabH) {
                    selectedTab = i;
                    return;
                }
            }
        }

        // Combot
        if (selectedTab == 1) {
            int cx = px + 132, cy = py + 62, cw = pw - 152;
            int x = cx + 14, rowY = cy + 14 + 24, rowW = cw - 28, rowH = 22;

            if (mouseX >= x && mouseX < x + rowW && mouseY >= rowY && mouseY < rowY + rowH) {
                if (button == 1) auraExpanded = !auraExpanded;
                else if (button == 0) combatToggles[0] = !combatToggles[0];
                return;
            }

            if (auraExpanded) {
                int subX = x + 4;
                int subW = rowW - 8;
                int subY = rowY + rowH + 4;

                if (button == 0 && mouseX >= subX && mouseX < subX + subW && mouseY >= subY && mouseY < subY + 22) {
                    draggingSlider = 0;
                    setSliderValue(0, mouseX, subX, subW);
                    return;
                }
                subY += 26;
                if (button == 0 && mouseX >= subX && mouseX < subX + subW && mouseY >= subY && mouseY < subY + 22) {
                    draggingSlider = 1;
                    setSliderValue(1, mouseX, subX, subW);
                    return;
                }
                subY += 26;
                if (button == 0 && mouseX >= subX && mouseX < subX + subW && mouseY >= subY && mouseY < subY + 22) {
                    draggingSlider = 2;
                    setSliderValue(2, mouseX, subX, subW);
                    return;
                }
                subY += 26;
                if (button == 0 && mouseX >= subX && mouseX < subX + subW && mouseY >= subY && mouseY < subY + 22) {
                    AuraSettings.onlyCrits = !AuraSettings.onlyCrits;
                    return;
                }
                subY += 26;
                if (button == 0 && mouseX >= subX && mouseX < subX + subW && mouseY >= subY && mouseY < subY + 22) {
                    AuraSettings.attackBehind = !AuraSettings.attackBehind;
                    return;
                }
            }
        }

        // Render
        if (selectedTab == 2) {
            int cx = px + 132, cy = py + 62, cw = pw - 152;
            int x = cx + 14, rowY = cy + 14 + 24, rowW = cw - 28, rowH = 22, gap = 4;
            for (int i = 0; i < renderToggles.length; i++) {
                int ry = rowY + i * (rowH + gap);
                if (mouseX >= x && mouseX < x + rowW && mouseY >= ry && mouseY < ry + rowH) {
                    renderToggles[i] = !renderToggles[i];
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceClick) {
        if (draggingWM) {
            HudPositions.wmX = mouseX - dragOffsetX;
            HudPositions.wmY = mouseY - dragOffsetY;
        }
        if (draggingXP) {
            HudPositions.xpX = mouseX - dragOffsetX;
            HudPositions.xpY = mouseY - dragOffsetY;
        }
        if (draggingSlider >= 0 && selectedTab == 1) {
            int cx = px + 132, cw = pw - 152;
            int x = cx + 14, rowW = cw - 28;
            int subX = x + 4;
            int subW = rowW - 8;
            setSliderValue(draggingSlider, mouseX, subX, subW);
        }
        super.mouseClickMove(mouseX, mouseY, button, timeSinceClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
        if (which == 0) {
            draggingWM = false;
            draggingXP = false;
            draggingSlider = -1;
        }
        super.mouseMovedOrUp(mouseX, mouseY, which);
    }

    private void setSliderValue(int which, int mouseX, int sx, int sw) {
        double ratio = (double) (mouseX - sx - 8) / (sw - 16);
        ratio = Math.max(0, Math.min(1, ratio));

        if (which == 0) {
            double v = 2.0 + ratio * 4.0;
            AuraSettings.range = Math.round(v * 10) / 10.0;
        } else if (which == 1) {
            long v = Math.round(100 + ratio * 900);
            AuraSettings.delay = (v / 10) * 10;
        } else if (which == 2) {
            AuraSettings.rotationSpeed = Math.round(5 + ratio * 85);
        }
    }

    private int[] getXpDefaultPos() {
        int panelW = 180, panelH = 32;
        return new int[]{(th
