package spring.demo.telegramBot.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = "telegram", url = "${telegram.urlToken}")
public interface TelegramFeignClient {

        @PostMapping("/setWebhook")
        String setWebhook(SetWebhookRequest request);
}

