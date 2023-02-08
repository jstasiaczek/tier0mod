package com.ciap.mc.block.entity;

import com.ciap.mc.item.ModItemRegistry;
import com.ciap.mc.screen.OilPressScreenHandler;
import com.ciap.mc.tags.ModTags;
import com.ciap.mc.utils.TagUtils;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
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

public class OilPressBlockEntity extends SimpleOilMachine {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
    protected final PropertyDelegate propertyDelegate;

    public static final int ITEM_OIL_ADD = 20;
    public static final int MAX_OIL = 3 * 1000;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_INPUT_ITEM = 1;
    public static final int SLOT_OIL_INSERT = 2;
    public static final int SLOT_OIL_EXTRACT = 3;

    private int progress = 0;
    private int maxProgress = 100;
    private int maxFuelTime = 0;
    private int fuelTime = 0;

    // 0 - none
    // 1 - oil
    // 2 - uranium
    private int extractionType = 0;

    public boolean working;

    public OilPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityRegistry.OIL_PRESS, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0: return OilPressBlockEntity.this.fuelTime;
                    case 1: return OilPressBlockEntity.this.maxFuelTime;
                    case 2: return OilPressBlockEntity.this.progress;
                    case 3: return OilPressBlockEntity.this.maxProgress;
                    case 4: return OilPressBlockEntity.this.oilAmount;
                    case 5: return OilPressBlockEntity.this.extractionType;
                    default: return 0;
                }
            }
            public void set(int index, int value) {
                switch(index) {
                    case 0: OilPressBlockEntity.this.fuelTime = value; break;
                    case 1: OilPressBlockEntity.this.maxFuelTime = value; break;
                    case 2: OilPressBlockEntity.this.progress = value; break;
                    case 3: OilPressBlockEntity.this.maxProgress = value; break;
                    case 4: OilPressBlockEntity.this.oilAmount = value; break;
                    case 5: OilPressBlockEntity.this.extractionType = value; break;
                }
            }
            public int size() {
                return 6;
            }
        };
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
    @Override
    public Text getDisplayName() {
        return  extractionType == 2 ? Text.translatable("text.oil_press.title_uranium") : Text.translatable("text.oil_press.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new OilPressScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    private boolean consumeFuel() {
        if(!getStack(0).isEmpty()) {
            this.fuelTime = FuelRegistry.INSTANCE.get(this.removeStack(0, 1).getItem());
            this.maxFuelTime = this.fuelTime;
            return true;
        }
        return false;
    }

    private static boolean isConsumingFuel(OilPressBlockEntity entity) {
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

    protected void tickLiquid() {
        if (extractionType == 1) {
            this.tickOil();
        } else if (extractionType == 2){
            ItemStack stack = this.getStack(getStolOilExtract());
            if (!stack.equals(ItemStack.EMPTY) && stack.getCount() > 0 && stack.getItem().equals(ModItemRegistry.GREEN_LANTERN) && stack.getDamage() > 0) {
                if (stack.getDamage() >= oilAmount) {
                    stack.setDamage(stack.getDamage() - oilAmount);
                    oilAmount = 0;
                    this.setStack(getStolOilExtract(), stack);
                } else {
                    oilAmount = oilAmount - stack.getDamage();
                    stack.setDamage(0);
                    this.setStack(getStolOilExtract(), stack);
                }
            }
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, OilPressBlockEntity entity) {
        if (world.isClient()) {
            return;
        }

        if (isConsumingFuel(entity)) {
            entity.fuelTime --;
        }

        entity.tickLiquid();

        if (!hasRecipe(entity) || world.isReceivingRedstonePower(pos)) {
            entity.reset(state);
            return;
        }

        // TODO: consider checking item type ??

        if (!isConsumingFuel(entity)) {
            if (!entity.consumeFuel()) {
                entity.reset(state);
                return;
            }
        }

        if (entity.progress < entity.maxProgress) {
            if (!entity.working) {
                entity.working = true;
                world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
            }
            entity.progress++;
        } else {
            craftOutput(entity);
            entity.reset(state, hasRecipe(entity));
        }

    }

    private static void craftOutput(OilPressBlockEntity entity) {
        int oilAdd = ITEM_OIL_ADD * (entity.extractionType == 2 ? 2 : 1);
        entity.oilAmount += oilAdd;
        ItemStack item = entity.getStack(SLOT_INPUT_ITEM);
        if (item.getCount() > 1) {
            entity.setStack(SLOT_INPUT_ITEM, new ItemStack(item.getItem(), item.getCount() - 1));
        } else {
            entity.setStack(SLOT_INPUT_ITEM, ItemStack.EMPTY);
        }
    }

    private static Boolean hasRecipe(OilPressBlockEntity entity) {
        ItemStack itemToProcess = entity.getStack(SLOT_INPUT_ITEM);
        boolean canProcessUranium = (entity.extractionType == 0 || entity.extractionType == 2 || (entity.extractionType == 1 && entity.oilAmount == 0));
        boolean canProcessOil = (entity.extractionType == 0 || entity.extractionType == 1 || (entity.extractionType == 2 && entity.oilAmount == 0));

        if (entity.oilAmount >= MAX_OIL) {
            return false;
        }

        if (itemToProcess.getItem().equals(ModItemRegistry.URANIUM_SHARD) && canProcessUranium) {
            entity.extractionType = 2;
            return true;
        }
        if (TagUtils.containsItem(itemToProcess.getItem(), ModTags.Items.OIL_SOURCE) && canProcessOil) {
            entity.extractionType = 1;
            return true;
        }

        return false;
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


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("oil_press.progress", progress);
        nbt.putInt("oil_press.maxProgress", maxProgress);
        nbt.putInt("oil_press.fuelTime", fuelTime);
        nbt.putInt("oil_press.maxFuelTime", maxFuelTime);
        nbt.putInt("oil_press.oilAmount", oilAmount);
        nbt.putBoolean("oil_press.working", working);
        nbt.putInt("oil_press.extractionType", extractionType);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        progress = nbt.getInt("oil_press.progress");
        maxProgress = nbt.getInt("oil_press.maxProgress");
        fuelTime = nbt.getInt("oil_press.fuelTime");
        maxFuelTime = nbt.getInt("oil_press.maxFuelTime");
        oilAmount = nbt.getInt("oil_press.oilAmount");
        working = nbt.getBoolean("oil_press.working");
        extractionType = nbt.getInt("oil_press.extractionType");
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
