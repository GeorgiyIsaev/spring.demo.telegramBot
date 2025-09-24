package spring.demo.telegramBot.api.data.update;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CallbackQuery(
        @JsonProperty("message")
        MessageQuery message,

        @JsonProperty("data")
        String data
) {
}
