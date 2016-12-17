package org.squiddev.plethora.gameplay;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.blocks.IComputerTile;
import dan200.computercraft.shared.computer.core.ClientComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.client.gui.GuiCapture;
import org.squiddev.plethora.gameplay.neural.ItemComputerHandler;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;

import java.util.List;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.INSTANCE_ID;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.SESSION_ID;

public class ItemKeyboard extends ItemBase {
	public ItemKeyboard() {
		super("keyboard", 1);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() && !world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			NBTTagCompound tag = getTag(stack);
			if (tile instanceof IComputerTile) {
				tag.setInteger("x", pos.getX());
				tag.setInteger("y", pos.getY());
				tag.setInteger("z", pos.getZ());
				tag.setInteger("dim", world.provider.getDimensionId());

				// We'll rebind this elsewhere.
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);

				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.plethora.keyboard.bound")));
			} else if (tag.hasKey("x")) {
				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.plethora.keyboard.cleared")));

				tag.removeTag("x");
				tag.removeTag("y");
				tag.removeTag("z");
				tag.removeTag("dim");
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);
			}

			player.inventory.markDirty();

			return true;
		}

		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);
		if (world.isRemote) return;

		NBTTagCompound tag = getTag(stack);
		if (tag.hasKey("x", 99)) {
			int session = ComputerCraft.serverComputerRegistry.getSessionID();
			boolean dirty = false;
			if (tag.getInteger(SESSION_ID) != session) {
				tag.setInteger(SESSION_ID, session);
				tag.removeTag(INSTANCE_ID);
				dirty = true;
			}

			if (!tag.hasKey(INSTANCE_ID, 99) || !ComputerCraft.serverComputerRegistry.contains(tag.getInteger(INSTANCE_ID))) {
				World remote = DimensionManager.getWorld(tag.getInteger("dim"));
				BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
				if (remote != null && remote.isBlockLoaded(pos)) {
					TileEntity tile = remote.getTileEntity(pos);
					if (tile instanceof IComputerTile) {
						tag.setInteger(INSTANCE_ID, ((IComputerTile) tile).getComputer().getInstanceID());
						dirty = true;
					}
				}
			}

			if (dirty && entity instanceof EntityPlayer) {
				((EntityPlayer) entity).inventory.markDirty();
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer playerIn) {
		if (playerIn.isSneaking() || !world.isRemote) return stack;

		ClientComputer computer;
		NBTTagCompound tag = getTag(stack);
		if (tag.hasKey("x", 99)) {
			computer = getBlockComputer(tag);
		} else {
			ItemStack neural = NeuralHelpers.getStack(playerIn);
			if (neural == null) return stack;

			computer = ItemComputerHandler.getClient(neural);
		}

		if (computer == null) return stack;
		FMLClientHandler.instance().displayGuiScreen(playerIn, new GuiCapture(computer));

		return stack;
	}

	private static final ClientComputer getBlockComputer(NBTTagCompound tag) {
		if (!tag.hasKey(SESSION_ID, 99) || !tag.hasKey(INSTANCE_ID, 99)) return null;

		int instance = tag.getInteger(INSTANCE_ID);
		return ComputerCraft.clientComputerRegistry.get(instance);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);

		NBTTagCompound tag = getTag(stack);
		if (tag.hasKey("x", 99)) {
			ClientComputer computer = getBlockComputer(tag);
			String position = tag.getInteger("x") + ", " + tag.getInteger("y") + ", " + tag.getInteger("z");
			if (computer != null) {
				out.add(StatCollector.translateToLocalFormatted("item.plethora.keyboard.binding", position));
			} else {
				out.add(StatCollector.translateToLocalFormatted("item.plethora.keyboard.broken", position));
			}
		}
	}
}
