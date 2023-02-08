package com.ciap.mc.block;

import com.ciap.mc.block.entity.BreakerBlockEntity;
import com.ciap.mc.block.entity.ModBlockEntityRegistry;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import static com.ciap.mc.Tier0Mod.getIdentifier;

public class Breaker extends OrientableBlock implements BlockEntityProvider {
    public static Identifier ID = getIdentifier("breaker");

    public Breaker(Settings settings) {
        super(settings);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BreakerBlockEntity) {
                ItemScatterer.spawn(world, pos, (BreakerBlockEntity)blockEntity);
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    private void resetDestroyedBlock(World world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity == null) {
            return;
        }

        Direction direction = world.getBlockState(pos).get(Placer.FACING);
        BlockPos targetBlockPos = pos.offset(direction);

        world.setBlockBreakingInfo(((BreakerBlockEntity)entity).breakerId, targetBlockPos, -1);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        resetDestroyedBlock(world, pos);
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        resetDestroyedBlock(world, pos);
        super.onDestroyedByExplosion(world, pos, explosion);
    }

    /* BLOCK ENTITY */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BreakerBlockEntity(pos, state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntityRegistry.BREAKER, BreakerBlockEntity::tick);
    }
}
