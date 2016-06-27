# Modules

Modules are a way of interacting with the world. They can be mounted within
the manipulator, on a neural interface or used by the player.

## Introspection
 - Crafted from ender chest, player head/skull and gold.
 - Shift+right-click to bind to player.
 - **Manipulator:** Must be bound to a player, allows getting ender chest
   and player's inventory.
 - **Neural interface:** As above, but bound to the player that the interface
   is attached to.
 - **In hand:** Allows accessing the player's ender chest remotely.

## Frickin' laser beam
 - Crafted from diamond, iron, redstone.
 - **Manipulator:** Can be oriented and fired. Produces a laser beam
 - **Neural interface:** Fires in the direction the player is looking.
 - **In hand:** A simple laser beam.
 - Laser beam breaks blocks or damages enemies, setting them on fire.

## Scanner
 - Crafted from ender pearls, modem, iron and dirt.
 - **Manipulator/Neural interface:** Describes blocks within a cube centered on the player.
   Also allows wrapping them as peripherals if possible. The larger the radius the longer the
   delay before getting a result.
 - **In hand:** Highlights ores within a 8 block radius

## Sensor
 - Crafted from ender pearls, modem, iron and rotten flesh.
 - **Manipulator/Neural interface:** Describes entities within a cube centered on the player.
   Also allows wrapping them as peripherals if possible.
 -- **In hand:** Highlights entities within a 16 block radius.

## Holographic
 - Monitors and Gold
 - **Manipulator:** Allows drawing above the manipulator. 2D transparent screen that faces
   towards player.
 - **Neural interface:** Terminal glasses drawing in front of the screen.
 - **In hand:** Probably nothing.

# Manipulator
 - The manipulator has 9 inventory slots for storing various items and 1 slot for the
   module.
 - The module is rendered above the manipulator: like OpenPeripheral's sensor.

# Neural interface
 - Holds 5 modules.
 - Goes in helmet slot.
 - *Hopefully* can be mounted on other entities: squid with frickin' laser beams attached to
   their heads.
