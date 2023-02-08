package com.ciap.mc.renderer;

import com.ciap.mc.block.Breaker;
import com.ciap.mc.block.entity.SawmillBlockEntity;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class SawmillBlockRenderer implements BlockEntityRenderer<SawmillBlockEntity> {
    private static ItemStack stack = new ItemStack(ModItemRegistry.SAW, 1);

    public SawmillBlockRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(SawmillBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        long angle = blockEntity.getWorld().getTime() % 12;
        if (!blockEntity.working) {
            angle = 0;
        }
        RenderHelper.addWorkingParticle(blockEntity, blockEntity.working, 0.5);

        BlockState blockState = blockEntity.getCachedState();
        Direction direction = blockState.get(Breaker.FACING);
        matrices.scale(1, 1, 1);
        matrices.translate(0.5, 0.5, 0.5);

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(360 - (90 * direction.getHorizontal())));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle * 30));




        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, light, overlay, matrices, vertexConsumers, 0);

        int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

        matrices.pop();
    }
}