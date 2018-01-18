---
title: Redstone integrator
layout: text
---

## Redstone integrator
<div class="module-header">
	<div class="module-data">
		<p>
			The redstone integrator allows you to transmit redstone signals through networking cable, just like you
			would on a computer,
		</p>
	</div>
	<div class="module-image">
		<img src="{{ "images/items/redstone-integrator.png" | relative_url }}" alt="The redstone integrator controlling lamps." title="The redstone integrator controlling lamps."  />
	</div>
</div>

### Basic usage
The redstone integrator is acts as any normal peripheral, providing an API very similar to the default `redstone`
API. Simply place it down somewhere, attach it to a wired modem and wrap it as a peripheral:

```lua
local integrator = peripheral.find("redstone_integrator")
integrator.setOutput("north", true)
```

You'll note in the above example that we use `north`, whilst the `redstone` API uses `front` (or similar). As the
integrator has no sense of which way it's facing, one uses cardinal directions instead. Be aware that `up` and `down`
are used instead of `top` and `bottom` as well.
