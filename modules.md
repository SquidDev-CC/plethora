---
layout: text
title: Manipulating modules
---

## Manipulating modules
Modules are a way of exposing additional functionality to a computer. Generally this comes in the form of additional
methods, but it may also add extra metadata providers, expanding the capabilities of other modules.

### Discovering modules
Unlike peripherals, modules come in item form and so cannot be placed in world. Instead you should use a
Manipulator. This comes in two variants: Mk I and Mk II. The former can hold one module and the latter 5. Craft either
one of these and place it next to a computer. Then grab a module, we're going to use an entity sensor in this tutorial.

If you hover over the centre point of the manipulator you should see an outline of where the module can go.

![](/images/manipulator-without.png "A manipulator before putting the module in")

Right click to insert the module into the manipulator. You can right click with an empty hand to remove it again.

![](/images/manipulator-with.png "A manipulator with the module inserted")

No we've got the module in there, let's start doing some things with it. Fire up a Lua console and wrap the manipulator:

```lua
manip = peripheral.wrap("left")
```

As always, lets start listing the available methods:

```lua
manip.getDocs()

-- You should get something like
{
  hasModules = "function(names:string...):boolean -- Checks whether a module is a available",
  filterModules = "function(names:string...):table|nil -- Gets the methods which require these modules",
  listModules = "function():table -- Lists all modules available",
  -- Some module specific methods here. For instance:
  sense = "function():table -- Scan for entities in the vicinity",
  getMetaByID = "function(id:string) -- Find a nearby entity by UUID.
}
```

So firstly, let's list the modules our manipulator has.

```lua
manip.listModules()

{
	"plethora:sensor",
}
```

As expected, the list just has the entity sensor which we placed in. We can confirm the module is there with the
`.hasModules()` method:

```lua
manip.hasModules("plethora:sensor")

true
```

This is useful if your program requires the presence of particular modules and you want to fail gracefully are they not
installed.

Finally we have `.filterModules()`. This filters the method list to those provided by the specified modules. This isn't
really useful for us as we only have one module, but when you have multiple modules installed, it is really useful to
prevent two modules having the methods of the same name.

### Using modules
Well, now that we know a little bit about the modules we have installed, let's get cracking with them! Most modules will
provide at least a couple of methods. As we saw from `.getDocs()`, the entity sensor provides `.sense()` and
`.getMetaByID()`. We'll start with `.sense()`.

```lua
manip.sense()

{
  {
    x = 2.2, y = 0, z = -0.3,
    motionX = 0, motionY = 0, motionZ = 0,
    pitch = 25, yaw = 100,
    name = "SquidDev",
    displayName = "SquidDev",
    id = "blah-blah-blah",
  },
  ...
}
```

This provides us with a list of entities within a 8 block radius around the manipulator. Like `.list()` on inventories,
this method provides only a snapshot of the surrounding entities. Its worth noting a couple of things about the data provided:

 - The positions are relative to the sensor. Plethora tries to avoid "leaking" information about the environment, such
   as absolute positions of things.
 - The pitch and yaw are in degrees. This is simply because it is what Minecraft uses internally. As Lua's trigonometry
   functions require radians, you'll need to use `math.rad` and `math.deg` to convert between the two.

Let's find out a little more about this player. For that we'll use the `.getMetaByID()` function:

```lua
manip.getMetaByID("blah-blah-blah")
-- Note, you might want to store the result of .sense() in a variable instead of typing the UUID.

table: 66a31607
```

Blughr! That isn't very useful. The issue here is the metadata contains functions which ComputerCraft can't print out in
a nice way. Let's store it to a temporary variable and explore it that way instead. Whilst the original data is there,
there is also data about the food and health levels of the player, the player's armour and various states of the player
such as whether they are sleeping or not. We won't focus on it too much and will move on to more exciting things.

### Mixing modules
What distinguishes modules from normal peripherals is their ability to interact with each other. The block sensor allows
getting metadata about surrounding blocks and the daylight sensor allows you to get the light level of the current
block. When combined, the block sensor will also include light level's in blocks metadata.
