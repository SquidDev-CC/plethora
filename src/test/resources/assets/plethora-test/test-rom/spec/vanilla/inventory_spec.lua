describe("inventories", function()
	local function setup()
		commands.wait {
			commands.async.setblock("~", "~1", "~", "minecraft:air"),
			commands.async.setblock("~", "~1", "~", "minecraft:chest", 0, "replace", {
				Items = { { Slot = 0, id = "minecraft:stick", Count = 50 } }
			}),
		}

		return peripheral.wrap("top")
	end

	it("size", function()
		local inv = setup()
		expect(inv.size()):eq(27)
	end)

	it("list", function()
		local inv = setup()
		expect(inv.list()):same { { name = "minecraft:stick", damage = 0, count = 50 } }
	end)

	describe("getItemMetdata", function()
		it("on an empty slot", function()
			local inv = setup()
			expect(inv.getItemMeta(2)):eq(nil)
		end)

		it("on a stick", function()
			local inv = setup()
			expect(inv.getItemMeta(1)):same {
				name = "minecraft:stick", displayName = "Stick", rawName = "item.stick",
				damage = 0, maxDamage = 0, count = 50, maxCount = 64,
				ores = { stickWood = true }
			}
		end)
	end)
end)
