-- Finds hostile mobs and fires lasers at them

local modules = assert(peripheral.find("plethora:modules"), "Cannot find modules")

local laser = assert(modules.filterModules("plethora:laser"), "Cannot find laser")
local sensor = assert(modules.filterModules("plethora:sensor"), "Cannot find sensor")

local maxTicks = 10
local laserSpeed = 1.5

local function bestFire(entity, motion)
	local bestDelta = math.huge
	local ticks = false

	local x, y, z = entity.x, entity.y, entity.z
	local dx, dy, dz
	if motion then
		-- Not implemented yet. Not sure of the best way to do this
		dx, dy, dz = entity.motionX, entity.motionY, entity.motionZ
		for i = 1, maxTicks do
			local nx, ny, nz = x + dx * i, y + dy * i, z + dz * i

			local distance = nx * nx + ny * ny + nz * nz
			local delta = math.sqrt(distance) % laserSpeed
			if delta < bestDelta then
				bestDelta = delta
				bestTicks = i
			end
		end

		if not bestTicks then error("Serious bug happened somewhere") end
	else
		dx, dy, dz = 0, 0, 0
		ticks = 0
	end

	local nx, ny, nz = x + dx * ticks, y + dy * ticks, z + dz * ticks
	local pitch = -math.atan2(ny, math.sqrt(nx * nx + nz * nz))
	local yaw = math.atan2(-nx, nz)

	laser.fire(yaw, pitch, 5)
end

while true do
	local mobs = sensor.sense()

	local count = 0
	for _, mob in ipairs(mobs) do
		if mob.name == "Creeper" or mob.name == "Zombie" or mob.name == "Skelleton" then
			bestFire(mob, false)

			-- Ensure that we don't have too high a latency.
			count = count + 1
			if count >= 4 then break end
		end
	end

	if count == 0 then sleep(1) end
end
