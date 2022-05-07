import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptInput
import com.github.kinquirer.components.promptInputNumber
import com.github.kinquirer.components.promptList
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlin.system.exitProcess

fun main() {
    val version = "v1.0.0"
    println("Welcome to Invasion Toolbox $version")
    when (KInquirer.promptList(
        message = "What would you like to generate?",
        choices = listOf("New Invasion", "New Mob", "Exit")
    )) {
        "New Invasion" -> createInvasion()
        "New Mob" -> newMob()
        "Exit" -> {
            exitProcess(1)
        }
    }
}

fun createInvasion() {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val json = JsonObject()

    val id = KInquirer.promptInput(message = "What should the siege id be?")
    val name = KInquirer.promptInput(message = "What should the siege name be?")

    json.add(id, buildMain(name))
    println(gson.toJson(json))
}

fun buildMain(name: String): JsonObject {
    val main = JsonObject()

    main.add("selector", buildSelector())
    main.add("commands", buildCommands())
    main.addProperty("name", name)
    main.add("messages", buildMessages())
    main.add("waves", buildWaves())

    return main
}


/*
    "selector": {
      "gamestages": {
        "and": ["siegeevent"]
      },
      "weight": 32,
      "dimension": {
        "type": "include",
        "dimensions": [ 0 ]
      }
    }
 */
fun buildSelector(): JsonObject {
    fun buildGamestages(): JsonObject {
        val selectorGamestages = JsonObject()
        selectorGamestages.add("and", promptArray("Please enter a comma separated list of required game stages:"))
        return selectorGamestages
    }

    fun buildDimension(): JsonObject {
        val selectorDimension = JsonObject()
        selectorDimension.addProperty(
            "type",
            KInquirer.promptList(
                message = "How would you like to handle dimensions?",
                choices = listOf("include", "exclude")
            )
        )
        selectorDimension.add(
            "dimensions",
            promptArray("Please enter a comma seperated list of dimensions you want to add to this list:")
        )
        return selectorDimension
    }

    val selector = JsonObject()

    selector.add("gamestages", buildGamestages())
    selector.addProperty("weight", KInquirer.promptInputNumber(message = "What should the siege weight be?"))
    selector.add("dimension", buildDimension())

    return selector
}

/*
    "commands": {
      "start": [
        "/"
      ],
      "end": [
        "/"
      ],
      "staged": [
        {
          "complete": 0.5,
          "commands": [
            "/"
          ]
        }

      ]
    }
*/
fun buildCommands(): JsonObject {
    fun buildStaged(): JsonArray {
        val staged = JsonArray()

        while (true) {
            when (KInquirer.promptList(
                message = "Would you like to add an element to staged block?",
                choices = listOf("Add", "Done")
            )) {
                "Add" -> {
                    val stagedElement = JsonObject()
                    stagedElement.addProperty(
                        "complete",
                        KInquirer.promptInputNumber(message = "What would you like complete value to be?")
                    )
                    stagedElement.add(
                        "commands",
                        promptArray("Please enter a comma seperated list of commands to add.")
                    )
                    staged.add(stagedElement)
                }
                "Done" -> {
                    break
                }
            }
        }
        return staged
    }

    val commands = JsonObject()

    commands.add("start", promptArray("Please enter a comma separated list of commands to be executed on siege start."))
    commands.add("end", promptArray("Please enter a comma separated list of commands to be executed on siege end."))
    commands.add("staged", buildStaged())

    return commands
}

/*
    "messages": {
      "start": "",
      "end": "",
      "warn": {
        "ticks": 12000,
        "message": "."
      }
    }
 */
fun buildMessages(): JsonObject {
    fun buildWarn(): JsonObject {
        val warn = JsonObject()

        warn.addProperty(
            "ticks",
            KInquirer.promptInput(message = "How many ticks before should a player be warned before siege start?")
        )
        warn.addProperty(
            "ticks",
            KInquirer.promptInput(message = "What warning message should a player receive before siege start??")
        )

        return warn
    }

    val messages = JsonObject()

    messages.addProperty("start", KInquirer.promptInput(message = "What message should be sent on siege start?"))
    messages.addProperty("end", KInquirer.promptInput(message = "What message should be sent on siege end?"))
    messages.add("warn", buildWarn())

    return messages
}

