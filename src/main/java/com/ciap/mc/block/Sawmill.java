package com.ciap.mc.block;

import com.ciap.mc.block.entity.ModBlockEntityRegistry;
import com.ciap.mc.block.entity.SawmillBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Sawmill extends OrientableBlock {

    public Sawmill(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SawmillBlockEntity(pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SawmillBlockEntity) {
                ItemScatterer.spawn(world, pos, (SawmillBlockEntity) blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntityRegistry.SAWMILL, SawmillBlockEntity::tick);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (world.isClient() || !(entity instanceof LivingEntity)) {
            super.onSteppedOn(world, pos, state, entity);
            return;
        }
        LivingEntity livingEntity = ((LivingEntity) entity);
        SawmillBlockEntity blockEntity = ((SawmillBlockEntity) world.getBlockEntity(pos));
        if (blockEntity != null && blockEntity.working) {
            livingEntity.damage(DamageSource.GENERIC, 1f);
        }

        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    private static VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);
}