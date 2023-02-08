package com.ciap.mc.renderer;

import com.ciap.mc.block.entity.OilPressBlockEntity;
import com.ciap.mc.item.ModItemRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class OilPressBlockRenderer implements BlockEntityRenderer<OilPressBlockEntity> {
    private static ItemStack stack = new ItemStack(ModItemRegistry.OIL_PRESS_PISTON, 1);

    public OilPressBlockRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(OilPressBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        matrices.scale(2, 2, 2);
        long tick = blockEntity.getWorld().getTime() % 100;

        double pos = (double)tick / 200;
        if (tick >= 50) {
            pos = (double) (50 - (tick - 50)) / 200;
        }
        if (!blockEntity.working) {
            pos = 0.0d;
        }

        RenderHelper.addWorkingParticle(blockEntity, blockEntity.working, 0.8);
        matrices.translate(0.223, 0.4 + pos, 0.223);

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);

        int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);
        matrices.pop();
    }
}