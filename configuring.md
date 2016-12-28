---
title: Configuring Plethora
layout: text
---

## Configuring the mod
Plethora provides a series of configuration options to allow mod pack developers or server owners to restrict and
balance the use of specific features of the pack. Most options are documented within the `plethora-core.cfg` file,
though I will go into more detail here.

### Blacklisting mod integration
If you wish to disable integration with a specific mod then you should add its ID to the `blacklist.blacklistMods`
list. This will disable all converters, meta providers, transfer locations and methods. For more fine grained control,
see [Blacklisting providers](#blacklisting-providers).

It is important to note that this will not prevent interacting with that mod, just all mod-specific providers. For
instance, disabling Forestry would still allow you to inspect the inventory of an Apiary, but not view the genome of the
bees inside.

If you wish to disable wrapping a mod's peripherals, see [the next section](#blacklisting-peripherals).

### Blacklisting peripherals
You can disable the wrapping of particular tile entities through the `blacklist.blacklistTileEntities` section. You can
specify a fully qualified class name (such as `net.minecraft.tileentity.TileEntityChest` to prevent wrapping chests) or
an entire package (such as `net.minecraft.` to prevent wrapping vanilla tile entities). Note that the trailing `.` is
required to disambiguate between package and class blacklists.

### Blacklisting providers
It is possible to blacklist any "provider" registered with Plethora including transfer locations, meta providers and
methods. Like peripherals, blacklists can be in several forms:

 - Package prefixes such as `org.squiddev.plethora.integration.vanilla.`. This will blacklist every entry in that
   package and child packages. Take note of the trailing `.`.
 - Class names such as `org.squiddev.plethora.integration.vanilla.method.MethodsInventory`. This will blacklist the
   class, or all methods inside that class.
 - Method names such as `org.squiddev.plethora.integration.vanilla.method.MethodsInventory#getItem`. This will blacklist
   one method.

You can get the name of methods by reading the `basecosts` section, or browsing the GitHub repository. You can also
enable the `testing.debug` option and copy the relevant names from the console output.

### Cost system
These options control various aspects of the default cost handler. You may wish
to [read about the cost system](cost-system.html) first. The options here are relatively self-explanatory:

 - `initial` controls the initial value the cost handler starts at. You can generally leave this at the same level as
   the limit, though you will want to make sure it does not exceed it.
 - `limit` controls the maximum energy the handler can hold. Making this higher will mean more methods can be called in
   a short space of time.
 - `regen` controls the rate at which energy regenerates. The lower this value, the more time the program will have to
   wait between calling methods.
 - `allowNegative` allows the handler to fall in to negative energies, falling back to a more traditional rate limiting
   system. This is explained in more detail in the cost system documentation.

### Base method costs
It is possible to set the cost that will be consumed every time a method is called. This can be used to restrict the
rate at which powerful or computationally expensive functions are called. For more information about the cost
system, [see here](cost-system.html).

This section contains an option for every method which is registered with Plethora, along with a description of what
that method does. Whilst each method has a default cost of 0, you can configure it to be whatever you feel is suitable.
