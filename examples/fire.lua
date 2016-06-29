-- Fires lasers in a circle

local angle = 0
local deltaAngle = math.pi * 2 / 20

while true do
  peripheral.call("top", "fire", angle, 0, 2)
  angle = angle+deltaAngle
end
