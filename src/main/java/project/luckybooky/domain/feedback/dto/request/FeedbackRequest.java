package project.luckybooky.domain.feedback.dto.request;

import lombok.Getter;

public class FeedbackRequest {
    @Getter
    public static class FeedbackCreateRequestDTO {
        Boolean isSatisfied;
        String feedback;
        String comment;
    }
}
