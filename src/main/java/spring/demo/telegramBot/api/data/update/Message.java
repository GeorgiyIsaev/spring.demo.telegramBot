package spring.demo.telegramBot.api.data.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(

        @JsonProperty("message_id")
        Long messageId,

        @JsonProperty("text")
        String text,

        @JsonProperty("chat")
        Chat chat
        ){
}