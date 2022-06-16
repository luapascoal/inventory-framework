package me.saiintbrisson.minecraft;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class BukkitViewSlotContext extends AbstractViewSlotContext {

	BukkitViewSlotContext(ViewItem backingItem, @NotNull BaseViewContext parent) {
		super(backingItem, parent);
	}

	@Override
	public final int getSlot() {
		return getBackingItem().getSlot();
	}

	@Override
	public Player getPlayer() {
		return BukkitViewer.toPlayerOfContext(this);
	}

	@Override
	public @NotNull InventoryClickEvent getClickOrigin() {
		throwNotClickContext();
		return null;
	}

	@Override
	public boolean isLeftClick() {
		throwNotClickContext();
		return false;
	}

	@Override
	public boolean isRightClick() {
		throwNotClickContext();
		return false;
	}

	@Override
	public boolean isMiddleClick() {
		throwNotClickContext();
		return false;
	}

	@Override
	public boolean isShiftClick() {
		throwNotClickContext();
		return false;
	}

	@Override
	public boolean isKeyboardClick() {
		throwNotClickContext();
		return false;
	}

	@Override
	public boolean isOnEntityContainer() {
		throwNotClickContext();
		return false;
	}

	@Override
	public boolean isOutsideClick() {
		throwNotClickContext();
		return false;
	}

	@Contract(value = " -> fail", pure = true)
	private void throwNotClickContext() {
		throw new IllegalStateException(
			"Cannot retrieve click information from a non-click context"
		);
	}

}