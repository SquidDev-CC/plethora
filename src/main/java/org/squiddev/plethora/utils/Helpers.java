package org.squiddev.plethora.utils;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.sun.istack.internal.Nullable;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.block.Block;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.squiddev.plethora.core.ConfigCore;
import org.squiddev.plethora.core.PlethoraCore;
import org.squiddev.plethora.gameplay.Plethora;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

/**
 * Helper methods for various things
 */
public final class Helpers {
	private Helpers() {
	}

	/**
	 * Translate any variant of a string
	 *
	 * @param strings The strings to try to translate
	 * @return The first translateable string
	 */
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
			if (net.minecraft.util.text.translation.I18n.canTranslate(string)) {
				return net.minecraft.util.text.translation.I18n.translateToLocal(string);
			}
		}

		return def;
	}

	@SuppressWarnings("deprecation")
	public static String translateToLocal(String key) {
		return net.minecraft.util.text.translation.I18n.translateToLocal(key);
	}

	@SuppressWarnings("deprecation")
	public static String translateToLocalFormatted(String format, Object... args) {
		return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(format, args);
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

	private static int nextId(World world, String type) {
		return IDAssigner.getNextIDFromFile("computer/lastid_" + type + ".txt");
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

		ModelResourceLocation res = new ModelResourceLocation(name, "inventory");
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
		world.spawnEntity(entity);
	}

	public static Set<String> getContainingMods(File file) {
		Set<String> modIds = new HashSet<>();

		for (ModContainer container : Loader.instance().getModList()) {
			if (container.getSource().equals(file)) {
				modIds.add(container.getModId());
			}
		}

		return modIds;
	}

	public static File getContainingJar(Class<?> klass) {
		String path = klass.getProtectionDomain().getCodeSource().getLocation().getPath();

		int bangIndex = path.indexOf('!');
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

	public static boolean blacklisted(@Nullable Iterable<String> blacklist, @Nonnull String name) {
		if (blacklist == null) return false;

		for (String prefix : blacklist) {
			if (!name.startsWith(prefix)) continue;

			// If they're equal, then it definitely matches.
			if (name.length() == prefix.length()) return true;

			// Match prefixes of "pkg.", "pkg.Class#" or "pkg.Class$"
			char last = prefix.charAt(prefix.length() - 1);
			if (last == '.' || last == '#' || last == '$') return true;

			// Determine if the next character is a separator, thus it definitely matches.
			char next = name.charAt(prefix.length());
			if (next == '.' || next == '#' || next == '$' || next == '(') return true;
		}

		return false;
	}

	private static final Set<String> blacklistedMods = new HashSet<>();

	public static boolean modLoaded(String mod) {
		return Loader.isModLoaded(mod) || ModAPIManager.INSTANCE.hasAPI(mod);
	}

	public static boolean modBlacklisted(String mod) {
		return (ConfigCore.Blacklist.blacklistMods != null && ConfigCore.Blacklist.blacklistMods.contains(mod)) || blacklistedMods.contains(mod);
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
		String name = stack.getTranslationKey();

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

			ResourceLocation name = TileEntity.REGISTRY.getNameForObject(te.getClass());
			if (name != null) return name.toString();

			Block block = te.getBlockType();
			int meta = te.getBlockMetadata();

			return block != null ? getName(new ItemStack(block, 1, meta)) : te.getClass().getSimpleName();
		} else {
			return owner.getClass().getSimpleName();
		}
	}

	@Nonnull
	public static String tryGetName(Object owner) {
		try {
			return getName(owner);
		} catch (Throwable e) {
			PlethoraCore.LOG.error("Error getting data for " + owner.getClass().getName(), e);
			return owner.getClass().getSimpleName();
		}
	}

	public static boolean onEntityInteract(Item item, EntityPlayer player, Entity target, EnumHand hand) {
		if (!(target instanceof EntityLivingBase)) return false;

		ItemStack current = player.getHeldItem(hand);
		if (current.isEmpty() || current.getItem() != item) return false;

		boolean result = item.itemInteractionForEntity(current, player, (EntityLivingBase) target, hand);

		if (current.getCount() <= 0) {
			player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
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
			for (Type arg : allTargets) {
				// If the type argument is a subtype then work correctly.
				if (arg instanceof Class<?> && ((Class<?>) arg).isAssignableFrom(target)) {
					PlethoraCore.LOG.warn("Specified target as " + target.getName() + " but got superclass" + arg + " for " + klass.getName());
					valid |= !ConfigCore.Testing.strict;
				} else if (arg instanceof TypeVariable) {
					// Try to find something limited by this arg
					TypeVariable<?> var = (TypeVariable) arg;
					for (Type bound : var.getBounds()) {
						if (bound instanceof Class<?> && ((Class<?>) bound).isAssignableFrom(target)) {
							PlethoraCore.LOG.warn("Specified target as " + target.getName() + " but got generic parameter with matching bound " + var.getName() + " extends " + ((Class<?>) bound).getName() + " for " + klass.getName());
							valid |= !ConfigCore.Testing.strict;
						}
					}
				}
			}
			if (valid) return;
		}

		String message = "Annotation target " + target.getName() + " does not match type parameters";
		message += allTargets == null ? " (cannot find any type parameters)" : " (specified parameters are " + allTargets + ")";

		if (ConfigCore.Testing.strict) {
			throw new IllegalStateException(message);
		} else {
			PlethoraCore.LOG.error(message);
		}
	}

	private static int hashStack(@Nonnull ItemStack stack) {
		int hash = stack.getItem().hashCode() * 31 + stack.getItemDamage();
		if (stack.hasTagCompound()) hash = hash * 31 + stack.getTagCompound().hashCode();
		return hash;
	}

	public static int hashStacks(Iterable<ItemStack> stacks) {
		int hash = 0;
		for (ItemStack stack : stacks) {
			hash *= 31;
			if (!stack.isEmpty()) hash += Helpers.hashStack(stack);
		}
		return hash;
	}

	public static boolean isHolding(EntityLivingBase entity, Item item) {
		ItemStack left = entity.getHeldItem(EnumHand.MAIN_HAND);
		if (!left.isEmpty() && left.getItem() == item) return true;

		ItemStack right = entity.getHeldItem(EnumHand.OFF_HAND);
		if (!right.isEmpty() && right.getItem() == item) return true;

		return false;
	}

	public static boolean isHolding(EntityLivingBase entity, Item item, int damage) {
		ItemStack left = entity.getHeldItem(EnumHand.MAIN_HAND);
		if (!left.isEmpty() && left.getItem() == item && left.getItemDamage() == damage) return true;

		ItemStack right = entity.getHeldItem(EnumHand.OFF_HAND);
		if (!right.isEmpty() && right.getItem() == item && right.getItemDamage() == damage) return true;

		return false;
	}

	/**
	 * An direct version of {@link java.util.stream.Stream#map(Function)} and collecting to a list. This allows us to
	 * presize the output.
	 *
	 * @param <T>  The elements in the input list
	 * @param <U>  The elements in the output list
	 * @param list The list to map over
	 * @param f    The function to transform the objects
	 * @return The
	 */
	@Nonnull
	public static <T, U> List<U> map(@Nonnull Collection<T> list, @Nonnull Function<T, U> f) {
		int size = list.size();
		switch (size) {
			case 0:
				return Collections.emptyList();
			case 1:
				return Collections.singletonList(f.apply(list.iterator().next()));
			default:
				return map(list, size, f);
		}
	}

	@Nonnull
	public static <T, U> List<U> map(@Nonnull Iterable<T> list, @Nonnull Function<T, U> f) {
		return map(list, 4, f);
	}

	@Nonnull
	public static <T, U> List<U> map(@Nonnull Iterable<T> list, int size, @Nonnull Function<T, U> f) {
		List<U> result = new ArrayList<>(size);
		for (T elem : list) result.add(f.apply(elem));
		return result;
	}
}
