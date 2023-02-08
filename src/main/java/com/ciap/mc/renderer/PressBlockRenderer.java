package com.ciap.mc.renderer;

import com.ciap.mc.block.entity.PressBlockEntity;
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
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class PressBlockRenderer implements BlockEntityRenderer<PressBlockEntity> {
    private static ItemStack stack = new ItemStack(ModItemRegistry.PRESS_PISTON, 1);

    public PressBlockRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(PressBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.scale(1.95f, 1.95f, 1.95f);
        long tick = blockEntity.getWorld().getTime() % 60;

        ItemStack item = blockEntity.working ? blockEntity.getStack(PressBlockEntity.SLOT_INPUT_ITEM) : ItemStack.EMPTY;


        double pos = (double)tick / 200;
        if (tick >= 30) {
            pos = (double) (30 - (tick - 30)) / 200;
        }
        if (!blockEntity.working) {
            pos = 0.15d;
        }



        RenderHelper.addWorkingParticle(blockEntity, blockEntity.working, 0.9);
        matrices.translate(0.25, 0.4 + pos, 0.25);

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);

        int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);
        matrices.pop();
        matrices.push();
        matrices.scale(0.8f, 0.8f, 0.8f);
        if (item.getItem() instanceof BlockItem) {
            matrices.translate(0.55, 0.30, 0.55);
        } else {
            matrices.translate(0.55, 0.40, 0.55);
        }
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
        MinecraftClient.getInstance().getItemRenderer().renderItem(item, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);
        matrices.pop();
    }
}