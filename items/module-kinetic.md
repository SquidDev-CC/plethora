---
layout: module
title: Kinetic augment

summary: >-
           The kinetic augment is the latest breakthrough in biocybernetics. It grants a computer direct access to the
           nervous system of a player or mob.

module: plethora:kinetic
usable:
 - Minecart computer
 - Neural interface
 - Pocket computer
 - Turtle
image: module-kinetic.png
---

### Basic usage
While the kinetic augment has a wide range of functions, the best place to start is the most fun: `.launch()`. This
functions very similarly to a laser's `.fire()` method: taking a yaw and pitch (horizontal and vertical angle) and a
power. When called, this will catapult the current entity in the supplied direction, the resulting velocity depending on
the provided power.

```lua
local kinetic = peripheral.wrap(--[[ whatever ]])

-- Continuously fire the player into the sky
while true do
	kinetic.launch(0, -90, 4)
	sleep(0.5)
end
```

Combined with other modules, the kinetic augment can be used in great number of ways. One can fire yourself in the
direction you're currently looking, slow your descent if you're falling to fast, etc… Take a look at some of the
examples to get some ideas.

### With turtles
Kinetic augments can also be used as a turtle upgrade. When equipped, it acts as both a tool _and_
peripheral. `turtle.dig()` or `turtle.attack()` will use the currently selected item in the inventory to break blocks or
attack.

Beware, turtles do not use these tools with their normal finesse. Durability will be consumed, and blocks may take
multiple swings to break.

### Other functionality
If you're a low-tech kind of person, you can always experience the joys of `.launch()` by hand. First, hold carefully
grip the kinetic augment with either hand. Then charge it up by holding right click, feeling the raw power accumulate
in your muscles. Finally release, and enjoy the feel of the wind rushing in your hair and the rapidly approaching brick
wall.

### Configuring
The kinetic augment can be configured with the `kinetic` section of the `plethora.cfg` file.

 - `launchMax=4`: The maximum power that can be used to launch an entity.

 - `launchCost=4`: The cost per power level to launch an entity. By default a computer will gain 10 energy points each
   tick ([read about the cost system][cost_system] for more information).

 - `launchYScale=0.5`: The amount the y velocity is scaled when launching an entity. The Y axis does not experience
   friction in the same way other axis do, and so small changes in veloctity have a marge larger effect.

 - `launchElytraScale=0.4`: The amount a player's velocity is scaled by if they are using an elytra. When flying a
   player experiences much less friction, meaning small velocity increases can send the player a long distance.

 - `launchFallReset=true`: Whether to scale the fall distance if a player launches themselves. Minecraft computes fall
   damage from how long a player has been in the air rather than what speed they are travelling at. Consequently you can
   be falling very slowly but still die. If a player launches themselves upwards Plethora will correct the fall distance
   to account for the change in velocity.

   Note that this may not function correctly with wolds with custom gravity, such as Galacticraft planets.

 - `launchFloatReset=true`: Whether to reset the "floating" time after launching. This allows players to fly with the
   kinetic augment without being kicked.

> **Note:** This is not an exhaustive list of configuration options for the kinetic augment - this only includes ones
> which require further explaination. Please consult the config file for a full list.

[cost_system]: {{ "cost-system.html" | relative_url }}
