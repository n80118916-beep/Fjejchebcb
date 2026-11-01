package com.example.helpermod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HelperMod.MODID, name = "Helper Mod", version = "1.1")
public class HelperMod {
    public static final String MODID = "helpermod";

    @Mod.Instance(MODID)
    public static HelperMod instance;

    @SidedProxy(
        clientSide = "com.example.helpermod.ClientProxy",
        serverSide = "com.example.helpermod.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }
  }
