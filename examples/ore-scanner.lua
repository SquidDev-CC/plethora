--- This renders a minimap showing nearby ores using the overlay glasses and block scanner.

--- We start the program by specifying a series of configuration options. Feel free to ignore these, and use the values
--- inline. Whilst you don't strictly speaking need a delay between each iteration, it does reduce the impact on the
--- server.
local scanInterval = 0.2
local renderInterval = 0.05
local scannerRange = 8
local scannerWidth = scannerRange * 2 + 1

--- These values aren't very exciting, they just control what the minimap looks like
local size = 0.5
local cellSize = 16
local offsetX = 75
local offsetY = 75

--- We end our configuration section by defining the ores we're interested in and what colour we'll draw them as. We
--- define some ores as having a higher priority, so large ore veins don't mask smaller veins of more precious ores.
local ores = {
	["minecraft:diamond_ore"] = 10,
	["minecraft:emerald_ore"] = 10,
	["minecraft:gold_ore"] = 8,
	["minecraft:redstone_ore"] = 5,
	["minecraft:lapis_ore"] = 5,
	["minecraft:iron_ore"] = 2,
	["minecraft:coal_ore"] = 1
}

local colours = {
	["minecraft:coal_ore"] = { 150, 150, 150 },
	["minecraft:iron_ore"] = { 255, 150, 50 },
	["minecraft:lava"] = { 150, 75, 0 },
	["minecraft:gold_ore"] = { 255, 255, 0 },
	["minecraft:diamond_ore"] = { 0, 255, 255 },
	["minecraft:redstone_ore"] = { 255, 0, 0 },
	["minecraft:lapis_ore"] = { 0, 50, 255 },
	["minecraft:emerald_ore"] = { 0, 255, 0 }
}

--- Now let's get into the interesting stuff! Let's look for a neural interface and check we've got all the required
--- modules.
local modules = peripheral.find("neuralInterface")
if not modules then error("Must have a neural interface", 0) end
if not modules.hasModule("plethora:scanner") then error("The block scanner is missing", 0) end
if not modules.hasModule("plethora:glasses") then error("The overlay glasses are missing", 0) end

--- Now we've got our neural interface, let's extract the canvas and ensure nothing else is on it.
local canvas = modules.canvas()
canvas.clear()

--- We now need to set up our minimap. We create a 2D array of text objects around the player, each starting off
--- displaying an empty string. If we find an ore, we'll update their colour and text.
local block_text = {}
local blocks = {}
for x = -scannerRange, scannerRange, 1 do
	block_text[x] = {}
	blocks[x] = {}

	for z = -scannerRange, scannerRange, 1 do
		block_text[x][z] = canvas.addText({ 0, 0 }, " ", 0xFFFFFFFF, size)
		blocks[x][z] = { y = nil, block = nil }
	end
end

--- We also create a marker showing the current player's location.
canvas.addText({ offsetX, offsetY }, "^", 0xFFFFFFFF, size * 2)

--- Our first big function is the scanner: this searches for ores near the player, finds the most important ones, and
--- updates the block table.
local function scan()
	while true do
		local scanned_blocks = modules.scan()

		--- For each nearby position, we search the y axis for interesting ores. We look for the one which has
		--- the highest priority and update the block information
		for x = -scannerRange, scannerRange do
			for z = -scannerRange, scannerRange do
				local best_score, best_block, best_y = -1
				for y = -scannerRange, scannerRange do
					--- The block scanner returns blocks in a flat array, so we index into it with this rather scary formulae.
					local scanned = scanned_blocks[scannerWidth ^ 2 * (x + scannerRange) + scannerWidth * (y + scannerRange) + (z + scannerRange) + 1]

					--- If there is a block here, and it's more interesting than our previous ores, then let's use that!
					if scanned then
						local new_score = ores[scanned.name]
						if new_score and new_score > best_score then
							best_block = scanned.name
							best_score = new_score
							best_y = y
						end
					end
				end

				-- Update our block table with this information.
				blocks[x][z].block = best_block
				blocks[x][z].y = best_y
			end
		end

		--- We wait for some delay before starting again. This isn't _strictly_ needed, but helps reduce server load
		sleep(scanInterval)
	end
end

--- The render function takes our block information generated in the previous function and updates the text elements.
local function render()
	while true do
		--- If possible, we rotate the map using the current player's look direction. If it's not available, we'll just
		--- use north as up.
		local meta = modules.getMetaOwner and modules.getMetaOwner()
		local angle = meta and math.rad(-meta.yaw % 360) or math.rad(180)

		--- Like before, loop over every nearby block and update something. Though this time we're updating objects on
		--- the overlay canvas.
		for x = -scannerRange, scannerRange do
			for z = -scannerRange, scannerRange do
				local text = block_text[x][z]
				local block = blocks[x][z]

				if block.block then
					--- If we've got a block here, we update the position of our text element to account for rotation,
					local px = math.cos(angle) * -x - math.sin(angle) * -z
					local py = math.sin(angle) * -x + math.cos(angle) * -z

					local sx = math.floor(px * size * cellSize)
					local sy = math.floor(py * size * cellSize)
					text.setPosition(offsetX + sx, offsetY + sy)

					--- Then change the text and colour to match the location of the ore
					text.setText(tostring(block.y))
					text.setColor(table.unpack(colours[block.block]))
				else
					--- Otherwise we just make sure the text is empty. We don't need to faff about with clearing the
					--- colour or position, as we'll change it next iteration anyway.
					text.setText(" ")
				end
			end
		end

		sleep(renderInterval)
	end
end

--- We now run our render and scan loops in parallel, continually updating our block list and redisplaying it to the
--- wearer.
parallel.waitForAll(render, scan)
