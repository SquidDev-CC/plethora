---
layout: text
title: Moving items
---

## Transferring items and other things
No automation system is useful if you can't receive an input or output an, urm, output. This is where transfer locations
come in. Transfer locations are a remote location that a peripheral can interact with, sending items or other things.

### Transfer locations
To get started, wrap an inventory such as a chest and call the `.getTransferLocations()` method. This will print out a
list of strings of "places" which it can send items to. Let's go through the different categories of string you might
see.

The most simple is `self`. This is simply a reference to the current object. This is useful if you want to transfer an
item from one slot in an inventory to another slot in the same inventory.

The next, pretty obvious, list is the cardinal directions: `up`, `down`, `north`, `south`, `east` and `west`. You might
not see all of these, depending on whether there is a tile entity on that side. Using these allows you to transfer items
to adjacent objects.

A similar list is `up_side`, `down_side`, etc... This might be useful if you want to access a specific side of an
inventory (such as the bottom of a furnace).

You might also see a list of numbers. These correspond to the slots in the inventory which have an item in. By
transferring into one of these slots you transfer into that item's internal storage. This allows you to insert items
into backpacks or the like.

One final list will only appear if your peripheral is connected to a modem and you
have [CCTweaks](https://minecraft.curseforge/projects/cctweaks) installed. This will list all other peripherals on a
network, allowing item transfer across a wired network.

### Chaining locations
If needed, you can chain transfer locations together with a dot (`.`). For instance, to access the left side of the
inventory above you, you would use `up.left_side`. If you need to list the "child" transfer locations, you can pass the
"path" to `.getTransferLocations()`:

```lua
chest.getTransferLocations()

{ "up", "east", "up_side", "down_side", "0", "1", ... }

chest.getTransferLocations("up")

{ "down", "up_side", "down_side", ... }
```

### Transferring items
Well, let's try to move some items about. To do this we'll want to use `.pushItems()` and `.pullItems()`. Let's look at
the documentation for these:

```lua
chest.getDocs("pushItems")
"function(to:string, fromSlot:int[, limit:int][, toSlot:int]):int -- Push items from this inventory to another inventory. Returns the amount transferred."

chest.getDocs("pullItems")
"function(from:string, fromSlot:int[, limit:int][, toSlot:int]):int -- Pull items to this inventory from another inventory. Returns the amount transferred."
```

As you can see, they are pretty much identical, just with different directions. Let's try them out:

```lua
-- First push all the items into the upper chest
chest.pushItems("up", 1)

-- And pull them back down again
chest.pullItems("up", 1)
```

You can, of course, substitute "up" for any transfer location, including chained transfer locations:

```lua
-- Pull from the bottom side above. Were this a furnace, this would be the output side.
chest.pullItems("up.bottom_side", 1)
```
