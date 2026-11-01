package com.example.helpermod;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatEvent;

import java.util.List;

public class ChatHandler {

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String msg = event.message.trim();
        if (!msg.startsWith(".cfg")) return;

        event.setCanceled(true);
        String[] p = msg.split("\\s+");

        if (p.length < 2) {
            send("§b§lHelper §f§lMod §8» §7Использование: §f.cfg save/load/list §7[имя]");
            return;
        }

        String action = p[1].toLowerCase();

        if (action.equals("list")) {
            List<String> cfgs = ConfigManager.list();
            if (cfgs.isEmpty()) {
                send("§b§lHelper §f§lMod §8» §7Нет сохранённых конфигов");
            } else {
                send("§b§lHelper §f§lMod §8» §7Конфиги §8(" + cfgs.size() + "):");
                StringBuilder sb = new StringBuilder("  §f");
                for (int i = 0; i < cfgs.size(); i++) {
                    if (i > 0) sb.append("§8, §f");
                    sb.append(cfgs.get(i));
                }
                send(sb.toString());
            }
            return;
        }

        if (p.length < 3) {
            send("§b§lHelper §f§lMod §8» §cУкажи имя конфига");
            return;
        }
        String name = p[2];

        if (action.equals("save")) {
            try {
                ConfigManager.save(name);
                send("§b§lHelper §f§lMod §8» §aКонфиг §f" + name + " §aсохранён");
            } catch (Exception e) {
                send("§b§lHelper §f§lMod §8» §cОшибка: " + e.getMessage());
            }
        } else if (action.equals("load")) {
            if (ConfigManager.load(name)) {
                send("§b§lHelper §f§lMod §8» §aКонфиг §f" + name + " §aзагружен");
            } else {
                send("§b§lHelper §f§lMod §8» §cКонфиг §f" + name + " §cне найден");
            }
        } else {
            send("§b§lHelper §f§lMod §8» §cНеизвестная команда: §f" + action);
        }
    }

    private void send(String msg) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(msg));
    }
                  }
