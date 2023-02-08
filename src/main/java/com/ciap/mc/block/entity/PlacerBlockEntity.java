package com.ciap.mc.block.entity;

import com.ciap.mc.block.Placer;
import com.ciap.mc.screen.PlacerScreenHandler;
import com.ciap.mc.utils.ImplementedInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlacerBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private int delay = 0;
    private int delayProgress = 0;

    private boolean wasPowered = false;

    public PlacerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityRegistry.PLACER, pos, state);
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
    @Override
    public Text getDisplayName() {
        return Text.translatable("text.placer.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new PlacerScreenHandler(syncId, inv, this);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PlacerBlockEntity entity) {
        if (world.isClient()) {
            return;
        }
        if (entity.wasPowered && !world.isReceivingRedstonePower(pos)) {
            entity.wasPowered = false;
        }
        if (entity.delayProgress < entity.delay) {
            entity.delayProgress++;
            return;
        }
        if (!world.isReceivingRedstonePower(pos) || entity.wasPowered) {
            return;
        }
        entity.wasPowered = true;

        ItemStack itemStack = entity.getStack(0);
        Block block = Block.getBlockFromItem(itemStack.getItem());
        Direction direction = world.getBlockState(pos).get(Placer.FACING);
        BlockPos targetBlockPos = pos.offset(direction);
        Boolean isPosEmpty = world.isAir(targetBlockPos) || world.isWater(targetBlockPos);

        if (!isPosEmpty || Blocks.AIR.equals(block) || itemStack.getCount() == 0) {
            return;
        }

        world.setBlockState(targetBlockPos, block.getDefaultState());
        entity.setStack(0, new ItemStack(itemStack.getItem(), itemStack.getCount() - 1));
        world.playSound(null, targetBlockPos, block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1f, 1f);

        entity.delay = 10;
        entity.delayProgress = 0;
    }


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("placer.delay", delay);
        nbt.putBoolean("placer.wasPowered", wasPowered);
        nbt.putInt("placer.delayProgress", delayProgress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        delay = nbt.getInt("placer.delay");
        delayProgress = nbt.getInt("placer.delayProgress");
        wasPowered = nbt.getBoolean("placer.wasPowered");
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
