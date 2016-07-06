-- Fires lasers in a circle

local laser = assert(peripheral.find("plethora:laser"), "Cannot find laser")

local angle = 0
local deltaAngle = math.pi * 2 / 20

while true do
	laser.fire(angle, 0, 2)
	angle = angle + deltaAngle
end
