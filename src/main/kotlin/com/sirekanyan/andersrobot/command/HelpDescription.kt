package com.sirekanyan.andersrobot.command

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand

class HelpDescription(word: String, en: String, ru: String) {
    val enCommand = BotCommand(word, en)
    val ruCommand = BotCommand(word, ru)
}