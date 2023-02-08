package com.ciap.mc.renderer;

import com.ciap.mc.block.entity.HarvesterBlockEntity;
import com.ciap.mc.item.ModItemRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class HarvesterBlockRenderer implements BlockEntityRenderer<HarvesterBlockEntity> {
    private static ItemStack stack = new ItemStack(ModItemRegistry.HARVESTER_HEAD, 1);
    private static ItemStack hoe = new ItemStack(Items.IRON_HOE, 1);

    public HarvesterBlockRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(HarvesterBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        long angle = blockEntity.getWorld().getTime() % 360;
        if (!blockEntity.working) {
            angle = 0;
        }
        RenderHelper.addWorkingParticle(blockEntity, blockEntity.working, 0.5);

        BlockState blockState = blockEntity.getCachedState();
        matrices.scale(2f, 2f, 2f);
        matrices.translate(0.25, 0.5, 0.25);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(angle));

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);

        int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

        matrices.pop();
    }
}