package com.ciap.mc.renderer;

import com.ciap.mc.block.Breaker;
import com.ciap.mc.block.entity.CrusherBlockEntity;
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
public class CrusherBlockRenderer implements BlockEntityRenderer<CrusherBlockEntity> {
    // A jukebox itemstack
    private static ItemStack stack = new ItemStack(ModItemRegistry.CRUSHING_WHEEL, 1);

    public CrusherBlockRenderer(BlockEntityRendererFactory.Context ctx) {}

    public void setTranslation(MatrixStack matrices, Direction direction) {
        setTranslation(matrices, direction, false);
    }
    public void setTranslation(MatrixStack matrices, Direction direction, boolean secondPos) {
        double odd = secondPos ? 0.3d : 0d;

        switch (direction) {
            case NORTH:
                matrices.translate(0.5, 0.65, 0.6-odd);
                break;
            case SOUTH:
                matrices.translate(0.5, 0.65, 0.3+odd);
                break;
            case EAST:
                matrices.translate(0.3 + odd, 0.65, 0.5);
                break;
            case WEST:
                matrices.translate(0.6 - odd, 0.65, 0.5);
                break;
        }
    }

    @Override
    public void render(CrusherBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        long angle = blockEntity.getWorld().getTime() % 18;
        if (!blockEntity.working) {
            angle = 0;
        }
        RenderHelper.addWorkingParticle(blockEntity, blockEntity.working, 0.6);

        BlockState blockState = blockEntity.getCachedState();
        Direction direction = blockState.get(Breaker.FACING);
        matrices.scale(1, 1, 1);
        setTranslation(matrices, direction);

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(360 - (90 * direction.getHorizontal())));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(angle * 20));




        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, light, overlay, matrices, vertexConsumers, 0);

        int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

        matrices.pop();
        matrices.push();
        matrices.scale(1, 1, 1);
        setTranslation(matrices, direction, true);

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(360 - (90 * direction.getHorizontal())));
        matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(angle * 20));




        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, light, overlay, matrices, vertexConsumers, 0);
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

        matrices.pop();
    }
}