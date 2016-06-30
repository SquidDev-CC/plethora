# Examples
Plethora aims to be self-explanitory but here are a couple of basic things you can do:

#### Explode nearby creepers
This requires the sensor module.

```lua
-- Wrap the manipulator above
local p = peripheral.wrap("top")

while true do
	-- For each entity in the area
        for _, item in ipairs(p.scan()) do
		-- If it is a creeper
                if item.name == "Creeper" then
			-- Wrap it as a peripheral and explode it
                        p.getByID(item.id).explode()
                end
        end
        sleep(0.5)
end
```
