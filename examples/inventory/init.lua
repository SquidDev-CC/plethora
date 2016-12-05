local dir = fs.getDir(shell.getRunningProgram())

local match = dofile(fs.combine(dir, "match.lua"))
local read = dofile(fs.combine(dir, "read.lua"))
local dialogue = dofile(fs.combine(dir, "dialogue.lua"))
local items = dofile(fs.combine(dir, "items.lua"))

local deposit = "Dropper_0"
local pickup = "Chest_6"
local rsSide = "right"

local function createLookup(tbl)
	local out = {}
	for i = 1, #tbl do out[tbl[i]] = true end
	return out
end
local blacklist = createLookup { deposit, pickup, "left", "right", "top", "bottom", "front", "back" }
local blacklistTypes = createLookup { "Furnace" }

for _, name in ipairs(peripheral.getNames()) do
	if not blacklist[name] and not blacklistTypes[peripheral.getType(name)] then
		local remote = peripheral.wrap(name)
		if remote and remote.getItemMeta then
			print("Loading " .. name)
			items.loadPeripheral(name, remote)
		end
	end
end

local function compareName(a, b) return a.displayName < b.displayName end
local function compareCount(a, b)
	if a.count == b.count then
		return a.displayName >= b.displayName
	else
		return a.count >= b.count
	end
end

local function compareHashLookup(lookup)
	return function(a, b) return lookup[a.hash] < lookup[b.hash] end
end

local function complete(filter)
	local results = {}
	if filter ~= "" and filter ~= nil then
		filter = filter:lower()
		for _, item in pairs(items.getItemEntries()) do
			local option = item.displayName
			if #option + (addSpaces and 1 or 0) > #filter and string.sub(option, 1, #filter):lower() == filter then
				local result = option:sub(#filter + 1)
				results[#results + 1] = result
			end
		end
	end
	return results
end

local display
local lastFilter = nil
local function redraw(filter)
	filter = filter or lastFilter
	lastFilter = filter

	if filter == "" or filter == nil then
		display = {}
		for _, item in pairs(items.getItemEntries()) do
			if item.count > 0 then
				display[#display + 1] = item
			end
		end

		table.sort(display, compareCount)
		local lookup = {}
		display = {}

		for _, item in pairs(items.getItemEntries()) do
			if item.count > 0 then
				local match1, score1 = match(item.name, filter)
				local match2, score2 = match(item.displayName, filter)

				local score
				if match1 and match2 then score = math.max(score1, score2)
				elseif match1 then        score = score1
				elseif match2 then        score = score2
				end

				if score then
					lookup[item.hash] = score
					display[#display + 1] = item
				end
			end
		end

		table.sort(display, compareHashLookup(lookup))
	else
		display = {}
	end

	local x, y = term.getCursorPos()
	local back, fore = term.getBackgroundColor(), term.getTextColor()

	term.setBackgroundColor(colours.lightGrey)
	term.setTextColor(colours.white)

	term.setCursorPos(1, 2)
	term.clearLine()
	term.write(("%30s \149 %6s \149 %s"):format("Item", "Dmg", "Count"))

	term.setBackgroundColor(colours.grey)
	term.setTextColor(colours.white)

	local width, height = term.getSize()
	for i = 1, height - 2 do
		term.setCursorPos(1, i + 2)
		term.clearLine()

		local item = display[i]
		if item then
			term.write(("%30s \149 %6s \149 %s"):format(item.displayName, item.damage, item.count))
		end
	end

	term.setCursorPos(x, y)
	term.setBackgroundColor(back)
	term.setTextColor(fore)
end

term.setCursorPos(1, 1)
term.setBackgroundColor(colours.white)
term.setTextColor(colours.black)
term.clear()

local readCoroutine = coroutine.create(read)
local ok, msg = coroutine.resume(readCoroutine, nil, nil, complete, redraw)

while ok and coroutine.status(readCoroutine) ~= "dead" do
	local ev = table.pack(os.pullEvent())

	if ev[1] == "mouse_click" then
		local index = ev[4] - 2
		if index >= 1 and index <= #display then
			local entry = display[index]

			local width, height = term.getSize()
			local dWidth, dHeight = math.min(width, 30), 8
			local dX, dY = math.floor((width - dWidth) / 2), math.floor((height - dHeight) / 2)

			local quantity = tonumber(dialogue("Number required", read, dX, dY, dWidth, dHeight))

			if quantity then
				items.extract(deposit, entry, quantity)
			end
			redraw()
		end
	elseif ev[1] == "peripheral" then
		local name = ev[2]
		if not blacklist[name] and not blacklistTypes[peripheral.getType(name)] then
			local remote = peripheral.wrap(name)
			if remote and remote.getItemMeta then
				items.loadPeripheral(name, remote)
			end
		end
	elseif ev[1] == "peripheral_detach" then
		items.unloadPeripheral(ev[2])
	end

	local pickup = peripheral.wrap(pickup)

	while redstone.getInput(rsSide) do
		for slot, item in pairs(pickup.list()) do
			item.slot = slot
			local entry = items.getItemEntry(item, pickup, slot)
			items.insert(pickup, entry, item)
		end

		redraw()
	end

	ok, msg = coroutine.resume(readCoroutine, table.unpack(ev, 1, ev.n))
end

term.setBackgroundColor(colours.black)
term.setTextColor(colours.white)
term.clear()
term.setCursorPos(1, 1)

if not ok then error(msg, 0) end
