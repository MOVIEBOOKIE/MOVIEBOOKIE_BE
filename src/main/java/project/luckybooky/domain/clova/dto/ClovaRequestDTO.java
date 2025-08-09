package project.luckybooky.domain.clova.dto;

import java.util.List;
import lombok.Data;

@Data
public class ClovaRequestDTO {
    private List<Message> messages;
    private double topP;
    private int topK;
    private int maxTokens;
    private double temperature;
    private double repeatPenalty;
    private List<String> stopBefore;
    private boolean includeAiFilters;
    private long seed;

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
