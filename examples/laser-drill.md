---
title: Laser drill
layout: text
---

## Laser drill <small>[Raw](laser-drill.lua)</small>
This script fires a laser in the direction the player is looking when they sneak.

Firstly we want to ensure that we have a neural interface and wrap it.

```lua
local modules = peripheral.find("neuralInterface")
if not modules then
	error("Must have a neural interface", 0)
end
```

We require an introspection module and entity sensor to get the direction the player is facing in. Obviously a laser
is required too.

```lua
if not modules.hasModule("plethora:sensor") then
	error("Must have a laser", 0)
end
if not modules.hasModule("plethora:introspection") then
	error("Must have an introspection module", 0)
end
if not modules.hasModule("plethora:laser", 0) then
	error("Must have a laser", 0)
end
```

Now that we're all set up, we loop forever. First we get the player's metadata and check they're sneaking. If so we
fire a laser and sleep to allow the energy buffer to refil. Otherwise we sleep for a short period of time before
checking again.

```lua
while true do
	local meta = modules.getMetaOwner()
	if meta.isSneaking then
		modules.fire(meta.yaw, meta.pitch, 5)
		sleep(0.2)
	else
		sleep(0.1)
	end
end
```

