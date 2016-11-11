-- When the player shifts, this fires a laser in the direction the player is looking

local modules = assert(peripheral.find("plethora:modules"), "Cannot find modules")

local sensor = modules.filterModules("plethora:sensor")
local intro = modules.filterModules("plethora:introspection")
local laser = modules.filterModules("plethora:laser")

local id = intro.getID()

while true do
	local me = sensor.getMetaByID(id)
	if me.isSneaking then
		laser.fire(me.yaw, me.pitch, 5)
	else
		sleep(0.1)
	end
end
