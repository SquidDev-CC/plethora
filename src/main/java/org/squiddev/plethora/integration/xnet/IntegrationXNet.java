package org.squiddev.plethora.integration.xnet;

import mcjty.xnet.blocks.cables.ConnectorTileEntity;
import mcjty.xnet.blocks.facade.FacadeItemBlock;
import mcjty.xnet.blocks.facade.IFacadeSupport;
import mcjty.xnet.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.squiddev.plethora.api.meta.BaseMetaProvider;
import org.squiddev.plethora.api.meta.BasicMetaProvider;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class IntegrationXNet {
	private IntegrationXNet() {
	}

	/*
	 *XNet
	 * Replicate the 'OC XNet Driver' behavior
	 * TODO Research how to use 'routing'; hard to provide meta for something you don't know yourself...
	 * TODO Determine how to expose the mimicked block for an IFacadeSupport
	 *
	 */

	/*
	 * Redstone Proxy - Not sure if this is of interest, as we have our own RS support
	 * Connector
	 *   Confirm that the energy capability is properly exposed (or if it should be...)
	 * Controller
	 * Router
	 * Wireless Router
	 */

	public static final IMetaProvider<IFacadeSupport> META_FACADE = new BaseMetaProvider<IFacadeSupport>() {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<IFacadeSupport> context) {
			IFacadeSupport target = context.getTarget();
			IBlockState mimicState = target.getMimicBlock();

			//TODO As an alternative to this, the mimicked block is also exposed as part of the
			// IExtendedBlockState; see mcjty.xnet.blocks.cables.ConnectorBlock.getExtendedState
			// and mcjty.xnet.blocks.facade.FacadeBlock.getExtendedState

			//REFINE This has one minor edge case: new facades default to cobblestone,
			// BUT the mimicState isn't set until the facade is broken!
			return mimicState == null
				? Collections.emptyMap()
				: Collections.singletonMap("mimicState", context.makePartialChild(mimicState).getMeta());
		}
	};

	public static final IMetaProvider<ItemStack> META_FACADE_ITEM = new ItemStackContextMetaProvider<FacadeItemBlock>(
		FacadeItemBlock.class
	) {
		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, @Nonnull FacadeItemBlock item) {
			//REFINE Technically, we should check if the mimicked block is set (versus using the default)...
			// See the behavior in mcjty.xnet.blocks.facade.FacadeItemBlock.getMimicBlock
			return Collections.singletonMap("mimicState", context.makePartialChild(FacadeItemBlock.getMimicBlock(context.getTarget())));
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			return new ItemStack(ModBlocks.facadeBlock);
		}
	};

	public static final IMetaProvider<ConnectorTileEntity> META_CONNECTOR= new BasicMetaProvider<ConnectorTileEntity>() {

		@Nonnull
		@Override
		public Map<String, ?> getMeta(@Nonnull ConnectorTileEntity context) {
			Map<String, Object> out = new HashMap<>();

			Map<String, Boolean> enabledMap = new HashMap<>(EnumFacing.VALUES.length);
			for (EnumFacing facing : EnumFacing.VALUES) {
				enabledMap.put(facing.toString(), context.isEnabled(facing));
			}
			out.put("isEnabled", enabledMap);

			out.put("connectorName", context.getConnectorName());

			return out;
		}

		@Nonnull
		@Override
		public ConnectorTileEntity getExample() {
			ConnectorTileEntity tile = new ConnectorTileEntity();

			tile.setConnectorName("Simple Example");
			//MEMO The tile's `enabled` mask defaults to 0x3f, or all enabled
			tile.setEnabled(EnumFacing.DOWN, false);
			tile.setEnabled(EnumFacing.SOUTH, false);

			return tile;
		}
	};
}