/*
    "waves": [
      {
        "delayTicks": [ 0 ],
        "groups": [
          {
            "mobs": [
              {
                "id": "zombiesiege.grunt",
                "count": [ 14, 16 ],
                "spawn": {
                  "type": "ground",
                  "light": [
                    0,
                    7
                  ],
                  "rangeXZ": [
                    16,
                    32
                  ],
                  "rangeY": 8,
                  "stepRadius": 4,
                  "sampleDistance": 2
                }
              }
            ]
          }
        ]
      }
    ]
 */
fun buildWaves(): JsonArray {
    val waves = JsonArray()

    fun buildSpawn(): JsonObject {
        val spawn = JsonObject()

        spawn.addProperty(
            "type",
            KInquirer.promptList(
                message = "What should the spawn type be?",
                choices = listOf("ground", "air", "beneath")
            )
        )
        spawn.add("light", promptArray("What should the light range be? Format: min,max"))
        spawn.add("light", promptArray("What should the XZ range be? Format: min,max"))
        spawn.addProperty("rangeY", KInquirer.promptInputNumber(message = "What should the rangeY be?"))
        spawn.addProperty("stepRadius", KInquirer.promptInputNumber(message = "What should the step radius be?"))
        spawn.addProperty(
            "sampleDistance",
            KInquirer.promptInputNumber(message = "What should the sample distance be?")
        )

        return spawn
    }

    fun buildMobs(): JsonArray {
        val mobs = JsonArray()
        while (true) {
            when (KInquirer.promptList(
                message = "Would you like to add a new mob to the group?",
                choices = listOf("Add Mob", "Done")
            )) {
                "Add Mob" -> {
                    val mob = JsonObject()

                    mob.addProperty("id", KInquirer.promptInput("What is the mob id?"))
                    mob.add("count", promptArray("What should the count be? (You can use commas for multiple numbers)"))
                    mob.add("spawn", buildSpawn())
                    mobs.add(mob)
                }
                "Done" -> {
                    break
                }
            }

        }
        return mobs
    }

    fun buildGroups(): JsonArray {
        val groups = JsonArray()

        while (true) {
            when (KInquirer.promptList(
                message = "Would you like to add a new group to the wave?",
                choices = listOf("Add Group", "Done")
            )) {
                "Add Group" -> {
                    val group = JsonObject()
                    group.addProperty("weight", KInquirer.promptInputNumber("What should the group weight be?"))
                    group.addProperty(
                        "forceSpawn",
                        KInquirer.promptList(
                            message = "Should forceSpawn be enabled?",
                            choices = listOf("true", "false")
                        )
                    )
                    group.add("mobs", buildMobs())
                    groups.add(group)
                }
                "Done" -> {
                    break
                }
            }
        }
        return groups
    }

    while (true) {
        when (KInquirer.promptList(
            message = "Would you like to add a new wave?",
            choices = listOf("New Wave", "Done")
        )) {
            "New Wave" -> {
                val wave = JsonObject()
                wave.add("delayTicks", promptArray("How many delay ticks should this wave have?"))
                wave.add("groups", buildGroups())
                waves.add(wave)
            }
            "Done" -> {
                break
            }
        }

    }

    return waves
}

fun newMob() {
    println("This has not been implemented yet :(")
}

fun promptArray(prompt: String): JsonArray {
    var raw: String = KInquirer.promptInput(message = prompt)
    raw = raw.replace(" ", "")

    val jsonArray = JsonArray()

    for (s in raw.split(",")) {
        try {
            jsonArray.add(s.toInt())
        } catch (e: java.lang.NumberFormatException) {
            jsonArray.add(s)
        }
    }

    return jsonArray
}
