package com.foodtech.back.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class WowKitchenBot extends AbilityBot {

    private String CHAT_ID = "-1001498328341";

    public WowKitchenBot() {
        this("1609944630:AAHv_h0vKjv-JxMFyNSk6bHvsmLQpFRwYwo", "Wow-kitchen-bot");
    }

    public WowKitchenBot(String botToken, String botUsername, DefaultBotOptions botOptions) {
        super(botToken, botUsername, botOptions);
    }

    public WowKitchenBot(String botToken, String botUsername) {
        super(botToken, botUsername);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
    }

    public void sendMessageToChat(String content) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(content);
        sendMessage.setChatId(CHAT_ID);
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException var4) {
            log.error("Error send message to chat");
        }
    }

    @Override
    public int creatorId() {
        return 0;
    }
}
