package com.foodtech.back.config;


import com.foodtech.back.bot.WowKitchenBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
public class TelegramBotConfig {

    @Bean
    public WowKitchenBot wowKitchenBot() {
        try {
            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            // Register your newly created AbilityBot
            WowKitchenBot bot = new WowKitchenBot();
            //register bot in context
            botsApi.registerBot(bot);
            return bot;
        } catch (Exception e) {
            log.error("Error while init telegram bot : {}", e.getMessage());
        }
        return null;
    }

}
