package me.saiintbrisson.minecraft;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;

public class VirtualView {

    protected final ViewItem[] items;

    public VirtualView(ViewItem[] items) {
        this.items = items;
    }

    /**
     * Returns all registered {@link ViewItem}s.
     */
    public ViewItem[] getItems() {
        return items;
    }

    /**
     * Returns a {@link ViewItem} that is in the specified slot or {@code null} if not defined.
     *
     * @param slot the item slot.
     */
    public ViewItem getItem(int slot) {
        return items[slot];
    }

    /**
     * Returns the number of the first slot available or not.
     */
    public int getFirstSlot() {
        return 0;
    }

    /**
     * Returns the number of the last slot available or not.
     */
    public int getLastSlot() {
        return items.length - 1;
    }

    /**
     * Registers a {@link ViewItem} in the specified slot.
     *
     * @param slot the item slot.
     */
    public ViewItem slot(int slot) {
        final int max = getLastSlot() + 1;
        if (slot > max)
            throw new IllegalArgumentException("Slot exceeds the inventory limit (expected: < " + max + ", given: " + slot + ").");

        return items[slot] = new ViewItem(slot);
    }

    /**
     * Registers a {@link ViewItem} with a {@link ItemStack} in the specified slot.
     *
     * @param slot the item slot.
     * @param item the item to be set.
     */
    public ViewItem slot(int slot, ItemStack item) {
        return slot(slot).withItem(item);
    }

    /**
     * Registers a {@link ViewItem} in the specified row and column.
     *
     * @param row    the item slot row.
     * @param column the item slot column.
     */
    public ViewItem slot(int row, int column) {
        return slot((Math.max((row - 1), 0) * 9) + Math.max((column - 1), 0));
    }

    /**
     * Registers a {@link ViewItem} with a {@link ItemStack} in the specified row and column.
     *
     * @param row    the item slot row.
     * @param column the item slot column.
     * @param item   the item to be set.
     */
    public ViewItem slot(int row, int column, ItemStack item) {
        return slot(row, column).withItem(item);
    }

    /**
     * Registers a {@link ViewItem} in the first slot.
     *
     * @see #getFirstSlot()
     */
    public ViewItem firstSlot() {
        return slot(getFirstSlot());
    }

    /**
     * Registers a {@link ViewItem} with a {@link ItemStack} in the first slot.
     *
     * @param item the item to be set.
     * @see #getFirstSlot()
     */
    public ViewItem firstSlot(ItemStack item) {
        return slot(getFirstSlot(), item);
    }

    /**
     * Registers a {@link ViewItem} in the last slot.
     *
     * @see #getLastSlot()
     */
    public ViewItem lastSlot() {
        return slot(getLastSlot());
    }

    /**
     * Registers a {@link ViewItem} with a {@link ItemStack} in the last slot.
     *
     * @param item the item to be set.
     * @see #getLastSlot()
     */
    public ViewItem lastSlot(ItemStack item) {
        return slot(getLastSlot(), item);
    }

    /**
     * Render all items in this view to the specified context.
     *
     * @param context the target context.
     */
    public void render(ViewContext context) {
        Preconditions.checkNotNull(context, "Context cannot be null.");

        for (int i = 0; i < items.length; i++) {
            render(context, i);
        }
    }

    public void render(ViewContext context, int slot) {
        final ViewItem item = resolve(context, slot);
        if (item == null)
            return;

        render(context, item, slot);
    }

    /**
     * Renders a {@link ViewItem} for the specified context.
     *
     * @param context the target context.
     * @param slot    the slot that the item will be rendered.
     */
    public void render(ViewContext context, ViewItem item, int slot) {
        Preconditions.checkNotNull(item, "Render item cannot be null");

        final ItemStack fallback = item.getItem();
        if (item.getRenderHandler() != null) {
            final ViewSlotContext render = context instanceof ViewSlotContext ?
                    (ViewSlotContext) context :
                    new DelegatedViewContext(context, slot, fallback);
            item.getRenderHandler().handle(render);
            if (render.hasChanged()) {
                context.getInventory().setItem(slot, render.getItem());
                return;
            }
        }

        if (fallback == null)
            throw new IllegalArgumentException("No item were provided and the rendering function was not defined at slot " + slot + ".");

        context.getInventory().setItem(slot, fallback);
    }

    /**
     * Updates the specified {@link ViewContext} according to this view.
     *
     * @param context the target context.
     */
    public void update(ViewContext context) {
        Preconditions.checkNotNull(context, "Context cannot be null");
        for (int i = 0; i < items.length; i++) {
            update(context, i);
        }
    }

    /**
     * Updates only one {@link ViewItem} in that view to the specified {@link ViewContext}.
     *
     * @param context the target context.
     * @param slot    the slot that the item will be updated.
     */
    public void update(ViewContext context, int slot) {
        Preconditions.checkNotNull(context, "Context cannot be null");

        final ViewItem item = resolve(context, slot);
        if (item == null)
            return;

        if (item.getUpdateHandler() != null) {
            final ViewSlotContext update = context instanceof ViewSlotContext ?
                    (ViewSlotContext) context :
                    new DelegatedViewContext(context, slot, item.getItem());

            item.getUpdateHandler().handle(update);
            render(update, item, slot);
            return;
        }

        // update handler can be used as a void function so
        // we must fallback to the render handler to update the item
        render(context, item, slot);
    }

    private ViewItem resolve(ViewContext context, int slot) {
        if (this instanceof ViewContext)
            throw new IllegalArgumentException("Context can't resolve items itself");

        final ViewItem item = items[slot];
        if (item == null) {
            final ViewItem virtual = context.getItem(slot);
            if (virtual == null)
                return null;

            return virtual;
        }

        return item;
    }

}