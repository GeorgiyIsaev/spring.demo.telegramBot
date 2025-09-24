package spring.demo.telegramBot.api.data.update;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageQuery(

        @JsonProperty("message_id")
        Long messageId,

        @JsonProperty("chat")
        Chat chat
) {
}
