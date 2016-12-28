#!/usr/bin/env lua
--- Generates the example files

local files = {
	"auto-feeder.lua",
	"laser-drill.lua",
}

local eHandle = assert(io.open("_includes/example-list.html", "w+"))

for i = 1, #files do
	local file = files[i]
	local path = "examples/" .. file

	local name = file:gsub("%.lua$", ""):gsub("-", " "):gsub("^%l", string.upper)
	local output = path:gsub("%.lua$", ".md")
	local outputHtml = path:gsub("%.lua$", ".html")
	local raw = path:gsub("%.lua$", "-raw.md")

	print("Writing " .. output)

	eHandle:write(("<li><a href=\"{{ %q | relative_url }}\">%s</a></li>\n"):format(outputHtml, name))

	local oHandle = io.open(output, "w+")
	local rHandle = io.open(raw, "w+")

	oHandle:write("---\n")
	oHandle:write(("title: %s\n"):format(name))
	oHandle:write("layout: text\n")
	oHandle:write("---\n\n")
	oHandle:write(("## %s <small>[Raw](%s)</small>\n"):format(name, path, raw))

	rHandle:write("---\n")
	rHandle:write(("title: %s\n"):format(name))
	rHandle:write("layout: default\n")
	rHandle:write("---\n\n")
	rHandle:write(("## %s <small>[Raw](%s) | [Annotated](%s)</small>\n```lua\n"):format(name, path, output))

	local code, empty = false, false

	for line in io.lines(path) do
		rHandle:write(line)
		rHandle:write("\n")

		local newCode, newEmpty = code, false
		if line:find("^%s*%-%-%-") then
			newCode = false
			line = line:gsub("^%s*%-%-%-%s*", "")
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
