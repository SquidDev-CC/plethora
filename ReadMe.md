# CCJam-2016 → Plethora

 - A 1.8.9 peripheral provider.
 - Focuses on interaction as well as getting information
 - Utilisies Forge's new capability system

## Requirements
 - Minecraft 1.8.9 with recent version of Forge (I'm using 11.15.1.1902)
 - ComputerCraft 1.79

## Planned features
 - Meta provider system: provides metadata about objects
 - Method provider system: allows interacting with objects
   - Context sensitive: for instance you have methods on just `ItemStack`s but also methods on `ItemStack`s in inventories.
 - Sensor: various "modules" allow interacting with the world around
   - Also have range upgrades? 4, 8, 16 block radius?
 - Headset: like sensor but with player introspection: eat food through Lua.
   - Terminal glasses: later addition probably

## Potential modules
 - Introspection:
   - Allows getting inventory of current sensor or player
   - Expose baubles and ender chest inventories on players?
 - Remote:
   - Gather a list of blocks in the vicinity
   - Wrap peripherals in the area/
 - Entity:
   - List of entities in the vicinity
   - Wrap peripherals on the entities
