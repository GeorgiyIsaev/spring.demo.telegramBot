package spring.demo.telegramBot.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;


@FeignClient(name = "telegram", url = "${telegram.urlToken}")
public interface TelegramFeignClient {

        @PostMapping("/setWebhook")
        String setWebhook(SetWebhookRequest request);


        @PostMapping("/sendMessage")
        String sendMessage(SendMessage request);

//        @PostMapping("/inlineKeyboardMarkup")
//        String inlineKeyboardMarkup(List<InlineKeyboardButton> inlineKeyboardButton);
}

