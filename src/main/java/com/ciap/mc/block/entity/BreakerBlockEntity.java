package com.ciap.mc.block.entity;

import com.ciap.mc.block.Breaker;
import com.ciap.mc.screen.BreakerScreenHandler;
import com.ciap.mc.utils.ImplementedInventory;
import net.fabricmc.fabric.api.registry.FuelRegistry;
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
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class BreakerBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {
    public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
    private int destroyProgress = 0;
    public boolean working = false;
    public int breakerId = NEXT_BREAKER_ID.incrementAndGet();

    private int maxFuelTime = 0;

    private int fuelTime = 0;

    private float leftHardness = 0.0f;
    protected final PropertyDelegate propertyDelegate;

    public static final float DESTROY_POWER = 0.01f;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public BreakerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityRegistry.BREAKER, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0: return BreakerBlockEntity.this.fuelTime;
                    case 1: return BreakerBlockEntity.this.maxFuelTime;
                    default: return 0;
                }
            }
            public void set(int index, int value) {
                switch(index) {
                    case 0: BreakerBlockEntity.this.fuelTime = value; break;
                    case 1: BreakerBlockEntity.this.maxFuelTime = value; break;
                }
            }
            public int size() {
                return 2;
            }
        };
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
    @Override
    public Text getDisplayName() {
        return Text.translatable("text.breaker.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BreakerScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    private boolean consumeFuel() {
        if(!getStack(0).isEmpty()) {
            this.fuelTime = FuelRegistry.INSTANCE.get(this.removeStack(0, 1).getItem());
            this.maxFuelTime = this.fuelTime;
            return true;
        }
        return false;
    }

    private static boolean isConsumingFuel(BreakerBlockEntity entity) {
        return entity.fuelTime > 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, BreakerBlockEntity entity) {
        if (world.isClient()) {
            return;
        }
        Direction direction = world.getBlockState(pos).get(Breaker.FACING);
        BlockPos targetBlockPos = pos.offset(direction);

        if (isConsumingFuel(entity)) {
            entity.fuelTime --;
        }

        if (world.isReceivingRedstonePower(pos)) {
            entity.reset(state);
            world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, -1);
            return;
        }

        Boolean isPosEmpty = world.isAir(targetBlockPos) || world.isWater(targetBlockPos);

        if (isPosEmpty) {
            entity.reset(state);
            world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, -1);
            return;
        }

        if (!isConsumingFuel(entity)) {
            if (!entity.consumeFuel()) {
                world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, -1);
                entity.reset(state);
                return;
            }
        }

        BlockState blockToDestroy = world.getBlockState(targetBlockPos);
        float hardness = blockToDestroy.getHardness(world, pos);

        if (hardness == -1 || hardness > 10) {
            entity.reset(state);
            world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, -1);
            return;
        } else if (hardness == 0.0f) {
            hardness = 0.1f;
        }

        if (entity.leftHardness == 0) {
            entity.leftHardness = hardness;
        }

        entity.leftHardness = entity.leftHardness - DESTROY_POWER;
        float calc = (hardness - entity.leftHardness) == 0 ? DESTROY_POWER : hardness - entity.leftHardness;
        entity.destroyProgress = Math.round(calc / hardness * 100) / 10;

        if (entity.leftHardness < 0.1) {
            world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, -1);
            world.setBlockState(targetBlockPos, Blocks.AIR.getDefaultState(), 3);
            Block.dropStacks(blockToDestroy, world, targetBlockPos);
            world.playSound(null, targetBlockPos, blockToDestroy.getBlock().getDefaultState().getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1f, 1f);
            entity.reset(state);
        } else {
            if (!entity.working) {
                entity.working = true;
                world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
            }
            world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, entity.destroyProgress);
        }
    }

    private void reset(BlockState state) {
        boolean wasWorking = this.working;
        this.leftHardness = 0;
        this.destroyProgress = 0;
        this.working = false;
        if (wasWorking) world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    }


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putFloat("breaker.leftHardness", leftHardness);
        nbt.putInt("breaker.destroyProgress", destroyProgress);
        nbt.putInt("breaker.fuelTime", fuelTime);
        nbt.putInt("breaker.maxFuelTime", maxFuelTime);
        nbt.putBoolean("breaker.working", working);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        leftHardness = nbt.getFloat("breaker.leftHardness");
        destroyProgress = nbt.getInt("breaker.destroyProgress");
        maxFuelTime = nbt.getInt("breaker.maxFuelTime");
        fuelTime = nbt.getInt("breaker.fuelTime");
        working = nbt.getBoolean("breaker.working");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
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
