package com.example.helpermod;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class KeyHandler {
    public static KeyBinding openGui;

    public static void init() {
        openGui = new KeyBinding(
            "key.helpermod.open",
            Keyboard.KEY_RSHIFT,
            "key.categories.helpermod"
        );
        ClientRegistry.registerKeyBinding(openGui);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (openGui.isPressed() && mc.currentScreen == null && mc.thePlayer != null) {
            mc.displayGuiScreen(new HelperGui());
        }
    }
}
