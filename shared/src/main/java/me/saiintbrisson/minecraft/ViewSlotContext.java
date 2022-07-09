package me.saiintbrisson.minecraft;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a context in which there is a specific slot related to it, the main context
 * encompasses the entire container in an actor's view, the ViewSlotContext encapsulates a context
 * for just one slot of a container.
 *
 * <p>Methods specific to a ViewSlotContext will only apply to that slot.
 *
 * @see ViewContext
 * @see ViewSlotClickContext
 * @see ViewSlotMoveContext
 * @see PaginatedViewSlotContext
 */
public interface ViewSlotContext extends ViewContext {

    /**
     * The parent context of this context.
     *
     * <p><b><i>This is an internal inventory-framework API that should not be used from outside of
     * this library. No compatibility guarantees are provided.</i></b>
     *
     * @return The parent context of this context.
     */
    @ApiStatus.Internal
    ViewContext getParent();

    /**
     * Returns the slot position of this context in the current container.
     *
     * @return The slot position of this context.
     */
    int getSlot();

    /**
     * Updates this slot context individually.
     *
     * <p>This method is a shortcut to {@link AbstractView#update(ViewContext, int)}.
     */
    void updateSlot();

    /**
     * The item tied to this context.
     *
     * <p>For example, if it is a click context it will be the item that was clicked. If it is a
     * rendering context it will be the item being rendered.
     *
     * @return The item tied to this context.
     * @deprecated Use {@link #getItemWrapper()} instead.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.5.4")
    ItemStack getItem();

    /**
     * Returns the wrapper containing the item related to this context.
     *
     * <p><b><i>This is an internal inventory-framework API that should not be used from outside of
     * this library. No compatibility guarantees are provided.</i></b>
     *
     * @return The current item wrapper.
     */
    @ApiStatus.Internal
    @NotNull
    ItemWrapper getItemWrapper();

    /**
     * Defines a new item for this context, triggering an {@link
     * AbstractVirtualView#inventoryModificationTriggered() inventory modification}.
     *
     * <p>If you need to change the item partially use {@link #updateItem(Consumer)}.
     *
     * @param item The new item that'll be set.
     */
    void setItem(@Nullable Object item);

    /**
     * Defines a new item for this context, triggering an {@link
     * AbstractVirtualView#inventoryModificationTriggered() inventory modification}.
     *
     * <p>If you need to change the item partially use {@link #updateItem(Consumer)}.
     *
     * @param item The new item that'll be set.
     * @return This context.
     */
    ViewSlotContext withItem(@Nullable Object item);

    /**
     * Applies a patch to the current item.
     *
     * <p>This method should be used when only a partial modification is required to be applied to
     * the item, triggering an {@link AbstractVirtualView#inventoryModificationTriggered() inventory
     * modification}.
     *
     * <p>If you need to update the item completely use {@link #setItem(Object)}.
     *
     * @param updater The update function.
     */
    void updateItem(Consumer<ItemWrapper> updater);

    /**
     * Whether this context originated from an interaction coming from the actor's container and not
     * from the view's container.
     *
     * @return If this context originated from the actor's container
     */
    boolean isOnEntityContainer();

    /**
     * Checks if the item in this context has been changed.
     *
     * <p><b><i>This is an internal inventory-framework API that should not be used from outside of
     * this library. No compatibility guarantees are provided.</i></b>
     *
     * @return If the item in this context has been changed.
     */
    @ApiStatus.Internal
    boolean hasChanged();

    /**
     * Marks this context as changed.
     *
     * <p>Improperly changing this property can cause unexpected side effects.
     *
     * <p><b><i>This is an internal inventory-framework API that should not be used from outside of
     * this library. No compatibility guarantees are provided.</i></b>
     *
     * @param changed If the context item was changed.
     */
    @ApiStatus.Internal
    void setChanged(boolean changed);

    /**
     * Returns the value of a user-defined property for the item of this slot context or throws an
     * exception if the property has not been set.
     *
     * @param key The property key.
     * @param <T> The property value type.
     * @return This item.
     * @throws NoSuchElementException If the property has not been set.
     */
    <T> T data(@NotNull String key);

    /**
     * Converts this context to a pagination context.
     *
     * <p>It only works if the view that originated this context is a paginated view, throwing an
     * IllegalStateException if the root of this context is not paginated.
     *
     * @param <T> The pagination item type.
     * @return This context as a PaginatedViewContext.
     * @throws IllegalStateException If the root of this context is not paginated.
     */
    @Override
    <T> PaginatedViewContext<T> paginated();
}