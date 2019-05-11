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
			Map<String, Object> out = new HashMap<>(4);

			if (GeneralConfig.manageOwnership) {
				String ownerName = context.getOwnerName();
				UUID ownerID = context.getOwnerUUID();
				if (ownerName != null && !ownerName.isEmpty() && ownerID != null) {
					Map<String, Object> ownerMap = new HashMap<>(2);
					ownerMap.put("name", ownerName);
					ownerMap.put("id", ownerID.toString());
					out.put("owner", ownerMap);

					int securityChannel = context.getSecurityChannel();
					if (securityChannel != -1) out.put("securityChannel", securityChannel);
				}
			}

			out.put("infusion", context.getInfused());
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

			Map<String, Object> out = new HashMap<>(5);

			if (GeneralConfig.manageOwnership && nbt.hasKey("owner", Constants.NBT.TAG_STRING)) {
				String ownerName = nbt.getString("owner");
				if (!ownerName.isEmpty() && nbt.hasKey("idM", Constants.NBT.TAG_LONG) && nbt.hasKey("idL", Constants.NBT.TAG_LONG)) {
					Map<String, Object> ownerMap = new HashMap<>(2);
					ownerMap.put("name", ownerName);

					UUID ownerID = new UUID(nbt.getLong("idM"), nbt.getLong("idL"));
					ownerMap.put("id", ownerID.toString());

					out.put("owner", ownerMap);

					if (nbt.hasKey("secChannel", Constants.NBT.TAG_INT)) {
						int securityChannel = nbt.getInteger("secChannel");
						if (securityChannel != -1) out.put("securityChannel", securityChannel);
					}
				}
			}

			if (nbt.hasKey("Energy", Constants.NBT.TAG_LONG)) out.put("energy", nbt.getLong("Energy"));

			//TODO Find a way to only add the 'infused' properties to ItemBlocks that can actually be infused
			//Unfortunately, `mcjty.lib.blocks.GenericItemBlock` doesn't expose the base Block,
			// so we can't call `isInfusable`; this results in the 'infusion level' showing on ItemBlocks
			// that can't be infused...
			if (nbt.hasKey("infused", Constants.NBT.TAG_INT)) out.put("infusion", nbt.getInteger("infusion"));
			out.put("infusionMax", GeneralConfig.maxInfuse);

			return out;
		}

		//TODO Determine if we can implement `getExample` without having to set the Block and Tile
		// Preferably without manually constructing the NBT...
	};

}
