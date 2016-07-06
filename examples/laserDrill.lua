-- When the player shifts, this fires a laser in the direction the player is looking

local sensor = peripheral.find("plethora:sensor")
local intro = peripheral.find("plethora:introspection")
local laser = peripheral.find("plethora:laser")

local id = intro.getID()

while true do
	local me = sensor.getMetaByID(id)
	if me.isSneaking then
		laser.fire(me.yaw, me.pitch, 5)
	else
		sleep(0.1)
	end
end
