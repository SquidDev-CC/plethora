---
title: Minecart computer
layout: text
---

## Minecart computer
<div class="module-header">
	<div class="module-data">
		<p>
			Have you ever been working really hard on your rail-road and felt lonely? Or perhaps you've had one too many
			issues with furnace minecarts and are looking for something more inteligent? Then have we a solution for
			you! By placing a computer inside an empty minecart, you can combine the power of a computer with the
			utility of a minecart.
		</p>
	</div>
	<div class="module-image">
		<img src="{{ "images/items/minecart-computer.png" | relative_url }}" />
	</div>
</div>

### Basic usage
Simply right-click an empty minecart with any computer (normal, advanced or command), placing it inside it. Clicking the
computer screen will turn it on and open the terminal - you can now use it like a normal computer.

Of course, a computer only so much use on its own: we need a way to interact with the world, and for that we need
peripherals. Thankfully, minecart computers also have the ability to equip peripherals. Simply hover over any face of
the computer (aside from the front) until an outline appears. Then right click whilst holding some peripheral or
module. The item should show up on that side. You can right click again with an empty hand to remove it.

![]({{ "images/items/minecart-computer-compare.png" | relative_url }})

Now that you've attached the item, you can simply wrap it like any other peripheral. Let's see what the kinetic augment
offers:

```lua
local kinetic = peripheral.wrap("top")
kinetic.propel(1) -- Propel the minecart along the track.
```

Once you've finished with the computer, you can simply break it to recover the consituent parts. Make sure to label your
locomotive friend first though!

> **Note:** Not all peripherals or modules are usable inside a kinetic augment. If you find one which you feel should
> be, do [let me know on the issue tracker](https://github.com/SquidDev-CC/Plethora/issues).
