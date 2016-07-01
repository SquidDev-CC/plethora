--- Attempts to keep the player fully fed
-- Requires sensor and introspection modules

local sensor = assert(peripheral.find("plethora:moduleSensor"), "Cannot find sensor")
local me = assert(peripheral.find("plethora:moduleIntrospection"), "Cannot find introspection")

-- Get basic details about the player
local id = me.getID()
local inv = me.getInventory()

local cachedSlot = false

while true do
	-- While the player is hungry
	local data = sensor.getMetaByID(id)
	while data.food.hungry do
		-- If we've got food there already then use it
		local method
		if cachedSlot then
			local item = inv.getItem(cachedSlot)
			method = item and item.consume
		end

		if not method then
			-- Else look for food
			for i = 1, inv.size() do
				local item = inv.getItem(i)
				if item and item.consume then
					print("Using food in slot " .. i)
					method = item.consume
					cachedSlot = i
					break
				end
			end
		end

		-- Eat the food!
		if method then
			method()
		else
			print("Cannot find food")
			break
		end

		-- Refetch data
		data = sensor.getMetaByID(id)
	end
	sleep(5)
end
