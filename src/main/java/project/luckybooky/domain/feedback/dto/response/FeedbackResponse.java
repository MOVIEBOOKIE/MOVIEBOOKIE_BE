package project.luckybooky.domain.feedback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class FeedbackResponse {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class FeedbackCreateResultDTO {
        Long feedbackId;
        LocalDateTime createdAt;
    }
}
