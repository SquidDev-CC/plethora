package org.squiddev.plethora.gameplay.keyboard;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerRegistry;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.method.IContextBuilder;
import org.squiddev.plethora.api.module.BasicModuleHandler;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.neural.ItemComputerHandler;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;
import org.squiddev.plethora.gameplay.registry.Registration;
import org.squiddev.plethora.utils.Helpers;
import org.squiddev.plethora.utils.TinySlot;

import javax.annotation.Nonnull;
import java.util.List;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.INSTANCE_ID;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.SESSION_ID;

public class ItemKeyboard extends ItemBase {
	public ItemKeyboard() {
		super("keyboard", 1);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		return onItemUse(player.getHeldItem(hand), world, player);
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			if (world.isRemote) return EnumActionResult.SUCCESS;

			TileEntity tile = world.getTileEntity(pos);
			ItemStack stack = player.getHeldItem(hand);
			NBTTagCompound tag = stack.getTagCompound();
			if (tile instanceof TileComputerBase) {
				if (!((TileGeneric) tile).isUsable(player, true)) return EnumActionResult.FAIL;

				if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

				tag.setInteger("x", pos.getX());
				tag.setInteger("y", pos.getY());
				tag.setInteger("z", pos.getZ());
				tag.setInteger("dim", world.provider.getDimension());

				// We'll rebind this elsewhere.
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);

				player.sendStatusMessage(new TextComponentTranslation("item.plethora.keyboard.bound"), true);
			} else if (tag != null && tag.hasKey("x")) {
				tag.removeTag("x");
				tag.removeTag("y");
				tag.removeTag("z");
				tag.removeTag("dim");
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);

				player.sendStatusMessage(new TextComponentTranslation("item.plethora.keyboard.cleared"), true);
			}

			// Clear the tag compound: pocket upgrades check this when determining if something can be stacked
			// so we want the default to be null.
			if (tag != null && tag.isEmpty()) stack.setTagCompound(null);

			player.inventory.markDirty();

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);
		if (world.isRemote) return;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("x", NBT.TAG_ANY_NUMERIC)) {
			int session = ComputerCraft.serverComputerRegistry.getSessionID();
			boolean dirty = false;
			if (tag.getInteger(SESSION_ID) != session) {
				tag.setInteger(SESSION_ID, session);
				tag.removeTag(INSTANCE_ID);
				dirty = true;
			}

			if (!tag.hasKey(INSTANCE_ID, NBT.TAG_ANY_NUMERIC) || !ComputerCraft.serverComputerRegistry.contains(tag.getInteger(INSTANCE_ID))) {
				World remote = DimensionManager.getWorld(tag.getInteger("dim"));
				BlockPos pos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
				if (remote != null && remote.isBlockLoaded(pos)) {
					TileEntity tile = remote.getTileEntity(pos);
					if (tile instanceof TileComputerBase) {
						ServerComputer computer = ((TileComputerBase) tile).getServerComputer();
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

	private static EnumActionResult onItemUse(ItemStack stack, World world, EntityPlayer player) {
		if (player.isSneaking()) return EnumActionResult.PASS;
		if (world.isRemote) return EnumActionResult.SUCCESS;

		ServerComputer computer;
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("x", NBT.TAG_ANY_NUMERIC)) {
			computer = getBlockComputer(ComputerCraft.serverComputerRegistry, tag);
		} else {
			TinySlot slot = NeuralHelpers.getSlot(player);
			if (slot == null) return EnumActionResult.FAIL;

			computer = ItemComputerHandler.getServer(slot.getStack(), player, slot);
		}

		if (computer == null) return EnumActionResult.FAIL;
		GuiHandler.openKeyboard(player, world, computer);
		return EnumActionResult.SUCCESS;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		EnumActionResult result = onItemUse(stack, world, player);
		return ActionResult.newResult(result, stack);
	}

	private static <T extends IComputer> T getBlockComputer(ComputerRegistry<T> registry, NBTTagCompound tag) {
		if (!tag.hasKey(SESSION_ID, NBT.TAG_ANY_NUMERIC) || !tag.hasKey(INSTANCE_ID, NBT.TAG_ANY_NUMERIC)) return null;

		int instance = tag.getInteger(INSTANCE_ID);
		return registry.get(instance);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag flag) {
		super.addInformation(stack, world, out, flag);

		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("x", NBT.TAG_ANY_NUMERIC)) {
			ClientComputer computer = getBlockComputer(ComputerCraft.clientComputerRegistry, tag);
			String position = tag.getInteger("x") + ", " + tag.getInteger("y") + ", " + tag.getInteger("z");
			out.add(Helpers.translateToLocalFormatted(computer != null ? "item.plethora.keyboard.binding" : "item.plethora.keyboard.broken", position));
		}
	}

	@Nonnull
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return KeyboardModule.INSTANCE;
	}

	@SubscribeEvent
	public void onPlayerClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!Helpers.isHolding(event.getEntityLiving(), this)) return;

		// Cancel all right clicks on blocks with this item
		if (!event.getEntityLiving().isSneaking()) {
			event.setCanceled(true);
		}
	}

	private static final class KeyboardModule extends BasicModuleHandler {
		public static final KeyboardModule INSTANCE = new KeyboardModule();

		private KeyboardModule() {
			super(new ResourceLocation(Plethora.ID, "keyboard"), Registration.itemKeyboard);
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
			return capability == Constants.MODULE_HANDLER_CAPABILITY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
			return capability == Constants.MODULE_HANDLER_CAPABILITY ? (T) this : null;
		}

		@Override
		public void getAdditionalContext(@Nonnull final IModuleAccess access, @Nonnull IContextBuilder builder) {
			super.getAdditionalContext(access, builder);

			Object owner = access.getOwner();
			if (owner instanceof EntityPlayerMP) {
				final EntityPlayerMP player = (EntityPlayerMP) owner;
				builder.addAttachable(new IAttachable() {
					@Override
					public void attach() {
						ServerKeyListener.add(player, access);
					}

					@Override
					public void detach() {
						ServerKeyListener.remove(player, access);
					}
				});
			}
		}
	}
}
