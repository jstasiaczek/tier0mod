package com.ciap.mc.block;

import com.ciap.mc.block.entity.HarvesterBlockEntity;
import com.ciap.mc.block.entity.ModBlockEntityRegistry;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.ciap.mc.Tier0Mod.getIdentifier;

public class Harvester extends OrientableBlock implements BlockEntityProvider {
    public static Identifier ID = getIdentifier("harvester");

    public Harvester(Settings settings) {
        super(settings);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof HarvesterBlockEntity) {
                ItemScatterer.spawn(world, pos, (HarvesterBlockEntity)blockEntity);
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    /* BLOCK ENTITY */
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HarvesterBlockEntity(pos, state);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntityRegistry.HARVESTER, HarvesterBlockEntity::tick);
    }
}
