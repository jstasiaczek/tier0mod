package com.ciap.mc.renderer;

import com.ciap.mc.block.Breaker;
import com.ciap.mc.block.entity.TreeCutterBlockEntity;
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
public class TreeCutterBlockRenderer implements BlockEntityRenderer<TreeCutterBlockEntity> {
    // A jukebox itemstack
    private static ItemStack stack = new ItemStack(ModItemRegistry.SAW, 1);

    public TreeCutterBlockRenderer(BlockEntityRendererFactory.Context ctx) {}

    public void translateByFacing(MatrixStack matrices, Direction direction) {
        int facing = direction.getHorizontal();

        switch (facing) {
            case 0:
                matrices.translate(0.74, 0.6, 0.8);
                return;
            case 2:
                matrices.translate(0.27, 0.6, 0.2);
                return;
            case 3:
                matrices.translate(0.8, 0.6, 0.27);
                return;
            case 1:
                matrices.translate(0.27, 0.6, 0.74);
                return;
            default:
                matrices.translate(0.5, 0.5, 0.1);
                return;
        }
    }

    public int getAngleFromTime(long time) {
        int angle = (int)(time) % 180;
        if (angle > 45 && angle <= 90) {
            angle = 45 - (angle - 45);
        } else if (angle > 90 && angle <= 135) {
            angle = angle - 90;
        } else if (angle > 135) {
            angle = 45 - (angle - 135);
        }

        angle *=2;
        return angle;
    }

    @Override
    public void render(TreeCutterBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        BlockState blockState = blockEntity.getCachedState();
        Direction direction = blockState.get(Breaker.FACING);
        translateByFacing(matrices, direction);
        matrices.scale(1, 1, 1);

        long angle = blockEntity.getWorld().getTime() % 12;
        if (!blockEntity.working) {
            angle = 0;
        }

        int facing = direction.getHorizontal();


        RenderHelper.addWorkingParticle(blockEntity, blockEntity.working, 0.6);

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270 - (90 * direction.getHorizontal())));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle * 30));

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, light, overlay, matrices, vertexConsumers, 0);

        int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

        // Mandatory call after GL calls
        matrices.pop();
    }
}