---
layout: module
title: Entity Sensor

summary: >-
           The entity sensor provides a way to query information about surrounding entities. There are so many ways this
           could go wrong…

module: plethora:sensor
usable:
 - Manipulator
 - Neural interface
 - Pocket computer
 - Turtle
 - Minecart computer
image: module-sensor.png
---

### Basic usage
The entity sensor provides many nifty methods, but `.sense()` is definitely the one to get started with. After all, it's
what the sensor does best! This finds all entities within 32 blocks of the sensor and reports some very basic
information about them.

```lua
local sensor = peripheral.wrap(--[[ whatever ]])
for _, entity in pairs(sensor.sense()) do
  print(("We found an entity (name: %s, uuid: %s)"):format(entity.name, entity.id))
end
```

If you want to find some more information about an entity (maybe you want to find out how hungry your friends are), you
can use `.getMetaByID` and `.getMetaByName`. The first of these is a little more general, at the cost of being slightly
more confusing. `.getMetaByID` takes an entity's UUID and returns lots of metadata about it. This ID can be found with
the above `.sense` method, though beware - it's possible the entity may have wandered off and thus no longer be within
range.

```lua
local entities = sensor.sense()
if #entities > 0 then
  local meta = sensor.getMetaByID(entities[1].id)
  if meta then print(textutils.serialise(meta)) end
end
```

`.getMetaByName` does much the same, but only operates on players, taking a username instead.

### Other functionality
Holding the entity sensor will display an orb on every nearby entity. This provides a nice way of hunting down those
pesky zombies!

![Observing nearby entities]({{ "images/items/module-sensor-usage.png" | relative_url }} "Observing nearby entities")

### Configuring
The entity sensor can be configured using the `sensor` category of the `plethora.cfg` file:

 - `radus=16`: The maximum distance that a sensor can query. Note that the area is a cube, not a sphere.
