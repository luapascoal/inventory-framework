package me.devnatan.inventoryframework.context;

import static java.lang.String.format;
import static me.devnatan.inventoryframework.utils.SlotConverter.convertSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import me.devnatan.inventoryframework.InventoryFrameworkException;
import me.devnatan.inventoryframework.PlatformView;
import me.devnatan.inventoryframework.RootView;
import me.devnatan.inventoryframework.ViewConfig;
import me.devnatan.inventoryframework.ViewContainer;
import me.devnatan.inventoryframework.Viewer;
import me.devnatan.inventoryframework.component.ComponentFactory;
import me.devnatan.inventoryframework.component.ItemComponentBuilder;
import me.devnatan.inventoryframework.internal.LayoutSlot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

@SuppressWarnings("rawtypes")
public abstract class PlatformRenderContext<T extends ItemComponentBuilder<T, C>, C extends IFContext>
        extends PlatformConfinedContext implements IFRenderContext {

    // --- Must inherit from parent context ---
    private final UUID id;
    protected final PlatformView root;
    private final ViewConfig config;
    private final Map<String, Viewer> viewers;
    private final Object initialData;
    private final Viewer subject;

    // --- Inherited ---
    private final ViewContainer container;
    private boolean rendered;

    // --- Properties ---
    private final List<ComponentFactory> componentBuilders = new ArrayList<>();
    private final List<LayoutSlot> layoutSlots = new ArrayList<>();
    private BiFunction<Integer, Integer, ComponentFactory> availableSlotFactory;

    PlatformRenderContext(
            @NotNull UUID id,
            @NotNull PlatformView root,
            @NotNull ViewConfig config,
            @NotNull ViewContainer container,
            @NotNull Map<String, Viewer> viewers,
            Viewer subject,
            Object initialData) {
        this.id = id;
        this.root = root;
        this.config = config;
        this.container = container;
        this.viewers = viewers;
        this.subject = subject;
        this.initialData = initialData;
    }

    /**
     * Creates a new platform builder instance.
     *
     * @return A new platform builder instance.
     */
    // TODO use ElementFactory's `createBuilder` instead
    protected abstract T createBuilder();

    /**
     * Creates a new item builder without a specified slot.
     * <p>
     * This function is for creating items whose slot is set dynamically during item rendering.
     * <pre>{@code
     * unsetSlot().onRender(render -> {
     *     render.setItem(...);
     *     render.setSlot(...);
     * });
     * }</pre>
     *
     * <p><b><i> This API is experimental and is not subject to the general compatibility guarantees
     * such API may be changed or may be removed completely in any further release. </i></b>
     *
     * @return An item builder to configure the item.
     */
    @ApiStatus.Experimental
    public final T unsetSlot() {
        return createRegisteredBuilder();
    }

    /**
     * Adds an item to a specific slot in the context container.
     *
     * @param slot The slot in which the item will be positioned.
     * @return An item builder to configure the item.
     */
    public final @NotNull T slot(int slot) {
        return createRegisteredBuilder().withSlot(slot);
    }

    /**
     * Adds an item at the specific column and ROW (X, Y) in that context's container.
     *
     * @param row    The row (Y) in which the item will be positioned.
     * @param column The column (X) in which the item will be positioned.
     * @return An item builder to configure the item.
     */
    @NotNull
    public T slot(int row, int column) {
        checkAlignedContainerTypeForSlotAssignment();
        return createRegisteredBuilder()
                .withSlot(convertSlot(
                        row,
                        column,
                        getContainer().getRowsCount(),
                        getContainer().getColumnsCount()));
    }

    /**
     * Sets an item in the first slot of this context's container.
     *
     * @return An item builder to configure the item.
     */
    public @NotNull T firstSlot() {
        return createRegisteredBuilder().withSlot(getContainer().getFirstSlot());
    }

    /**
     * Sets an item in the last slot of this context's container.
     *
     * @return An item builder to configure the item.
     */
    public @NotNull T lastSlot() {
        return createRegisteredBuilder().withSlot(getContainer().getLastSlot());
    }

    /**
     * Adds an item in the next available slot of this context's container.
     *
     * @return An item builder to configure the item.
     */
    public @NotNull T availableSlot() {
        final T builder = createBuilder();
        availableSlotFactory =
                (index, slot) -> (ComponentFactory) builder.copy().withSlot(slot);
        return builder;
    }

    /**
     * Adds an item in the next available slot of this context's container.
     *
     * <pre>{@code
     * availableSlot((index, builder) -> builder.withItem(...));
     * }</pre>
     *
     * @param factory A factory to create the item builder to configure the item.
     *                The first parameter is the iteration index of the available slot.
     */
    public void availableSlot(@NotNull BiConsumer<Integer, T> factory) {
        availableSlotFactory = (index, slot) -> {
            final T builder = createBuilder();
            builder.withSlot(slot);
            factory.accept(index, builder);
            return (ComponentFactory) builder;
        };
    }

    /**
     * Defines the item that will represent a character provided in the context layout.
     *
     * @param character The layout character target.
     * @return An item builder to configure the item.
     */
    public @NotNull T layoutSlot(char character) {
        requireNonReservedLayoutCharacter(character);

        // TODO More detailed exception message
        final LayoutSlot layoutSlot = getLayoutSlots().stream()
                .filter(value -> value.getCharacter() == character)
                .findFirst()
                .orElseThrow(() -> new InventoryFrameworkException("Missing layout character: " + character));

        final T builder = createBuilder();
        final int elIndex = getLayoutSlots().indexOf(layoutSlot);
        getLayoutSlots().set(elIndex, layoutSlot.withFactory($ -> (ComponentFactory) builder));
        return builder;
    }

    /**
     * Defines the item that will represent a character provided in the context layout.
     *
     * <pre>{@code
     * layoutSlot('F', (index, builder) -> builder.withItem(...));
     * }</pre>
     *
     * @param character The layout character target.
     */
    public void layoutSlot(char character, @NotNull BiConsumer<Integer, T> factory) {
        requireNonReservedLayoutCharacter(character);

        // TODO More detailed exception message
        final LayoutSlot layoutSlot = getLayoutSlots().stream()
                .filter(value -> value.getCharacter() == character)
                .findFirst()
                .orElseThrow(() -> new InventoryFrameworkException("Missing layout character: " + character));

        final int elIndex = getLayoutSlots().indexOf(layoutSlot);
        getLayoutSlots().set(elIndex, layoutSlot.withFactory(index -> {
            final T builder = createBuilder();
            factory.accept(index, builder);
            return (ComponentFactory) builder;
        }));
    }

    /**
     * Creates a new platform builder instance and registers it.
     *
     * @return A new registered platform builder instance.
     */
    protected final T createRegisteredBuilder() {
        final T builder = createBuilder();
        componentBuilders.add((ComponentFactory) builder);
        return builder;
    }

    /**
     * Throws an {@link IllegalStateException} if container type is not aligned.
     */
    private void checkAlignedContainerTypeForSlotAssignment() {
        if (!getContainer().getType().isAligned())
            throw new IllegalStateException(String.format(
                    "Non-aligned container type %s cannot use row-column slots, use absolute %s instead",
                    getContainer().getType().getIdentifier(), "#slot(n)"));
    }

    /**
     * Checks if the character is a reserved layout character.
     *
     * @param character The character to be checked.
     * @throws IllegalArgumentException If the given character is a reserved layout character.
     */
    private void requireNonReservedLayoutCharacter(char character) {
        if (character == LayoutSlot.FILLED_RESERVED_CHAR)
            throw new IllegalArgumentException(format(
                    "The '%c' character cannot be used because it is only available for backwards compatibility. Please use another character.",
                    character));
    }

    @Override
    public final @NotNull UUID getId() {
        return id;
    }

    @Override
    public final @NotNull ViewContainer getContainer() {
        return container;
    }

    @Override
    public final @NotNull ViewConfig getConfig() {
        return config;
    }

    @Override
    public final @NotNull Map<String, Viewer> getIndexedViewers() {
        return viewers;
    }

    @Override
    public final Object getInitialData() {
        return initialData;
    }

    @Override
    public final Viewer getViewer() {
        return subject;
    }

    @Override
    public final @NotNull @UnmodifiableView List<ComponentFactory> getComponentFactories() {
        return Collections.unmodifiableList(componentBuilders);
    }

    @Override
    public final @NotNull List<LayoutSlot> getLayoutSlots() {
        return layoutSlots;
    }

    @Override
    public final void addLayoutSlot(@NotNull LayoutSlot layoutSlot) {
        layoutSlots.add(layoutSlot);
    }

    @Override
    public final BiFunction<Integer, Integer, ComponentFactory> getAvailableSlotFactory() {
        return availableSlotFactory;
    }

    @Override
    public final void closeForPlayer() {
        tryThrowDoNotWorkWithSharedContext("closeForEveryone()");
        super.closeForPlayer();
    }

    @Override
    public final void openForPlayer(@NotNull Class<? extends RootView> other) {
        tryThrowDoNotWorkWithSharedContext("openForEveryone(Class)");
        super.openForPlayer(other);
    }

    @Override
    public final void openForPlayer(@NotNull Class<? extends RootView> other, Object initialData) {
        tryThrowDoNotWorkWithSharedContext("openForEveryone(Class, Object)");
        super.openForPlayer(other, initialData);
    }

    @Override
    public final void updateTitleForPlayer(@NotNull String title) {
        tryThrowDoNotWorkWithSharedContext("updateTitleForEveryone(String)");
        super.updateTitleForEveryone(title);
    }

    @Override
    public final void resetTitleForPlayer() {
        tryThrowDoNotWorkWithSharedContext("resetTitleForEveryone()");
        super.resetTitleForPlayer();
    }

    @Override
    public final boolean isRendered() {
        return rendered;
    }

    /**
     * <b><i> This is an internal inventory-framework API that should not be used from outside of
     * this library. No compatibility guarantees are provided. </i></b>
     */
    @ApiStatus.Internal
    public final void setRendered() {
        this.rendered = true;
    }
}