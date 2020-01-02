#!/usr/bin/env lua
--- Generates the example files

local files = {
	"laser-drill.lua",
	"laser-sentry.lua",
	"auto-feeder.lua",
	"fly.lua",
	"ore-scanner.lua",
}

local pre = "examples/"
local eHandle = assert(io.open("_includes/example-list.html", "w+"))

for i = 1, #files do
	local file = files[i]
	local stripped = file:gsub("%.lua$", "")
	local name = stripped:gsub("-", " "):gsub("^%l", string.upper)

	local output = stripped
	local raw = stripped .. "-raw"

	print("Writing " .. output)

	eHandle:write(("<li><a href=\"{{ %q | relative_url }}\">%s</a></li>\n"):format(pre .. output .. ".html", name))

	local oHandle = io.open(pre .. output .. ".md", "w+")
	local rHandle = io.open(pre .. raw .. ".md", "w+")

	oHandle:write("---\n")
	oHandle:write(("title: %s\n"):format(name))
	oHandle:write("layout: text\n")
	oHandle:write("---\n\n")
	oHandle:write(("## %s <small>[Raw](%s)</small>\n"):format(name, file))

	rHandle:write("---\n")
	rHandle:write(("title: %s\n"):format(name))
	rHandle:write("layout: default\n")
	rHandle:write("---\n\n")
	rHandle:write(("## %s <small>[Raw](%s) | [Annotated](%s)</small>\n```lua\n"):format(name, file, output .. ".html"))

	local code, empty = false, false

	for line in io.lines(pre .. file) do
		rHandle:write(line)
		rHandle:write("\n")

		local newCode, newEmpty = code, false
		if line:find("^%s*%-%-%-") then
			newCode = false
			line = line:gsub("^%s*%-%-%-%s?", "")
		elseif line == "" then
			newEmpty = true
		else
			newCode = true
		end

		-- Don't write when we have a blank line: prevents trailing lines in code blocks.
		if not newEmpty then
			if newCode ~= code then
				--- Write the code "boundaries"
				if newCode then
					oHandle:write("\n```lua\n")
				else
					oHandle:write("```\n\n")
				end
			elseif empty then
				-- If we have a blank line without a boundary then write it.
				oHandle:write("\n")
			end

			oHandle:write(line)
			oHandle:write("\n")
		end

		code = newCode
		empty = newEmpty
	end

	if code then
		oHandle:write("```\n\n")
	end

	oHandle:close()

	rHandle:write("```\n")
	rHandle:close()
end

eHandle:close()
