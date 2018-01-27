---
layout: module
title: Frickin' laser beam

summary: >-
           The frickin' laser beam fires a bolt of superheated plasma, a softnose laser, or some other handwavey
           science. This incredible projectile can deal incredible damage to mobs and blocks alike.

module: plethora:laser
usable:
 - Manipulator
 - Neural interface
 - Pocket computer
 - Turtle
 - Minecart computer
image: module-laser.png
---

### Basic usage
Like all good tools, the laser does one thing, and does it well: firing lasers. The `.fire()` method takes three
arguments: the first two specifying the direction and the last specifying the potency.

The angle is provided through a yaw (angle on the horizontal axis) and pitch (angle on the verticle axis), each measured
in degrees. The potency can be any number between 0.5 and 5, with higher values dealing more damage but taking longer to
recharge (note these values are configurable).

```lua
local laser = peripheral.wrap(--[[ whatever ]])

for yaw = 0, 360, 10 do
	laser.fire(yaw, 0, 2) -- Fire a laser on the horizontal with potency 2
	sleep(0.2) -- Wait for it to recharge
end
```

Attempting to fire the laser before it has recharged will result in an error: `Insufficient energy (requires 50.0, has
20.0)`. Note that the actual values may vary. For more information, [read about the cost system][cost_system].

### Other functionality
If you are rather paranoid about the singularity, you may want to avoid giving computers access to these highly powered
weapons. Instead you can elect to fire the laser by hand. It functions a little like a bow, charging it for longer will
result in a more powerful (and more deadly) projectile.

### Configuring
The laser can be configured within the `laser` category of the `plethora.cfg` file:

 - `minimumPotency=0.5`: The minimum potency a laser can have. Raising this prevents computers spamming a large number
   of lasers in a short amount of time.

 - `maximumPotency=5`: The maximum potency a laser can have.

 - `cost=10`: The "energy" cost per potency for a laser. By default a computer will gain 10 energy points each tick (see
   [here][cost_system] for more information).

 - `damage=4`: The damage done to an entity for each potency. Players and most mobs have 20 health, meaning a fully
   charged laser can kill an unarmoured creature with one hit.

[cost_system]: http://localhost:8080/plethora/cost-system.html
