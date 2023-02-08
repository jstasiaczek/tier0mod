package com.ciap.mc.block.entity;

import com.ciap.mc.Tier0Mod;
import com.ciap.mc.item.ModItemRegistry;
import com.ciap.mc.screen.HarvesterScreenHandler;
import com.ciap.mc.utils.ImplementedInventory;
import net.minecraft.block.*;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HarvesterBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {
    public boolean working = false;
    protected final PropertyDelegate propertyDelegate;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);
    private static final int OUTPUT_INVENTORY_COUNT = 9;
    private static final int TICKS_RUNNING_COST = 100 * 10;
    public static final int CHUNK_MAX_COUNT = 300;

    private static final int AREA_WIDTH = 7;

    public int currentIdx = 0;
    private int cokeChunksAmount = 0;

    public HarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityRegistry.HARVESTER, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0: return HarvesterBlockEntity.this.currentIdx;
                    case 1: return HarvesterBlockEntity.this.cokeChunksAmount;
                    default: return 0;
                }
            }
            public void set(int index, int value) {
                switch(index) {
                    case 0: HarvesterBlockEntity.this.currentIdx = value; break;
                    case 1: HarvesterBlockEntity.this.cokeChunksAmount = value; break;
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
        return Text.translatable("text.harvester.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new HarvesterScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    private void fuelTick() {
        if (this.cokeChunksAmount >= CHUNK_MAX_COUNT) {
            return;
        }
        ItemStack stack = this.getStack(9);
        if (!stack.getItem().equals(ModItemRegistry.COAL_COKE_CHUNKS)) {
            return;
        }
        int givenCount = stack.getCount() * 3;
        int leftCount = CHUNK_MAX_COUNT - this.cokeChunksAmount;
        if (givenCount <= leftCount) {
            this.setStack(9, ItemStack.EMPTY);
            this.cokeChunksAmount += givenCount;
            return;
        }
        if (givenCount > leftCount) {
            int oddStacks = leftCount /3;
            this.cokeChunksAmount += oddStacks * 3;
            this.setStack(9, new ItemStack(ModItemRegistry.COAL_COKE_CHUNKS, stack.getCount() - oddStacks));
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, HarvesterBlockEntity entity) {
        if (world.isClient()) {
            return;
        }
        BlockPos blockPos = getNextPos(pos, entity.currentIdx);
        entity.fuelTick();

        if (world.getTime() % TICKS_RUNNING_COST == 0 && entity.cokeChunksAmount > 0) {
            entity.cokeChunksAmount--;
        }

        if (entity.cokeChunksAmount > 0 && !entity.working) {
            entity.working = true;
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        } else if (entity.cokeChunksAmount <= 0 && entity.working) {
            entity.working = false;
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }

        if (world.getTime() % 100 == 0 && entity.working) {
            Tier0Mod.LOGGER.info("current index: "+entity.currentIdx);
            entity.processBlock(blockPos, world);
            entity.getNextIndex();
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    public static BlockPos getNextPos(BlockPos pos,  int index) {
        int x = index % AREA_WIDTH;
        int z = index / AREA_WIDTH;
        int size = AREA_WIDTH / 2;
        BlockPos corner = pos.add(-size, 0, -size);
        return corner.add(x, 0, z);
    }

    private void getNextIndex() {
        currentIdx++;
        if (currentIdx > AREA_WIDTH*AREA_WIDTH) {
            currentIdx = 0;
        }
    }

    public void processBlock(BlockPos blockPos, World world) {
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();

        if (block instanceof CropBlock) {
            processAgedCrop(blockState, blockPos, ((CropBlock) block).getAgeProperty(), ((CropBlock) block).getMaxAge(), 0);
        } else if (block instanceof NetherWartBlock) {
            processAgedCrop(blockState, blockPos, NetherWartBlock.AGE, 3, 0);
        } else if (block instanceof SweetBerryBushBlock) {
            processAgedCrop(blockState, blockPos, SweetBerryBushBlock.AGE, 3, 1);
        } else if (block instanceof CocoaBlock) {
            processAgedCrop(blockState, blockPos, CocoaBlock.AGE, 2, 0);
        } else if (block instanceof GourdBlock) {
            if (tryHarvestBlock(blockState, blockPos)) {
                world.breakBlock(blockPos, false);
            }
        } else if (block instanceof SugarCaneBlock
                || block instanceof CactusBlock
                || block instanceof BambooBlock
        ) {
            boolean breakBlocks = false;
            for (int y = 1; (blockState = world.getBlockState(blockPos.up(y))).getBlock() == block; y++) {
                if (y == 1) {
                    breakBlocks = tryHarvestBlock(blockState, blockPos.up(y));
                } else {
                    tryHarvestBlock(blockState, blockPos.up(y));
                }
                if (breakBlocks) world.breakBlock(blockPos.up(y), false);
            }
        }
    }

    private void processAgedCrop(BlockState blockState, BlockPos blockPos, IntProperty ageProperty, int maxAge, int newAge) {
        if (world == null) {
            return;
        }
        if (blockState.get(ageProperty) < maxAge) {
            return;
        }
        if (tryHarvestBlock(blockState, blockPos)) {
            world.setBlockState(blockPos, blockState.with(ageProperty, newAge), 2);
        }
    }

    private boolean tryHarvestBlock(BlockState blockState, BlockPos blockPos) {
        if (world == null) {
            return false;
        }
        if (insertToInventory(Block.getDroppedStacks(blockState, (ServerWorld) world, blockPos, null))) {
            this.cokeChunksAmount--;
            world.playSound(null, blockPos, blockState.getBlock().getDefaultState().getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1f, 1f);
            return true;
        }
        return false;
    }

    private boolean insertToInventory(List<ItemStack> stacks) {
        boolean result = true;
        List<ItemStack> dummyInv = new ArrayList<>();
        for (ItemStack stack : inventory) {
            dummyInv.add(stack.copy());
        }
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!tryInsertStack(stack, dummyInv)) {
                result = false;
            }

        }
        if (!result) return false;
        for (ItemStack stack : stacks) {
            insertToInv(stack);
        }
        return true;
    }

    private boolean insertToInv(ItemStack stack) {
        return tryInsertStack(stack, this.inventory);
    }

    private boolean tryInsertStack(ItemStack stack, List<ItemStack> inventory) {
        for (int i = 0; i < OUTPUT_INVENTORY_COUNT; i++) {
            ItemStack invStack = inventory.get(i);
            if (invStack.isEmpty()) {
                inventory.set(i, stack.copy());
                return true;
            }
            if (invStack.getItem().equals(stack.getItem()) && (invStack.getCount() + stack.getCount()) <= invStack.getMaxCount()) {
                inventory.set(i, new ItemStack(stack.getItem(), invStack.getCount() + stack.getCount()));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putBoolean("harvester.working", working);
        nbt.putInt("harvester.currentIdx", currentIdx);
        nbt.putInt("harvester.cokeChunksAmount", cokeChunksAmount);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        working = nbt.getBoolean("harvester.working");
        cokeChunksAmount = nbt.getInt("harvester.cokeChunksAmount");
        currentIdx = nbt.getInt("harvester.currentIdx");
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
