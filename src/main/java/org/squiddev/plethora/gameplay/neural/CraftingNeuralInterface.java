package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.registry.Registry;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.COMPUTER_ID;

public class CraftingNeuralInterface extends ShapedRecipes {
	public CraftingNeuralInterface() {
		super(3, 3, getRecipe(), new ItemStack(Registry.itemNeuralInterface));
	}

	private static ItemStack[] getRecipe() {
		ItemStack iron = new ItemStack(Items.iron_ingot);
		ItemStack gold = new ItemStack(Items.gold_ingot);
		ItemStack reds = new ItemStack(Items.redstone);
		ItemStack modm = PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1);
		ItemStack pock = PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, false);

		return new ItemStack[]{
			null, null, gold,
			iron, pock, reds,
			null, gold, modm
		};
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack output = getRecipeOutput().copy();

		ItemStack old = inv.getStackInRowAndColumn(1, 1);
		int id = ComputerCraft.Items.pocketComputer.getComputerID(old);
		String label = ComputerCraft.Items.pocketComputer.getLabel(old);

		// Copy across key properties
		NBTTagCompound tag = ItemBase.getTag(output);
		if (label != null) output.setStackDisplayName(label);
		if (id >= 0) tag.setInteger(COMPUTER_ID, id);

		return output;
	}
}
