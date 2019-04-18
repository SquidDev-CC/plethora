local old_print, old_printError = print, printError
local function log(x)
	local a = fs.open("log.txt", "a")
	a.writeLine(x)
	a.close()
end

local function print(msg)
	old_print(msg)
	log(msg)
	commands.say(msg)
end

local function printError(msg)
	old_printError(msg)
	log(msg)
	commands.say(msg)
end

--- Waits for one or more commands to finish, and verifies they have all been executed successfully.
--
-- @usage commands.wait { commands.async.say("Testing"), commands.async.say("things!") }
function commands.wait(commands)
	local n, remaining = #commands, {}
	for i = 1, n do
		remaining[commands[i]] = i
	end

	while n > 0 do
		local _, id, ok, res = os.pullEvent("task_complete")
		local i = remaining[id]
		if i then
			n = n - 1
			remaining[id] = nil

			if not ok then
				error(("Command %d failed: %s"):format(i, res), 2)
			end
		end
	end
end

local ok, err = pcall(function()
	print("Booting test monitor")

	if not fs.exists("cloud.lua") then
		local request, err = http.get("https://cloud-catcher.squiddev.cc/cloud.lua")
		if not request then
			printError(("Cannot load cloud-catcher (%s)"):format(err))
		else
			local contents = request.readAll()
			request.close()

			local handle = fs.open("cloud.lua", "w")
			handle.write(contents)
			handle.close()

			print("Fetched cloud-catcher")
		end
	end

	print(("Run '/computercraft queue #%d cloud|test' to control the test runner"):format(os.getComputerID()))

	while true do
		local command = table.pack(os.pullEvent("computer_command"))
		if command[2] == "cloud" then
			if cloud then
				printError("Already running cloud-catcher")
			else
				local token = command[3]
				if not token then
					-- Generate a random token.
					local tok_items = {}
					for i = 1, 32 do
						local idx = math.random(36)
						tok_items[i] = ("abcdefghijklmnopqrstuvwxyz0123456789"):sub(idx, idx)
					end
					token = table.concat(tok_items)
				end

				print(("Visit https://cloud-catcher.squiddev.cc/?id=%s to control the computer"):format(token))
				shell.run("bg", "cloud.lua", "-t80x30", token)
			end
		elseif command[2] == "test" then
			print("Running tests")
			shell.run("test-rom/mcfly.lua test-rom/spec")
		else
			printError("Unknown command")
		end
	end
end)

if not ok then
	printError(tostring(err))
end

os.shutdown()
