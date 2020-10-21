---
layout: module
title: Chat recorder

summary: >-
           The chat recorder acts as a proxy between you and your server's chat, listening in to what people say and
           sending messages on your behalf.

module: plethora:chat
usable:
 - Manipulator
 - Minecart computer
 - Neural interface
 - Pocket computer
 - Turtle
image: module-chat.png
---

### Basic usage
Once you have equipped a chat recorder, you should notice `chat_message` events beginning to appear. These will include
the player name, their message and the entity UUID:

```lua
while true do
	local _, player, message, uuid = os.pullEvent("chat_message")
	if message:find("[Hh]ello") then
		print(player .. "said hello!")
	end
end
```

Of course, it's no good printing to the screen. Let's say hello back to them! For this, we'll use the
`.say(message:string)` method:


```lua
local chat = peripheral.wrap(--[[ whatever ]])

while true do
	local _, player, message, uuid = os.pullEvent("chat_message")

	-- We exclude messages starting with "Hello to you too" so we don't
	-- reply to ourselves
	if message:find("[Hh]ello") and not message:find("^Hello to you too") then
		chat.say("Hello to you too " .. player)
	end
end
```

One thing to note here is that the chat recorder speaks _as the player_ rather than a separate object. Whilst this does
introduce some restrictions, it does grant some interesting abilities.

One of these comes in the form of "captures". We can specify a Lua pattern which will filter chat. If a message matches
the provided pattern, it will not be displayed to other players, instead queuing an event on a computer.

```lua
local chat = peripheral.wrap(-- [[whatever ]])
chat.capture("^!") -- Capture any messages starting with "!"

while true do
	local _, message, pattern, player, uuid = os.pullEvent("chat_capture")
	if pattern == "^!" then
		-- Run the provided command in the shell. So "!ls" will execute "ls".
		shell.run(message:sub(2))
	end
end
```

In order to use player-specific features inside a manipulator, you will need to bind the chat recorder to yourself. This
is done by shift + right-clicking it.


### Creative chat recorder
The creative chat recorder functions almost identically to a conventional chat recorder. However it is not tied to any
players, operating on the entire server. Consequently:

 - `.say` will not be bound to any player, printing a raw string instead.
 - `.capture` will capture the messages of _all_ players.

![The creative chat module]({{ "images/items/module-chat-creative.png" | relative_url }} "The creative chat module")

It is recommended that you [blacklist the `plethora:chat_creative` module][blacklist] if you operate a creative server.

### Configuring
The chat recorder can be configured using the `chat` category of the `plethora.cfg` file:

 - `maxLength=100`: The maximum length a chat message can be. Set to 0 to allow any length.

 - `allowFormatting=false`: Whether Minecraft formatting codes and other special characters can be used.

 - `allowMobs=true`: Whether non-player entities can post chat messages.

[blacklist]: {{ "configuring.html#blacklisting-modules" | relative_url }}
