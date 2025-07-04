package project.luckybooky.domain.clova.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClovaResponseDTO {
    @JsonProperty("message")
    private MessageData message;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageData {

        private String content;
    }
}
