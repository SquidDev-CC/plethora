-- Fires lasers in a circle

local modules = assert(peripheral.find("plethora:modules"), "Cannot find modules")

local laser = assert(modules.filterModules("plethora:laser"), "Cannot find laser")

local angle = 0
local deltaAngle = math.pi * 2 / 20

while true do
	laser.fire(angle, 0, 2)
	angle = angle + deltaAngle
end
