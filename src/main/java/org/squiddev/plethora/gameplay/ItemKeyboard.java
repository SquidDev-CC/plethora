package org.squiddev.plethora.gameplay;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.computer.blocks.IComputerTile;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerRegistry;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.PeripheralItemFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.gameplay.neural.ItemComputerHandler;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.List;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.INSTANCE_ID;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.SESSION_ID;

public class ItemKeyboard extends ItemBase {
	public ItemKeyboard() {
		super("keyboard", 1);
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		return onItemUse(stack, world, player) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			if (world.isRemote) return EnumActionResult.SUCCESS;

			TileEntity tile = world.getTileEntity(pos);
			NBTTagCompound tag = getTag(stack);
			if (tile instanceof IComputerTile) {
				if (tile instanceof TileGeneric && !((TileGeneric) tile).isUsable(player, true)) {
					return false;
				}

				tag.setInteger("x", pos.getX());
				tag.setInteger("y", pos.getY());
				tag.setInteger("z", pos.getZ());
				tag.setInteger("dim", world.provider.getDimension());

				// We'll rebind this elsewhere.
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);

				player.addChatMessage(new TextComponentString(Helpers.translateToLocal("item.plethora.keyboard.bound")));
			} else if (tag.hasKey("x")) {
				player.addChatMessage(new TextComponentString(Helpers.translateToLocal("item.plethora.keyboard.cleared")));

				tag.removeTag("x");
				tag.removeTag("y");
				tag.removeTag("z");
				tag.removeTag("dim");
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);
			}

			player.inventory.markDirty();

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
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
						IComputer computer = ((IComputerTile) tile).getComputer();
						if (computer != null) {
							tag.setInteger(INSTANCE_ID, computer.getInstanceID());
							dirty = true;
						}
					}
				}
			}

			if (dirty && entity instanceof EntityPlayer) {
				((EntityPlayer) entity).inventory.markDirty();
			}
		}
	}

	private boolean onItemUse(ItemStack stack, World world, EntityPlayer player) {
		if (player.isSneaking()) return false;
		if (world.isRemote) return true;

		ServerComputer computer;
		NBTTagCompound tag = getTag(stack);
		if (tag.hasKey("x", 99)) {
			computer = getBlockComputer(ComputerCraft.serverComputerRegistry, tag);
		} else {
			ItemStack neural = NeuralHelpers.getStack(player);
			if (neural == null) return false;

			computer = ItemComputerHandler.getServer(neural, player, player.inventory);
		}

		if (computer == null) return false;
		GuiHandler.openKeyboard(player, world, computer);
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer playerIn) {
		onItemUse(stack, world, playerIn);
		return stack;
	}

	private static <T extends IComputer> T getBlockComputer(ComputerRegistry<T> registry, NBTTagCompound tag) {
		if (!tag.hasKey(SESSION_ID, 99) || !tag.hasKey(INSTANCE_ID, 99)) return null;

		int instance = tag.getInteger(INSTANCE_ID);
		return registry.get(instance);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> out, boolean um) {
		super.addInformation(stack, player, out, um);

		NBTTagCompound tag = getTag(stack);
		if (tag.hasKey("x", 99)) {
			ClientComputer computer = getBlockComputer(ComputerCraft.clientComputerRegistry, tag);
			String position = tag.getInteger("x") + ", " + tag.getInteger("y") + ", " + tag.getInteger("z");
			if (computer != null) {
				out.add(Helpers.translateToLocalFormatted("item.plethora.keyboard.binding", position));
			} else {
				out.add(Helpers.translateToLocalFormatted("item.plethora.keyboard.broken", position));
			}
		}
	}

	@Override
	public void init() {
		super.init();

		GameRegistry.addShapedRecipe(new ItemStack(this),
			"  M",
			"SSI",
			"SSS",
			'M', PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1),
			'S', Blocks.STONE,
			'I', Items.IRON_INGOT
		);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

		ItemStack stack = event.entityLiving.getHeldItem();
		if (stack == null || stack.getItem() != this) return;

		// Cancel all right clicks on blocks with this item
		if (!event.entityPlayer.isSneaking()) {
			event.setCanceled(true);
		}
	}
}
