package com.example.helpermod;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.*;

public class ConfigManager {

    private static final File DIR = new File(Minecraft.getMinecraft().mcDataDir, "config/helpermod");

    public static void save(String name) throws IOException {
        if (!DIR.exists()) DIR.mkdirs();
        File f = new File(DIR, name + ".cfg");
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        w.println("# Helper Mod Config v1");

        for (int i = 0; i < HelperGui.COMBAT_OPTIONS.length; i++) {
            w.println("combat." + HelperGui.COMBAT_OPTIONS[i] + "=" + HelperGui.combatToggles[i]);
        }
        for (int i = 0; i < HelperGui.RENDER_OPTIONS.length; i++) {
            w.println("render." + HelperGui.RENDER_OPTIONS[i] + "=" + HelperGui.renderToggles[i]);
        }
        w.println("hud.wmX=" + HudPositions.wmX);
        w.println("hud.wmY=" + HudPositions.wmY);
        w.println("hud.xpX=" + HudPositions.xpX);
        w.println("hud.xpY=" + HudPositions.xpY);
        w.println("aura.range=" + AuraSettings.range);
        w.println("aura.delay=" + AuraSettings.delay);
        w.println("aura.onlyCrits=" + AuraSettings.onlyCrits);
        w.println("aura.rotationSpeed=" + AuraSettings.rotationSpeed);
        w.println("aura.attackBehind=" + AuraSettings.attackBehind);
        w.close();
    }

    public static boolean load(String name) {
        File f = new File(DIR, name + ".cfg");
        if (!f.exists()) return false;
        try {
            Properties p = new Properties();
            p.load(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            for (int i = 0; i < HelperGui.COMBAT_OPTIONS.length; i++) {
                HelperGui.combatToggles[i] = Boolean.parseBoolean(
                        p.getProperty("combat." + HelperGui.COMBAT_OPTIONS[i], "false"));
            }
            for (int i = 0; i < HelperGui.RENDER_OPTIONS.length; i++) {
                HelperGui.renderToggles[i] = Boolean.parseBoolean(
                        p.getProperty("render." + HelperGui.RENDER_OPTIONS[i], "false"));
            }
            HudPositions.wmX = Integer.parseInt(p.getProperty("hud.wmX", "6"));
            HudPositions.wmY = Integer.parseInt(p.getProperty("hud.wmY", "6"));
            HudPositions.xpX = Integer.parseInt(p.getProperty("hud.xpX", "-1"));
            HudPositions.xpY = Integer.parseInt(p.getProperty("hud.xpY", "-1"));
            AuraSettings.range = Double.parseDouble(p.getProperty("aura.range", "3.0"));
            AuraSettings.delay = Long.parseLong(p.getProperty("aura.delay", "550"));
            AuraSettings.onlyCrits = Boolean.parseBoolean(p.getProperty("aura.onlyCrits", "true"));
            AuraSettings.rotationSpeed = Double.parseDouble(p.getProperty("aura.rotationSpeed", "25"));
            AuraSettings.attackBehind = Boolean.parseBoolean(p.getProperty("aura.attackBehind", "true"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> list() {
        List<String> result = new ArrayList<String>();
        if (!DIR.exists()) return result;
        File[] files = DIR.listFiles();
        if (files == null) return result;
        for (File f : files) {
            String n = f.getName();
            if (n.endsWith(".cfg")) result.add(n.substring(0, n.length() - 4));
        }
        Collections.sort(result);
        return result;
    }
                      }
