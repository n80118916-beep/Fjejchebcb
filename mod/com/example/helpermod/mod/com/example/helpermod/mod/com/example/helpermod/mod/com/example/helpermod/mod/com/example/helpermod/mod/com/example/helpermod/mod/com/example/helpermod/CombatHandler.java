package com.example.helpermod;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;

public class CombatHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private long lastAttack = 0;
    private EntityPlayer currentTarget = null;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (mc.thePlayer.isDead || mc.thePlayer.getHealth() <= 0) return;

        if (!HelperGui.combatToggles[0]) {
            currentTarget = null;
            return;
        }

        currentTarget = findTarget();
        if (currentTarget == null) return;

        faceEntity(currentTarget);

        if (AuraSettings.onlyCrits && mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(false);
        }

        if (AuraSettings.onlyCrits && !canCrit()) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < AuraSettings.delay) return;

        mc.playerController.attackEntity(mc.thePlayer, currentTarget);
        mc.thePlayer.swingItem();
        lastAttack = now;
    }

    private EntityPlayer findTarget() {
        EntityPlayer best = null;
        double maxSq = AuraSettings.range * AuraSettings.range;
        double bestDist = maxSq;

        for (Object obj : mc.theWorld.playerEntities) {
            if (!(obj instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) obj;
            if (p == mc.thePlayer) continue;
            if (p.isDead || p.getHealth() <= 0) continue;
            if (!isValidTarget(p)) continue;

            double dist = mc.thePlayer.getDistanceSqToEntity(p);
            if (dist > bestDist) continue;

            if (!AuraSettings.attackBehind && isBehind(p)) continue;

            bestDist = dist;
            best = p;
        }
        return best;
    }

    private boolean isValidTarget(EntityPlayer p) {
        if (isBot(p)) return false;
        boolean naked     = (getArmorCount(p) == 0);
        boolean invisible = p.isInvisible();
        if (naked && invisible) return false;
        return true;
    }

    private int getArmorCount(EntityPlayer p) {
        int count = 0;
        for (int slot = 1; slot <= 4; slot++) {
            if (p.getEquipmentInSlot(slot) != null) count++;
        }
        return count;
    }

    private boolean isBot(EntityPlayer p) {
        if (mc.getNetHandler() == null) return false;
        String name = p.getCommandSenderName();
        for (Object obj : mc.getNetHandler().playerInfoList) {
            GuiPlayerInfo info = (GuiPlayerInfo) obj;
            if (info.name != null && info.name.equals(name)) return false;
        }
        return true;
    }

    private boolean isBehind(EntityPlayer p) {
        double dx = p.posX - mc.thePlayer.posX;
        double dz = p.posZ - mc.thePlayer.posZ;
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double diff = Math.abs(wrapAngle((float) (targetYaw - mc.thePlayer.rotationYaw)));
        return diff > 90.0;
    }

    private boolean canCrit() {
        return !mc.thePlayer.onGround
            && mc.thePlayer.motionY < 0
            && !mc.thePlayer.isInWater()
            && !mc.thePlayer.isOnLadder()
            && !mc.thePlayer.isSprinting()
            && !mc.thePlayer.isPotionActive(Potion.blindness);
    }

    private void faceEntity(EntityPlayer target) {
        double dx = target.posX - mc.thePlayer.posX;
        double dz = target.posZ - mc.thePlayer.posZ;
        double dy = (target.posY + target.getEyeHeight() * 0.9)
                  - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw   = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float targetPitch = (float) -(Math.atan2(dy, horizDist) * 180.0 / Math.PI);

        float maxStep = (float) AuraSettings.rotationSpeed;
        float yawDiff   = wrapAngle(targetYaw - mc.thePlayer.rotationYaw);
        float pitchDiff = targetPitch - mc.thePlayer.rotationPitch;

        mc.thePlayer.rotationYaw   += Math.max(-maxStep, Math.min(maxStep, yawDiff));
        mc.thePlayer.rotationPitch += Math.max(-maxStep, Math.min(maxStep, pitchDiff));

        while (mc.thePlayer.rotationYaw > 180)  mc.thePlayer.rotationYaw -= 360;
        while (mc.thePlayer.rotationYaw < -180) mc.thePlayer.rotationYaw += 360;
    }

    private float wrapAngle(float angle) {
        while (angle > 180)  angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}
