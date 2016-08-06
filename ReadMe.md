# Plethora [![Build Status](https://travis-ci.org/SquidDev-CC/plethora.svg?branch=master)](https://travis-ci.org/SquidDev-CC/plethora)

Plethora is a ComputerCraft peirpheral provider for Minecraft 1.8.9+. It aims to provide both metadata and peripherals
for most main-stream mods, in addition to vanilla Minecraft.

Plethora also adds a series of "modules" to the game. These modules can be used by the player with varying success.
They realy come in to their element when when put in a manipulator, providing a series of methods which allow
interacting with your environment. This includes:
 - Introspection: investigating the current player's inventory (and ender chest)
 - Scanner: scans blocks in an area, gathering metadata about them and allowing wrapping peripherals remotely
 - Sensor: scans entities in an area. Like the scanner this allows getting metadata and wrapping them as peripherals.
 - Frickin' laser beam. It fires lasers.

If you've ever wanted to embed a computer in your skull then today is your lucky day. Plethora provides a neural interface
which can be attached to your head, or some unsuspecting animal or monster. Right clicking the entity with a
neural controller allows you to interact with it. You can insert modules (which will be wrapped as peripherals) and
manipulate them with the built-in computer. Building a cyborg army has never been so easy.

You can also add a kinetic augment to the neural interface. This allows controlling the host entity in various ways.

## Requirements
 - Minecraft 1.8.9 with [recent version of Forge](http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.9.html)
   (I'm using 11.15.1.1902)
 - [ComputerCraft 1.79](http://minecraft.curseforge.com/projects/computercraft)
 - **[Recommended]** [JEI for 1.8.9](http://minecraft.curseforge.com/projects/just-enough-items-jei)

## Examples
There is a [whole directory of examples](https://github.com/SquidDev-CC/ccjam-2016/tree/master/examples) though
it is rather sparse at the moment. To get started, place a manipulator on top of a computer and place the appropriate
module within it.

## Documentation
You can create a HTML dump of all methods by running the command `\plethora_dump out.html`. When run in a single player
world this will save a file in the active directory (normally `.minecraft` or your modpack's folder).

This documentation is also [avaliable online](http://squiddev-cc.github.io/plethora/docs.html). However this may
be out of date. It was last generated for the 1.0.0-beta1 release.

## Images
![](https://raw.githubusercontent.com/SquidDev-CC/ccjam-2016/master/images/Squids%20and%20Lasers.png)

> You know, I have one simple request. And that is to have ~~sharks~~ squid with frickin' laser beams attached to their heads!

![](https://raw.githubusercontent.com/SquidDev-CC/ccjam-2016/master/images/Modules.png)

> Various modules available

[More images here](https://github.com/SquidDev-CC/ccjam-2016/tree/master/images)
