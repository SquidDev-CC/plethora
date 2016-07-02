-- When the player shifts, this fires a laser in the direction the player is looking

local sensor = peripheral.find("plethora:moduleSensor")
local intro = peripheral.find("plethora:moduleIntrospection")
local laser = peripheral.find("plethora:moduleLaser")

local id = intro.getID()

while true do
	local me = sensor.getMetaByID(id)
	if me.isSneaking then
		laser.fire(me.yaw, me.pitch, 5)
	else
		sleep(0.1)
	end
end
