import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptInput
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
    selector.addProperty("weight", KInquirer.promptInput(message = "What should the siege weight be?"))
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
                        KInquirer.promptInput(message = "What would you like complete value to be?")
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

fun buildWaves(): JsonObject {
    val waves = JsonObject()

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
        jsonArray.add(s)
    }

    return jsonArray
}
