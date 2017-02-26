package org.squiddev.plethora.gameplay.keyboard;

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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IAttachable;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.method.IContextBuilder;
import org.squiddev.plethora.api.module.BasicModuleHandler;
import org.squiddev.plethora.api.module.IModuleAccess;
import org.squiddev.plethora.api.module.IModuleHandler;
import org.squiddev.plethora.gameplay.GuiHandler;
import org.squiddev.plethora.gameplay.ItemBase;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.neural.ItemComputerHandler;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;
import org.squiddev.plethora.gameplay.registry.Packets;
import org.squiddev.plethora.gameplay.registry.Registry;
import org.squiddev.plethora.utils.TinySlot;

import javax.annotation.Nonnull;
import java.util.List;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.INSTANCE_ID;
import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.SESSION_ID;

public class ItemKeyboard extends ItemBase {
	public ItemKeyboard() {
		super("keyboard", 1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		return onItemUse(stack, world, player);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() && !world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			NBTTagCompound tag = stack.getTagCompound();
			if (tile instanceof IComputerTile) {
				if (tile instanceof TileGeneric && !((TileGeneric) tile).isUsable(player, true)) {
					return false;
				}

				if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());

				tag.setInteger("x", pos.getX());
				tag.setInteger("y", pos.getY());
				tag.setInteger("z", pos.getZ());
				tag.setInteger("dim", world.provider.getDimensionId());

				// We'll rebind this elsewhere.
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);

				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.plethora.keyboard.bound")));
			} else if (tag != null && tag.hasKey("x")) {
				player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("item.plethora.keyboard.cleared")));

				tag.removeTag("x");
				tag.removeTag("y");
				tag.removeTag("z");
				tag.removeTag("dim");
				tag.removeTag(SESSION_ID);
				tag.removeTag(INSTANCE_ID);
			}

			// Clear the tag compound: pocket upgrades check this when determining if something can be stacked
			// so we want the default to be null.
			if (tag != null && tag.hasNoTags()) stack.setTagCompound(null);

			player.inventory.markDirty();

			return true;
		}

		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, world, entity, itemSlot, isSelected);
		if (world.isRemote) return;

		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("x", 99)) {
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
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("x", 99)) {
			computer = getBlockComputer(ComputerCraft.serverComputerRegistry, tag);
		} else {
			TinySlot slot = NeuralHelpers.getSlot(player);
			if (slot == null) return false;

			computer = ItemComputerHandler.getServer(slot.getStack(), player, slot);
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

		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null && tag.hasKey("x", 99)) {
			ClientComputer computer = getBlockComputer(ComputerCraft.clientComputerRegistry, tag);
			String position = tag.getInteger("x") + ", " + tag.getInteger("y") + ", " + tag.getInteger("z");
			if (computer != null) {
				out.add(StatCollector.translateToLocalFormatted("item.plethora.keyboard.binding", position));
			} else {
				out.add(StatCollector.translateToLocalFormatted("item.plethora.keyboard.broken", position));
			}
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return KeyboardModule.INSTANCE;
	}

	@Override
	public void preInit() {
		super.preInit();

		ClientKeyListener listener = new ClientKeyListener();
		MinecraftForge.EVENT_BUS.register(listener);
		Plethora.network.registerMessage(listener, ListenMessage.class, Packets.LISTEN_MESSAGE, Side.CLIENT);
		Plethora.network.registerMessage(new ServerKeyListener(), KeyMessage.class, Packets.KEY_MESSAGE, Side.SERVER);

		PlethoraAPI.instance().moduleRegistry().registerPocketUpgrade(new ItemStack(this));
	}

	@Override
	public void init() {
		super.init();

		GameRegistry.addShapedRecipe(new ItemStack(this),
			"  M",
			"SSI",
			"SSS",
			'M', PeripheralItemFactory.create(PeripheralType.WirelessModem, null, 1),
			'S', Blocks.stone,
			'I', Items.iron_ingot
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

	private static class KeyboardModule extends BasicModuleHandler implements ICapabilityProvider, IModuleHandler {
		public static KeyboardModule INSTANCE = new KeyboardModule();

		private KeyboardModule() {
			super(new ResourceLocation(Plethora.ID, "keyboard"), Registry.itemKeyboard);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == Constants.MODULE_HANDLER_CAPABILITY;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
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
