package org.squiddev.plethora.utils;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Helper methods for various things
 */
public class Helpers {
	/**
	 * Translate any variant of a string
	 *
	 * @param strings The strings to try to translate
	 * @return The first translateable string
	 */
	@SuppressWarnings("deprecation")
	public static String translateAny(String... strings) {
		return translateOrDefault(strings[strings.length - 1], strings);
	}

	/**
	 * Translate any variant of a string
	 *
	 * @param def     The fallback string
	 * @param strings The strings to try to translate
	 * @return The first translateable string or the default
	 */
	@SuppressWarnings("deprecation")
	public static String translateOrDefault(String def, String... strings) {
		for (String string : strings) {
			if (I18n.canTranslate(string)) return I18n.translateToLocal(string);
		}

		return def;
	}

	@SuppressWarnings("deprecation")
	public static String translateToLocal(String key) {
		return I18n.translateToLocal(key);
	}

	@SuppressWarnings("deprecation")
	public static String translateToLocalFormatted(String format, Object... args) {
		return I18n.translateToLocalFormatted(format, args);
	}

	public static void twoWayCrafting(ItemStack a, ItemStack b) {
		GameRegistry.addShapelessRecipe(a, b);
		GameRegistry.addShapelessRecipe(b, a);
	}

	/**
	 * Add a series of crafting recipes with positions swapped.
	 *
	 * @param output The output stack
	 * @param a      The first item to swap
	 * @param b      The second item to swap
	 * @param args   Args as passed to {@link GameRegistry#addRecipe(net.minecraft.item.crafting.IRecipe)}
	 */
	public static void alternateCrafting(ItemStack output, char a, char b, Object... args) {
		GameRegistry.addRecipe(output, args);

		if (args[0] instanceof String[]) {
			String[] inputs = (String[]) args[0];

			for (int i = 0; i < inputs.length; i++) {
				inputs[i] = swapCharacters(inputs[i], a, b);
			}
		} else {
			int i = 0;
			while (args[i] instanceof String) {
				args[i] = swapCharacters((String) args[i], a, b);
				++i;
			}
		}

		GameRegistry.addRecipe(output, args);
	}

