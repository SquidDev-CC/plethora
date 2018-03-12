package org.squiddev.plethora.integration.refinedstorage;

import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemIdentity {
	private final ItemStack stack;

	public ItemIdentity(ItemStack stack) {
		this.stack = stack;
	}

	public ItemStack getStack() {
		return stack;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ItemIdentity that = (ItemIdentity) o;
		return equals(getStack(), that.getStack());
	}

	@Override
	public int hashCode() {
		return stack.getItem().hashCode() << 4 + stack.getItemDamage();
	}

	public static boolean equals(ItemStack left, ItemStack right) {
		return left.getItem() == right.getItem()
			&& left.getItemDamage() == right.getItemDamage()
			&& Objects.equals(left.getTagCompound(), right.getTagCompound())
			&& left.areCapsCompatible(right);
	}
}
