package com.example.helpermod;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private EntityLivingBase lastTarget = null;
    private long lastTargetTime = 0;

    private boolean fullbrightWasOn = false;
    private float savedGamma = 1.0f;

    private int savedHurtTime = 0;
    private boolean hurtCamRestore = false;

    private static final String[] ENCH = new String[] {
            "Защ", "ОгнЗащ", "Падение", "ВзрЗащ", "СнарЗащ", "Дых", "ВодСрод", "Шип",
            null, null, null, null, null, null, null, null,
            "Остр", "Небеса", "Паук", "Отбр", "Огонь", "Марод",
            null, null, null, null, null, null, null, null, null, null,
            "Эфф", "Шёлк", "Прочн", "Удача",
            null, null, null, null, null, null, null, null, null, null, null, null,
            "Сила", "Удар", "Пламя", "Беск",
            null, null, null, null, null, null, null, null, null,
            "УдМоря", "Прим"
    };
    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    // ═══════════════════════════════════════════════
    //  Мир
    // ═══════════════════════════════════════════════
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (HelperGui.renderToggles[0]) renderESP();
        if (HelperGui.renderToggles[2]) renderHatAll(event.partialTicks);
        if (HelperGui.renderToggles[6]) renderTracers();
        if (HelperGui.renderToggles[8]) renderStorageESP();
    }

    private void renderESP() {
        double rx = RenderManager.renderPosX, ry = RenderManager.renderPosY, rz = RenderManager.renderPosZ;
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0f);
        for (Object obj : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) obj;
            if (p == mc.thePlayer || p.isDead) continue;
            AxisAlignedBB bb = p.boundingBox.offset(-rx, -ry, -rz).expand(0.08, 0.08, 0.08);
            drawBox(bb, 0.24f, 0.67f, 1.0f);
        }
        GL11.glLineWidth(1.0f);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderHatAll(float partialTicks) {
        double rx = RenderManager.renderPosX, ry = RenderManager.renderPosY, rz = RenderManager.renderPosZ;
        for (Object obj : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) obj;
            if (p == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) continue;
            if (p.getDistanceToEntity(mc.thePlayer) > 64) continue;
            renderHat(p, rx, ry, rz, partialTicks);
        }
    }

    private void renderHat(EntityPlayer player, double rx, double ry, double rz, float partialTicks) {
        double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - rx;
        double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - ry;
        double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - rz;
        float yaw = player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTranslated(px, py + player.height, pz);
        GL11.glRotatef(-yaw, 0f, 1f, 0f);

        float r = 1.00f, g = 0.35f, b = 0.70f;
        drawSolidCube(-0.48f, 0.00f, -0.48f, 0.48f, 0.08f, 0.48f, r, g, b);
        drawSolidCube(-0.28f, 0.08f, -0.28f, 0.28f, 0.42f, 0.28f, r * 0.90f, g * 0.90f, b * 0.90f);
        drawSolidCube(-0.28f, 0.42f, -0.28f, 0.28f, 0.44f, 0.28f, 1.00f, 0.72f, 0.90f);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderTracers() {
        double rx = RenderManager.renderPosX, ry = RenderManager.renderPosY, rz = RenderManager.renderPosZ;
        double eyeX = mc.thePlayer.posX - rx;
        double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - ry;
        double eyeZ = mc.thePlayer.posZ - rz;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.5f);

        for (Object obj : mc.theWorld.playerEntities) {
            EntityPlayer p = (EntityPlayer) obj;
            if (p == mc.thePlayer) continue;
            if (p.getDistanceToEntity(mc.thePlayer) > 64) continue;
            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor4f(0.24f, 0.67f, 1.0f, 0.9f);
            GL11.glVertex3d(eyeX, eyeY, eyeZ);
            GL11.glVertex3d(p.posX - rx, p.posY + p.height * 0.5 - ry, p.posZ - rz);
            GL11.glEnd();
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void renderStorageESP() {
        double rx = RenderManager.renderPosX, ry = RenderManager.renderPosY, rz = RenderManager.renderPosZ;
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.5f);

        for (Object obj : mc.theWorld.loadedTileEntityList) {
            if (!(obj instanceof TileEntity)) continue;
            TileEntity te = (TileEntity) obj;
            float[] c = getStorageColor(te);
            if (c == null) continue;
            double dx = te.xCoord + 0.5 - mc.thePlayer.posX;
            double dy = te.yCoord + 0.5 - mc.thePlayer.posY;
            double dz = te.zCoord + 0.5 - mc.thePlayer.posZ;
            if (dx * dx + dy * dy + dz * dz > 96 * 96) continue;
            AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
                    te.xCoord - rx, te.yCoord - ry, te.zCoord - rz,
                    te.xCoord + 1 - rx, te.yCoord + 1 - ry, te.zCoord + 1 - rz
            ).expand(0.002, 0.002, 0.002);
            drawBox(bb, c[0], c[1], c[2]);
        }

        GL11.glLineWidth(1.0f);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private float[] getStorageColor(TileEntity te) {
        if (te instanceof TileEntityEnderChest)   return new float[]{0.70f, 0.30f, 1.00f};
        if (te instanceof TileEntityChest)        return new float[]{1.00f, 0.65f, 0.20f};
        if (te instanceof TileEntityFurnace)      return new float[]{0.75f, 0.75f, 0.75f};
        if (te instanceof TileEntityDispenser)    return new float[]{0.30f, 1.00f, 0.30f};
        if (te instanceof TileEntityDropper)      return new float[]{0.30f, 0.70f, 1.00f};
        if (te instanceof TileEntityBrewingStand) return new float[]{0.30f, 1.00f, 1.00f};
        if (te instanceof TileEntityHopper)       return new float[]{0.80f, 0.80f, 0.30f};
        return null;
    }

    // ═══════════════════════════════════════════════
    //  Оверлей (WaterMark / XP)
    // ═══════════════════════════════════════════════
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null) return;
        FontRenderer fr = mc.fontRenderer;

        // ─── WaterMark ───
        if (HelperGui.renderToggles[3]) {
            ServerData sd = mc.getCurrentServerData();
            String serverName;
            int ping = -1;
            if (sd == null) {
                serverName = "§7Singleplayer";
            } else {
                serverName = sd.serverName;
                if (serverName == null || serverName.isEmpty()) serverName = sd.serverIP;
                ping = sd.pingToServer;
            }
            String version = "§71.7.10";
            String modName = "§b§lHelper §f§lMod";
            int fps = Minecraft.getDebugFPS();
            String pingStr = (ping >= 0)
                    ? "§7Ping: §" + pingColor(ping) + ping + "ms"
                    : "§7Ping: §8—";
            String fpsStr = "§7FPS: §" + fpsColor(fps) + fps;
            String line1 = modName + "  §8|  §f" + serverName + "  §8|  " + version;
            String line2 = pingStr + "  §8|  " + fpsStr;

            int w1 = fr.getStringWidth(line1);
            int w2 = fr.getStringWidth(line2);
            int textW = Math.max(w1, w2);
            int iconSize = 22;
            int padL = 7, padR = 10, padT = 5, padB = 5, gap = 7;
            int panelW = padL + iconSize + gap + textW + padR;
            int panelH = padT + 10 + 2 + 10 + padB;
            int baseX = HudPositions.wmX, baseY = HudPositions.wmY;

            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            HelperGui.drawRoundedRect(baseX, baseY, panelW, panelH, 5, 0xD00D1116);
            HelperGui.drawRoundedRect(baseX, baseY, panelW, panelH, 5, 0x18449AFF);
            HelperGui.drawRoundedRect(baseX + 4, baseY, panelW - 8, 1, 1, 0xFF3DAAFF);
            HelperGui.drawRoundedRect(baseX, baseY + 4, 2, panelH - 8, 1, 0xFF3DAAFF);

            int ix = baseX + padL, iy = baseY + (panelH - iconSize) / 2;
            HelperGui.drawRoundedRect(ix, iy, iconSize, iconSize, 4, 0xFF141822);
            HelperGui.drawRoundedRect(ix + 1, iy + 1, iconSize - 2, iconSize - 2, 3, 0xFF2A5070);
            HelperGui.drawRoundedRect(ix + 3, iy + 3, iconSize - 6, iconSize - 6, 2, 0x803DAAFF);
            fr.drawStringWithShadow("§b§lH", ix + 7, iy + 7, 0xFFFFFF);

            int tx = ix + iconSize + gap, ty = baseY + padT;
            fr.drawStringWithShadow(line1, tx, ty, 0xFFFFFF);
            fr.drawStringWithShadow(line2, tx, ty + 12, 0xFFFFFF);

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

        // ─── XP (TargetHUD) ───
        if (HelperGui.renderToggles[4]) {
            if (mc.objectMouseOver != null
                    && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity e = mc.objectMouseOver.entityHit;
                if (e instanceof EntityLivingBase && e != mc.thePlayer && !e.isDead) {
                    lastTarget = (EntityLivingBase) e;
                    lastTargetTime = System.currentTimeMillis();
                }
            }
            if (lastTarget != null
                    && System.currentTimeMillis() - lastTargetTime < 3000
                    && !lastTarget.isDead
                    && lastTarget.getHealth() > 0) {
                drawXP(lastTarget, event, fr);
            }
        }
    }

    private void drawXP(EntityLivingBase target, RenderGameOverlayEvent.Post event, FontRenderer fr) {
        int sw = event.resolution.getScaledWidth();
        int sh = event.resolution.getScaledHeight();

        String name = target.getCommandSenderName();
        if (name.length() > 16) name = name.substring(0, 15) + "…";
        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        String hpStr = (int) Math.ceil(hp) + "/" + (int) maxHp;

        int iconSize = 26, padL = 6, padR = 10, padT = 5, padB = 5, gap = 7, barH = 4;
        int nameW = fr.getStringWidth(name);
        int hpW   = fr.getStringWidth(hpStr);
        int line1W = nameW + 6 + hpW;
        int barW   = Math.max(line1W, 80);
        int panelW = padL + iconSize + gap + barW + padR;
        int panelH = padT + 10 + 4 + 10 + 4 + barH + padB;

        int baseX, baseY;
        if (HudPositions.xpX >= 0 && HudPositions.xpY >= 0) {
            baseX = HudPositions.xpX;
            baseY = HudPositions.xpY;
        } else {
            baseX = (sw - panelW) / 2;
            baseY = sh - 22 - panelH - 4;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        HelperGui.drawRoundedRect(baseX, baseY, panelW, panelH, 5, 0xD00D1116);
        HelperGui.drawRoundedRect(baseX, baseY, panelW, panelH, 5, 0x18FF4444);
        HelperGui.drawRoundedRect(baseX + 4, baseY, panelW - 8, 1, 1, 0xFFFF5A5A);
        HelperGui.drawRoundedRect(baseX, baseY + 4, 2, panelH - 8, 1, 0xFFFF5A5A);

        int ix = baseX + padL, iy = baseY + (panelH - iconSize) / 2;
        HelperGui.drawRoundedRect(ix, iy, iconSize, iconSize, 4, 0xFF141822);
        drawEntityHead(target, ix + 1, iy + 1, iconSize - 2);
        HelperGui.drawRoundedRect(ix, iy, iconSize, iconSize, 4, 0x30FF5A5A);

        int tx = ix + iconSize + gap, ty = baseY + padT;
        fr.drawStringWithShadow(name, tx, ty, 0xFFFFFF);
        fr.drawStringWithShadow("§c" + hpStr, tx + nameW + 6, ty, 0xFFFFFF);

        int barX = tx, barY = ty + 12;
        HelperGui.drawRoundedRect(barX, barY, barW, barH, barH / 2, 0xFF222830);
        float ratio = Math.max(0f, Math.min(1f, hp / maxHp));
        int fillW = (int) (barW * ratio);
        int fillColor = ratio > 0.6f ? 0xFF55DD55 : ratio > 0.3f ? 0xFFEEDD44 : 0xFFFF4444;
        if (fillW > 0) {
            HelperGui.drawRoundedRect(barX, barY, fillW, barH, barH / 2, fillColor);
            HelperGui.drawRoundedRect(barX, barY, fillW, 1, 1, (fillColor & 0xFFFFFF) | 0x80FFFFFF);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawEntityHead(EntityLivingBase entity, int x, int y, int size) {
        Render renderer = RenderManager.instance.getEntityRenderObject(entity);
        if (!(renderer instanceof RendererLivingEntity)) return;
        RendererLivingEntity rle = (RendererLivingEntity) renderer;
        ModelBase model = rle.mainModel;
        if (model == null) return;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        RenderHelper.enableGUIStandardItemLighting();

        try {
            ResourceLocation loc = rle.getEntityTexture(entity);
            if (loc != null) mc.getTextureManager().bindTexture(loc);
        } catch (Exception ignored) {}

        GL11.glTranslatef(x + size / 2f, y + size / 2f, 100);
        float s = size * 1.3f;
        GL11.glScalef(s, s, s);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(180, 0, 1, 0);

        try {
            if (model instanceof ModelBiped) {
                ModelBiped m = (ModelBiped) model;
                m.bipedHead.render(0.0625F);
                if (m.bipedHeadwear != null && m.bipedHeadwear.showModel) m.bipedHeadwear.render(0.0625F);
            } else if (model instanceof ModelQuadruped) {
                ((ModelQuadruped) model).head.render(0.0625F);
            } else {
                GL11.glScalef(0.5f, 0.5f, 0.5f);
                RenderManager.instance.renderEntityWithPosYaw(entity, 0, 0, 0, 0, 1);
            }
        } catch (Exception ignored) {}

        RenderHelper.disableStandardItemLighting();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private String pingColor(int ping) {
        if (ping < 0)   return "8";
        if (ping < 60)  return "a";
        if (ping < 120) return "e";
        if (ping < 200) return "6";
        return "c";
    }

    private String fpsColor(int fps) {
        if (fps >= 120) return "a";
        if (fps >= 60)  return "e";
        if (fps >= 30)  return "6";
        return "c";
    }

    // ═══════════════════════════════════════════════
    //  NameTagger
    // ═══════════════════════════════════════════════
    @SubscribeEvent
    public void onRenderPlayerSpecial(RenderPlayerEvent.Specials.Pre event) {
        if (!HelperGui.renderToggles[1]) return;
        EntityPlayer player = event.entityPlayer;
        boolean isSelf = (player == mc.thePlayer);
        if (isSelf && mc.gameSettings.thirdPersonView == 0) return;
        if (player.getDistanceToEntity(mc.thePlayer) > 64) return;

        event.setCanceled(true);

        double x = event.x;
        double y = event.y + player.height + 0.5;
        double z = event.z;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef( RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-0.02f, -0.02f, 0.02f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        FontRenderer fr = mc.fontRenderer;
        int yOff = 0;

        String name = player.getCommandSenderName();
        int hp = (int) Math.ceil(player.getHealth());
        int maxHp = (int) player.getMaxHealth();
       
