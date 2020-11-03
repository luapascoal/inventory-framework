package me.saiintbrisson.minecraft;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public class ViewSlotContext extends ViewContext {

    private final int slot;
    private InventoryClickEvent clickOrigin;
    private ItemStack item;
    private boolean changed;

    public ViewSlotContext(View view, Player player, Inventory inventory, int slot, ItemStack item) {
        super(view, player, inventory);
        this.slot = slot;
        this.item = item == null ? null : item.clone();
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
        changed = true;
    }

    public InventoryClickEvent getClickOrigin() {
        return clickOrigin;
    }

    void setClickOrigin(InventoryClickEvent clickOrigin) {
        this.clickOrigin = clickOrigin;
    }

    boolean hasChanged() {
        return changed;
    }

    public void updateSlot() {
        view.update(this, slot);
        System.out.println("[" + slot + "] [update]: call");
    }

    public <T> T getSlotData(String key) {
        return getSlotData(slot, key);
    }

    public <T> T getSlotData(String key, Supplier<T> defaultValue) {
        return getSlotData(slot, key, defaultValue);
    }

    public void setSlotData(String key, Object value) {
        setSlotData(slot, key, value);
    }

    public boolean hasSlotData(String key) {
        return hasSlotData(slot, key);
    }

    @Override
    public String toString() {
        return "ViewSlotContext{" +
                "slot=" + slot +
                ", clickOrigin=" + clickOrigin +
                ", item=" + item +
                ", changed=" + changed +
                "} " + super.toString();
    }

}