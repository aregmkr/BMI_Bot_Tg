import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import io.github.cdimascio.dotenv.Dotenv

data class BmiRecord(
    val weight: Double,
    val height: Double,
    val bmi: Double,
    val category: String,
    val time: LocalDateTime = LocalDateTime.now()
)

val history_bmi: MutableMap<Long, MutableList<BmiRecord>> = mutableMapOf()

fun main() {
//    val dotenv = Dotenv.load()
//    val botToken = dotenv["TELEGRAM_BOT_TOKEN"]

    val botToken = System.getenv("TELEGRAM_BOT_TOKEN")

    var started: Boolean = false
    val bot = bot {
        token = botToken
        dispatch {
            command("start") {
                val res = bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id), text = "Send your weight in kg."
                )
                res.fold({}, {})
                started = true
            }

            command("help") {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Just follow the bot's questions. To start over, use /start"
                )
            }

            command("history") {
                val userId = message.chat.id
                val history = show_history(userId)

                bot.sendMessage(chatId = ChatId.fromId(userId), text = history)
            }

            var userWeight: Double? = null
            var userHeight: Double? = null

            text {
                val messageText = message.text
                if (started) {
                    if (userWeight == null) {
                        userWeight = messageText?.toDoubleOrNull()

                        if (userWeight != null) {
                            val heightMessage = bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id), text = "Now, send me your height in meters"
                            )
                            heightMessage.fold({}, {})
                        } else if (messageText == "/start" || messageText == "/help") {

                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id),
                                text = "Please send valid number for your weight in kg"
                            )
                        }
                    } else if (userHeight == null) {
                        userHeight = messageText?.toDoubleOrNull()

                        if (userHeight != null) {
                            val bmi = calculateBMI(userWeight ?: 1.0, userHeight ?: 1.0)
                            val category = getCategory(bmi)

                            history_bmi.getOrPut(message.chat.id) { mutableListOf() }
                                .add(BmiRecord(userWeight!!, userHeight!!, bmi, category))

                            bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id),
                                text = "Your BMI:${"%.2f".format(bmi)}\nCategory: $category\nTo start over, type /start."
                            )

                            userWeight = null
                            userHeight = null
                            started = false
                        } else {
                            bot.sendMessage(
                                chatId = ChatId.fromId(message.chat.id),
                                text = "Please send a valid number for your height in meters."
                            )
                        }
                    }
                }
            }
        }
    }
    bot.startPolling()
}

fun show_history(index: Long): String {
    val history = history_bmi[index]
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    return if (history.isNullOrEmpty()) {
        "No BMI history found"
    } else {
        history.joinToString("\n\n") { record ->
            "Weight: ${record.weight}kg\n" +
                    "Height: ${record.height}m\n" +
                    "BMI: ${"%.2f".format(record.bmi)}\n" +
                    "Category: ${record.category}\n" +
                    "Date: ${record.time.format(formatter)}"
        }
    }
}

fun calculateBMI(weight: Double, height: Double): Double {
    return weight / (height * height)
}

fun getCategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Underweight"
        bmi <= 24.9 -> "Normal weight"
        bmi <= 29.9 -> "Overweight"
        else -> "Obese"
    }
}