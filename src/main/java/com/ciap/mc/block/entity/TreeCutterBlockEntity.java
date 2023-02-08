package com.ciap.mc.block.entity;

import com.ciap.mc.block.TreeCutter;
import com.ciap.mc.item.ModItemRegistry;
import com.ciap.mc.screen.HarvesterScreenHandler;
import com.ciap.mc.utils.ImplementedInventory;
import com.ciap.mc.utils.TagUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeCutterBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {
    public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
    private int destroyProgress = 0;
    public boolean working = false;
    public int breakerId = NEXT_BREAKER_ID.incrementAndGet();
    public int currentIdx = 0;
    private int cokeChunksAmount = 0;

    private boolean treeIsFound = false;
    public static final int CHUNK_MAX_COUNT = 300;
    private static final int TICKS_RUNNING_COST = 100 * 10;
    private static final int OUTPUT_INVENTORY_COUNT = 9;


    private float leftHardness = 0.0f;
    protected final PropertyDelegate propertyDelegate;

    public static final float DESTROY_POWER = 0.01f;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);

    public TreeCutterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityRegistry.TREE_CUTTER, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0: return TreeCutterBlockEntity.this.currentIdx;
                    case 1: return TreeCutterBlockEntity.this.cokeChunksAmount;
                    default: return 0;
                }
            }
            public void set(int index, int value) {
                switch(index) {
                    case 0: TreeCutterBlockEntity.this.currentIdx = value; break;
                    case 1: TreeCutterBlockEntity.this.cokeChunksAmount = value; break;
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
        return Text.translatable("text.tree_cutter.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new HarvesterScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    private Tree getTree(BlockPos pos) {
        Tree tree = new Tree();
        tree.findTree(this.world, pos);
        return tree;
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

    public static void tick(World world, BlockPos pos, BlockState state, TreeCutterBlockEntity entity) {
        if (world.isClient()) {
            return;
        }
        Direction direction = world.getBlockState(pos).get(TreeCutter.FACING);
        BlockPos targetBlockPos = pos.offset(direction);

        Boolean isPosEmpty = world.isAir(targetBlockPos) || world.isWater(targetBlockPos);

        entity.fuelTick();

        if (world.getTime() % TICKS_RUNNING_COST == 0 && entity.cokeChunksAmount > 0) {
            entity.cokeChunksAmount--;
        }

        if (isPosEmpty || !Tree.isLog(world, targetBlockPos) || entity.cokeChunksAmount <= 0) {
            entity.reset(state);
            world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, -1);
            return;
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
            Tree tree = entity.getTree(targetBlockPos);
            int logsCount = tree.countLogs();
            entity.cokeChunksAmount = entity.cokeChunksAmount > logsCount ? entity.cokeChunksAmount - logsCount : 0;
            List<ItemStack> drop = tree.cutTree(world);
            for (ItemStack stack : drop) {
                entity.insertToInv(stack);
            }
            world.playSound(null, targetBlockPos, blockToDestroy.getBlock().getDefaultState().getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1f, 1f);
            entity.tryToSeed(targetBlockPos);
            entity.reset(state);
        } else {
            if (!entity.working) {
                entity.working = true;
                world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
            }
            world.setBlockBreakingInfo(entity.breakerId, targetBlockPos, entity.destroyProgress);
        }
    }

    private boolean tryToSeed(BlockPos targetBlock) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack item = inventory.get(i);
            if (TagUtils.containsItem(item.getItem(), ItemTags.SAPLINGS)) {
                Block block = Block.getBlockFromItem(item.getItem());
                world.setBlockState(targetBlock, block.getDefaultState());
                world.playSound(null, targetBlock, block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1f, 1f);
                inventory.set(i, item.getCount() > 1 ? new ItemStack(item.getItem(), item.getCount() -1) : ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    private void insertToInv(ItemStack stack) {
        for (int i = 0; i < OUTPUT_INVENTORY_COUNT; i++) {
            ItemStack invStack = inventory.get(i);
            if (invStack.isEmpty()) {
                inventory.set(i, stack.copy());
                break;
            }
            if (invStack.getItem().equals(stack.getItem()) && (invStack.getCount() + stack.getCount()) <= invStack.getMaxCount()) {
                inventory.set(i, new ItemStack(stack.getItem(), invStack.getCount() + stack.getCount()));
                break;
            }
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
        nbt.putFloat("tree_cutter.leftHardness", leftHardness);
        nbt.putInt("tree_cutter.destroyProgress", destroyProgress);
        nbt.putInt("tree_cutter.currentIdx", currentIdx);
        nbt.putInt("tree_cutter.cokeChunksAmount", cokeChunksAmount);
        nbt.putBoolean("tree_cutter.working", working);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        leftHardness = nbt.getFloat("tree_cutter.leftHardness");
        destroyProgress = nbt.getInt("tree_cutter.destroyProgress");
        currentIdx = nbt.getInt("tree_cutter.currentIdx");
        cokeChunksAmount = nbt.getInt("tree_cutter.cokeChunksAmount");
        working = nbt.getBoolean("tree_cutter.working");
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
