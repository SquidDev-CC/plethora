-- Explodes all nearby creepers

local p = peripheral.wrap("top")

while true do
        for _, item in ipairs(p.scan()) do
                if item.name == "Creeper" then
                        p.getByID(item.id).explode()
                end
        end
        sleep(0.5)
end
