package com.ciap.mc.block.entity;

import com.ciap.mc.item.ModItemRegistry;
import com.ciap.mc.recipe.PressRecipe;
import com.ciap.mc.screen.PressScreenHandler;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PressBlockEntity extends SimpleOilMachine {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    protected final PropertyDelegate propertyDelegate;

    public static final int OIL_SUBTRACT = 5;

    public static final int MAX_OIL = 3 * 1000;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_INPUT_ITEM = 1;
    public static final int SLOT_RESULT = 2;
    public static final int SLOT_OIL_INSERT = 3;
    public static final int SLOT_OIL_EXTRACT = 4;

    private int progress = 0;
    private int maxProgress = 60;
    private int maxFuelTime = 0;
    private int fuelTime = 0;

    public boolean working;

    public PressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityRegistry.PRESS, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0: return PressBlockEntity.this.fuelTime;
                    case 1: return PressBlockEntity.this.maxFuelTime;
                    case 2: return PressBlockEntity.this.progress;
                    case 3: return PressBlockEntity.this.maxProgress;
                    case 4: return PressBlockEntity.this.oilAmount;
                    default: return 0;
                }
            }
            public void set(int index, int value) {
                switch(index) {
                    case 0: PressBlockEntity.this.fuelTime = value; break;
                    case 1: PressBlockEntity.this.maxFuelTime = value; break;
                    case 2: PressBlockEntity.this.progress = value; break;
                    case 3: PressBlockEntity.this.maxProgress = value; break;
                    case 4: PressBlockEntity.this.oilAmount = value; break;
                }
            }
            public int size() {
                return 5;
            }
        };
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
    @Override
    public Text getDisplayName() {
        return Text.translatable("text.press.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new PressScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    private boolean consumeFuel() {
        if(!getStack(0).isEmpty()) {
            this.fuelTime = FuelRegistry.INSTANCE.get(this.removeStack(0, 1).getItem());
            this.maxFuelTime = this.fuelTime;
            return true;
        }
        return false;
    }

    private static boolean isConsumingFuel(PressBlockEntity entity) {
        return entity.fuelTime > 0;
    }

    @Override
    public int getStolOilExtract() {
        return SLOT_OIL_EXTRACT;
    }

    @Override
    public int getStolOilInsert() {
        return SLOT_OIL_INSERT;
    }

    public static void tick(World world, BlockPos pos, BlockState state, PressBlockEntity entity) {
        if (world.isClient()) {
            return;
        }

        if (isConsumingFuel(entity)) {
            entity.fuelTime --;
        }

        entity.tickOil();

        if(hasRecipe(entity) && hasEnoughOil(entity) && !world.isReceivingRedstonePower(pos)) {
            if (!isConsumingFuel(entity)) {
                if (!entity.consumeFuel()) {
                    entity.reset(state);
                    return;
                }
            }

            if (!entity.working) {
                entity.working = true;
                world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
            }
            entity.progress++;
            if(entity.progress > entity.maxProgress) {
                craftItem(entity, state);
            }
        } else {
            entity.reset(state);
        }
    }

    private static void craftItem(PressBlockEntity entity, BlockState state) {
        World world = entity.world;
        SimpleInventory recipeInventory = getRecipeInventory(entity);

        Optional<PressRecipe> match = world.getRecipeManager()
                .getFirstMatch(PressRecipe.TypeSerializer.INSTANCE, recipeInventory, world);

        if(match.isPresent()) {
            entity.removeStack(SLOT_INPUT_ITEM,1);
            entity.setStack(SLOT_RESULT, new ItemStack(match.get().getOutput().getItem(),
                    entity.getStack(SLOT_RESULT).getCount() + match.get().getOutput().getCount()));
            entity.oilAmount -= OIL_SUBTRACT;
            entity.reset(state, hasRecipe(entity));
        }
    }

    private void reset(BlockState state) {
        reset(state, false);
    }
    private void reset(BlockState state, boolean isStillWorking) {
        boolean wasWorking = this.working;
        this.progress = 0;
        this.working = isStillWorking || false;
        if (wasWorking && !isStillWorking) world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    }

    private static SimpleInventory getRecipeInventory(PressBlockEntity entity) {
        SimpleInventory inventory = new SimpleInventory(1);
        inventory.setStack(0, entity.getStack(SLOT_INPUT_ITEM));
        return inventory;
    }

    private static boolean hasEnoughOil(PressBlockEntity entity) {
        return entity.oilAmount >= OIL_SUBTRACT;
    }

    private static boolean hasRecipe(PressBlockEntity entity) {
        World world = entity.world;
        SimpleInventory recipeInventory = getRecipeInventory(entity);

        Optional<PressRecipe> match = world.getRecipeManager()
                .getFirstMatch(PressRecipe.TypeSerializer.INSTANCE, recipeInventory, world);

        if (!match.isPresent() || match.isEmpty()) {
            return false;
        }

        return match.isPresent() && canInsertItemIntoOutputSlot(entity, match.get().getOutput());
    }

    private static boolean canInsertItemIntoOutputSlot(PressBlockEntity entity, ItemStack output) {
        return (
                (entity.getStack(SLOT_RESULT).getItem() == output.getItem()  && entity.getStack(SLOT_RESULT).getCount() < entity.getStack(SLOT_RESULT).getMaxCount())
                        || entity.getStack(SLOT_RESULT).isEmpty()
        );
    }


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("press.progress", progress);
        nbt.putInt("press.maxProgress", maxProgress);
        nbt.putInt("press.fuelTime", fuelTime);
        nbt.putInt("press.maxFuelTime", maxFuelTime);
        nbt.putInt("press.oilAmount", oilAmount);
        nbt.putBoolean("press.working", working);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        progress = nbt.getInt("press.progress");
        maxProgress = nbt.getInt("press.maxProgress");
        fuelTime = nbt.getInt("press.fuelTime");
        maxFuelTime = nbt.getInt("press.maxFuelTime");
        oilAmount = nbt.getInt("press.oilAmount");
        working = nbt.getBoolean("press.working");
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
        if (FuelRegistry.INSTANCE.get(stack.getItem()) != null && slot == SLOT_FUEL) {
            return true;
        }
        if (Items.BUCKET.equals(stack.getItem()) && slot == SLOT_OIL_EXTRACT) {
            return true;
        }
        if (ModItemRegistry.OIL_BUCKET.equals(stack.getItem()) && slot == SLOT_OIL_INSERT) {
            return true;
        }
        return slot == SLOT_INPUT_ITEM;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return slot != SLOT_FUEL && slot != SLOT_INPUT_ITEM;
    }
}
