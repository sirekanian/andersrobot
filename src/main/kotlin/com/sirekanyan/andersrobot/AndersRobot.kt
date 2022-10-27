package com.sirekanyan.andersrobot

import com.sirekanyan.andersrobot.command.*
import com.sirekanyan.andersrobot.config.Config
import com.sirekanyan.andersrobot.config.ConfigKey.*
import com.sirekanyan.andersrobot.extensions.logError
import com.sirekanyan.andersrobot.extensions.logInfo
import org.telegram.telegrambots.bots.DefaultAbsSender
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.generics.LongPollingBot
import org.telegram.telegrambots.util.WebhookUtils

val botName = Config[BOT_USERNAME]
val adminId = Config[ADMIN_ID]
val delayedCommands = mutableMapOf<Long, Command>()
private val primaryCommands: List<Command> =
    listOf(
        LocationCommand,
        CityCommand(listOf("/start", "/temp", "погода"), Controller::onCityCommand, Controller::onWeatherCommand),
        CityCommand(listOf("/add", "добавить город"), Controller::onAddCity, Controller::onCityMissing),
        CityCommand(listOf("/delete", "удалить город"), Controller::onDeleteCity, Controller::onCityMissing),
        CityCommand(listOf("/forecast", "прогноз"), Controller::onForecastCommand, Controller::onCityMissing),
    )
private val secondaryCommands: List<Command> =
    listOf(
        RegexCommand("\\b(celsi|цельси)", Controller::onCelsiusCommand),
        RegexCommand("\\b((андерс|anders|погод[аеуы])\\b|градус)", Controller::onWeatherCommand),
    )
private val help = listOf(
    HelpDescription("/temp", "What's the temperature outside?", "Сколько градусов за окном?"),
    HelpDescription("/add", "Add city", "Добавить город"),
    HelpDescription("/delete", "Delete city", "Удалить город"),
    HelpDescription("/forecast", "Weather forecast", "Прогноз погоды"),
)

class AndersRobot : DefaultAbsSender(DefaultBotOptions()), LongPollingBot {

    private val factory = ControllerFactory()

    init {
        val scope = BotCommandScopeDefault.builder().build()
        execute(SetMyCommands(help.map(HelpDescription::ruCommand), scope, "ru"))
        execute(SetMyCommands(help.map(HelpDescription::enCommand), scope, null))
    }

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = Config[BOT_TOKEN]

    override fun onUpdateReceived(update: Update) {
        try {
            onUpdate(update)
        } catch (exception: Exception) {
            logError("Cannot handle update", exception)
            logError(update)
        }
    }

    private fun onUpdate(update: Update) {
        val message = update.message ?: run {
            println("Update is ignored: message is empty")
            return
        }
        val chatId = message.chatId
        val text = message.text
        println("${message.from?.id} (chat $chatId) => $text")
        val controller = factory.createController(this, update)
        for (command in primaryCommands) {
            if (command.execute(controller, message)) {
                return
            }
        }
        delayedCommands[chatId]?.let { command ->
            delayedCommands.remove(chatId)
            command.execute(controller, text)
            return
        }
        for (command in secondaryCommands) {
            if (command.execute(controller, message)) {
                return
            }
        }
    }

    override fun clearWebhook() {
        logInfo("Cleared.")
        WebhookUtils.clearWebhook(this)
    }

    override fun onClosing() {
        logInfo("Closed.")
    }

}