	/**
	 * Swap two characters in a string
	 *
	 * @param word The string to swap
	 * @param a    First character
	 * @param b    Second character
	 * @return Swapped string
	 */
	public static String swapCharacters(String word, char a, char b) {
		StringBuilder builder = new StringBuilder(word.length());

		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (c == a) {
				c = b;
			} else if (c == b) {
				c = a;
			}
			builder.append(c);
		}
		return builder.toString();
	}

	public static int nextId(World world, String type) {
		return IDAssigner.getNextIDFromFile(new File(ComputerCraft.getWorldDir(world), "computer/lastid_" + type + ".txt"));
	}

	public static int nextId(World world, IPeripheral peripheral) {
		return nextId(world, peripheral.getType());
	}

	public static String snakeCase(String name) {
		return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
	}

	@SideOnly(Side.CLIENT)
	public static void setupModel(Item item, int damage, String name) {
		name = Plethora.RESOURCE_DOMAIN + ":" + snakeCase(name);

		net.minecraft.client.renderer.block.model.ModelResourceLocation res = new ModelResourceLocation(name, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, damage, res);
	}

	public static final Random RANDOM = new Random();

	public static void spawnItemStack(World world, double x, double y, double z, ItemStack stack) {
		float dX = RANDOM.nextFloat() * 0.8F + 0.1F;
		float dY = RANDOM.nextFloat() * 0.8F + 0.1F;
		float dZ = RANDOM.nextFloat() * 0.8F + 0.1F;

		EntityItem entity = new EntityItem(world, x + dX, y + dY, z + dZ, stack);

		float motion = 0.05F;
		entity.motionX = RANDOM.nextGaussian() * (double) motion;
		entity.motionY = RANDOM.nextGaussian() * (double) motion + 0.20000000298023224D;
		entity.motionZ = RANDOM.nextGaussian() * (double) motion;
		world.spawnEntityInWorld(entity);
	}

	@SideOnly(Side.CLIENT)
	private static ItemModelMesher mesher;

	@SideOnly(Side.CLIENT)
	public static ItemModelMesher getMesher() {
		ItemModelMesher mesher = Helpers.mesher;
		if (mesher == null) {
			mesher = Helpers.mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		}
		return mesher;
	}

	public static Set<String> getContainingMods(File file) {
		Set<String> modIds = Sets.newHashSet();

		for (ModContainer container : Loader.instance().getModList()) {
			if (container.getSource().equals(file)) {
				modIds.add(container.getModId());
			}
		}

		return modIds;
	}

	public static File getContainingJar(Class<?> klass) {
		String path = klass.getProtectionDomain().getCodeSource().getLocation().getPath();

		int bangIndex = path.indexOf("!");
		if (bangIndex >= 0) {
			path = path.substring(0, bangIndex);
		}

		URL url;
		try {
			url = new URL(path);
		} catch (MalformedURLException ignored) {
			return null;
		}

		File file;
		try {
			file = new File(url.toURI());
		} catch (URISyntaxException ignored) {
			file = new File(url.getPath());
		}

		return file;
	}

	public static boolean classBlacklisted(Iterable<String> blacklist, String name) {
		for (String prefix : blacklist) {
			if (prefix.endsWith(".")) {
				if (name.startsWith(prefix)) return true;
			} else if (name.equals(prefix) || name.startsWith(prefix + "$") || name.startsWith(prefix + "#")) {
				// Include exact classes, child classes (trailing $) or child methods (trailing #).
				return true;
			}
		}

		return false;
	}

	private static final Set<String> blacklistedMods = Sets.newHashSet();

	public static boolean modLoaded(String mod) {
		return (Loader.isModLoaded(mod) || ModAPIManager.INSTANCE.hasAPI(mod)) && !ConfigCore.Blacklist.blacklistMods.contains(mod) && !blacklistedMods.contains(mod);
	}

	public static void blacklistMod(String mod) {
		blacklistedMods.add(mod);
	}

	@Nonnull
	public static String getName(Entity entity) {
		String name = EntityList.getEntityString(entity);
		if (name == null) {
			if (entity instanceof EntityPlayer) {
				return entity.getName();
			} else if (entity.hasCustomName()) {
				return entity.getCustomNameTag();
			} else {
				return "unknown";
			}
		} else {
			return name;
		}
	}

	public static String getName(ItemStack stack) {
		String name = stack.getUnlocalizedName();

		if (!Strings.isNullOrEmpty(name)) {
			name = StringUtils.removeStart(name, "tile.");
			name = StringUtils.removeStart(name, "item.");
			name = StringUtils.removeEnd(name, ".name");
			return name;
		} else {
			return stack.getItem().getRegistryName().toString();
		}
	}

	@Nonnull
	public static String getName(Object owner) {
		if (owner == null) {
			return "null";
		} else if (owner instanceof ItemStack) {
			return getName((ItemStack) owner);
		} else if (owner instanceof Item) {
			return ((Item) owner).getRegistryName().toString();
		} else if (owner instanceof Entity) {
			return getName((Entity) owner);
		} else if (owner instanceof Block) {
			return ((Block) owner).getRegistryName().toString();
		} else if (owner instanceof TileEntity) {
			TileEntity te = (TileEntity) owner;

			String name = TileEntity.classToNameMap.get(te.getClass());
			if (name != null) return name;

			Block block = te.getBlockType();
			int meta = te.getBlockMetadata();

			if (block != null) {
				return getName(new ItemStack(block, 1, meta));
			} else {
				return te.getClass().getSimpleName();
			}
		} else {
			return owner.getClass().getSimpleName();
		}
	}

	@Nonnull
	public static String tryGetName(Object owner) {
		try {
			return getName(owner);
		} catch (Throwable e) {
			DebugLogger.error("Error getting data for " + owner.getClass().getName(), e);
			return owner.getClass().getSimpleName();
		}
	}

	public static boolean onEntityInteract(Item item, EntityPlayer player, Entity target, EnumHand hand) {
		if (!(target instanceof EntityLivingBase)) return false;

		ItemStack current = player.getHeldItem(hand);
		if (current == null || current.getItem() != item) return false;

		boolean result = item.itemInteractionForEntity(current, player, (EntityLivingBase) target, hand);

		if (current.stackSize <= 0) {
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			ForgeEventFactory.onPlayerDestroyItem(player, current, hand);
		}

		return result;
	}

	/**
	 * Assert that this class's specified target matches the appropriate generic parameter.
	 *
	 * This will error when in strict mode, as well as not accepting any valid but non-equal
	 * targets (such as subtypes).
	 *
	 * @param klass  The class to check
	 * @param target The specified target
	 * @param iface  The parent class or interface which takes the target as a type parameter.
	 * @throws IllegalArgumentException If no matching type can be found (and in strict mode).
	 */
	public static void assertTarget(Class<?> klass, Class<?> target, Class<?> iface) {
		// Gather
		TypeToken<?> initial = TypeToken.of(klass);

		TypeToken<?>.TypeSet collection = iface.isInterface()
			? initial.getTypes().interfaces()
			: initial.getTypes().classes();

		// Look for perfect matches first, otherwise build up a list of all args.
		List<Type> allTargets = null;
		for (TypeToken<?> tok : collection) {
			if (tok.getRawType() == iface) {
				if (tok.getType() instanceof ParameterizedType) {
					Type[] args = ((ParameterizedType) tok.getType()).getActualTypeArguments();
					if (args.length == 0) continue;

					Type arg = args[0];
					if (arg == target) return;

					if (allTargets == null) allTargets = Lists.newArrayList();
					allTargets.add(arg);
				}
			}
		}

		if (allTargets != null) {
			boolean valid = false;
			for (java.lang.reflect.Type arg : allTargets) {
				// If the type argument is a subtype then work correctly.
				if (arg instanceof Class<?> && ((Class<?>) arg).isAssignableFrom(target)) {
					DebugLogger.warn("Specified target as " + target.getName() + " but got superclass" + arg + " for " + klass.getName());
					valid |= !ConfigCore.Testing.strict;
				} else if (arg instanceof TypeVariable) {
					// Try to find something limited by this arg
					TypeVariable var = (TypeVariable) arg;
					for (Type bound : var.getBounds()) {
						if (bound instanceof Class<?> && ((Class<?>) bound).isAssignableFrom(target)) {
							DebugLogger.warn("Specified target as " + target.getName() + " but got generic parameter with matching bound " + var.getName() + " extends " + ((Class<?>) bound).getName() + " for " + klass.getName());
							valid |= !ConfigCore.Testing.strict;
						}
					}
				}
			}
			if (valid) return;
		}

		String message = "Annotation target " + target.getName() + " does not match type parameters";
		if (allTargets == null) {
			message += " (cannot find any type parameters)";
		} else {
			message += " (specified parameters are " + allTargets + ")";
		}

		if (ConfigCore.Testing.strict) {
			throw new IllegalStateException(message);
		} else {
			DebugLogger.error(message);
		}
	}

	public static int hashStack(ItemStack stack) {
		int hash = stack.getItem().hashCode() * 31 + stack.getItemDamage();
		if (stack.hasTagCompound()) hash = hash * 31 + stack.getTagCompound().hashCode();
		return hash;
	}

	public static int hashStacks(ItemStack[] stacks) {
		int hash = 0;
		for (ItemStack stack : stacks) {
			hash *= 31;
			if (stack != null) hash += Helpers.hashStack(stack);
		}
		return hash;
	}

	public static boolean isHolding(EntityLivingBase entity, Item item) {
		ItemStack left = entity.getHeldItem(EnumHand.MAIN_HAND);
		if (left != null && left.getItem() == item) return true;

		ItemStack right = entity.getHeldItem(EnumHand.OFF_HAND);
		if (right != null && right.getItem() == item) return true;

		return false;
	}

	public static boolean isHolding(EntityLivingBase entity, Item item, int damage) {
		ItemStack left = entity.getHeldItem(EnumHand.MAIN_HAND);
		if (left != null && left.getItem() == item && left.getItemDamage() == damage) return true;

		ItemStack right = entity.getHeldItem(EnumHand.OFF_HAND);
		if (right != null && right.getItem() == item && right.getItemDamage() == damage) return true;

		return false;
	}
}
