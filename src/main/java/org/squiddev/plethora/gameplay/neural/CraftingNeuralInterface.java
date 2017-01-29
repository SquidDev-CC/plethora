package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import dan200.computercraft.shared.pocket.items.PocketComputerItemFactory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.registry.Registry;

import javax.annotation.Nonnull;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.COMPUTER_ID;

public class CraftingNeuralInterface extends ShapedOreRecipe {
	public CraftingNeuralInterface() {
		super(new ItemStack(Registry.itemNeuralInterface),
			"  G",
			"IPR",
			" GM",
			'G', "ingotGold",
			'I', "ingotIron",
			'R', "dustRedstone",
			'M', PeripheralItemFactory.create(PeripheralType.WiredModem, null, 1),
			'P', PocketComputerItemFactory.create(-1, null, ComputerFamily.Advanced, false)
		);
	}

	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
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
