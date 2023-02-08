package com.ciap.mc.renderer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RenderHelper {
    public static boolean isPaused() {
        return MinecraftClient.getInstance().isIntegratedServerRunning() && MinecraftClient.getInstance().isPaused();
    }
    public static void addWorkingParticle(BlockEntity blockEntity, boolean working, double height) {
        World world = blockEntity.getWorld();
        BlockPos bpos = blockEntity.getPos();


        double delta = world.getRandom().nextDouble();
        if (!isPaused() && (int)blockEntity.getWorld().getTime() % 20 == 0 && working) {
            world.addParticle(ParticleTypes.SMOKE, delta+bpos.getX(), height+bpos.getY(), delta+bpos.getZ(), 0.1, 0.1, 0.1);
        }
    }
}
