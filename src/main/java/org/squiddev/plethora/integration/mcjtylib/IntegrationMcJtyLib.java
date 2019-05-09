package org.squiddev.plethora.integration.mcjtylib;

import mcjty.lib.McJtyLib;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.blocks.GenericItemBlock;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;
import org.squiddev.plethora.utils.EntityPlayerDummy;
import org.squiddev.plethora.utils.WorldDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Injects(McJtyLib.PROVIDES)
public final class IntegrationMcJtyLib {
	private IntegrationMcJtyLib() {
	}

	/*
	 * MEMO To test McJtyLib and dependent integrations, Forge must be set to AT LEAST 14.23.5.2800
	 */

	public static final IMetaProvider<GenericTileEntity> META_GENERIC_TILE = new BasicMetaProvider<GenericTileEntity>() {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull GenericTileEntity context) {
			Map<String, Object> out = new HashMap<>(5);

			if (GeneralConfig.manageOwnership) {
				//TODO Do we want to expose the UUID, if set, regardless of the owner's name?
				// That is, how closely do we want to mimic the results of `mcjty.lib.tileentity.GenericTileEntity.addProbeInfo`
				String ownerName = context.getOwnerName();
				if (ownerName != null && !ownerName.isEmpty()) out.put("ownerName", ownerName);

				UUID owner = context.getOwnerUUID();
				if (owner != null) out.put("ownerUUID", owner.toString());

				int securityChannel = context.getSecurityChannel();
				if (securityChannel != -1) out.put("securityChannel", securityChannel);
			}

			out.put("infusion", context.getInfused());

			//REFINE Do we want to provide the max infusion on each `GenericTileEntity`,
			// provide it via a method, or provide `getInfusedFactor` as a percentage?
			out.put("infusionMax", GeneralConfig.maxInfuse);

			return out;
		}

		@Nonnull
		@Override
		public GenericTileEntity getExample() {
			GenericTileEntity tile = new GenericTileEntity();
			tile.setInfused(5);
			tile.setOwner(new EntityPlayerDummy(WorldDummy.INSTANCE));

			return tile;
		}
	};

	//Based on the code in `mcjty.lib.blocks.GenericBlock.intAddInformation`
	public static final IMetaProvider<ItemStack> META_GENERIC_ITEM_BLOCK = new ItemStackMetaProvider<GenericItemBlock>(
		GenericItemBlock.class
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ItemStack stack, @Nonnull GenericItemBlock item) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null) return Collections.emptyMap();

			Map<String, Object> out = new HashMap<>(6);

			if (GeneralConfig.manageOwnership) {
				//TODO Do we want to expose the UUID, if set, regardless of the owner's name?
				// That is, how closely do we want to mimic the results of `mcjty.lib.tileentity.GenericBlock.intAddInformation`
				if (nbt.hasKey("owner", Constants.NBT.TAG_STRING)) out.put("ownerName", nbt.getString("owner"));

				if (nbt.hasKey("idM", Constants.NBT.TAG_LONG) && nbt.hasKey("idL", Constants.NBT.TAG_LONG)) {
					UUID owner = new UUID(nbt.getLong("idM"), nbt.getLong("idL"));
					out.put("ownerUUID", owner.toString());
				}

				if (nbt.hasKey("secChannel", Constants.NBT.TAG_INT)) {
					int securityChannel = nbt.getInteger("secChannel");
					if (securityChannel != -1) out.put("securityChannel", securityChannel);
				}
			}

			if (nbt.hasKey("Energy", Constants.NBT.TAG_LONG)) out.put("energy", nbt.getLong("Energy"));

			//Unfortunately, `mcjty.lib.blocks.GenericItemBlock` doesn't expose the base Block,
			// so we can't call `isInfusable`; this results in the 'infusion level' showing on ItemBlocks
			// that can't be infused...
			if (nbt.hasKey("infused", Constants.NBT.TAG_INT)) out.put("infusion", nbt.getInteger("infusion"));

			//REFINE See task in META_GENERIC_TILE
			out.put("infusionMax", GeneralConfig.maxInfuse);

			return out;
		}

		//TODO Determine if we can implement `getExample` without having to set the Block and Tile
		// Preferably without manually constructing the NBT...
	};

}
