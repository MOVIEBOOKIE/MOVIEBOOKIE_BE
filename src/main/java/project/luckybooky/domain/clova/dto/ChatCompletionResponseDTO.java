package project.luckybooky.domain.clova.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatCompletionResponseDTO {
    @JsonProperty("message")
    private MessageData message;

    @Data
    public static class MessageData {
        private String content;
    }
}
