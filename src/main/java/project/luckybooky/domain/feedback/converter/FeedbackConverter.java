package project.luckybooky.domain.feedback.converter;

import project.luckybooky.domain.category.entity.Category;
import project.luckybooky.domain.event.dto.request.EventRequest;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.feedback.dto.request.FeedbackRequest;
import project.luckybooky.domain.feedback.dto.response.FeedbackResponse;
import project.luckybooky.domain.feedback.entity.Feedback;
import project.luckybooky.domain.feedback.entity.type.NegativeFeedback;
import project.luckybooky.domain.feedback.entity.type.PositiveFeedback;
import project.luckybooky.domain.location.entity.Location;
import project.luckybooky.domain.user.entity.User;

public class FeedbackConverter {
    public static Feedback toFeedback(
            FeedbackRequest.FeedbackCreateRequestDTO request,
            PositiveFeedback positiveFeedback,
            NegativeFeedback negativeFeedback,
            Event event,
            User user
    ) {
        return Feedback.builder()
                .isSatisfied(request.getIsSatisfied())
                .positiveFeedback(positiveFeedback)
                .negativeFeedback(negativeFeedback)
                .comment(request.getComment())
                .event(event)
                .user(user)
                .build();
    }

    public static FeedbackResponse.FeedbackCreateResultDTO toFeedbackCreateResultDTO(Feedback feedback) {
        return FeedbackResponse.FeedbackCreateResultDTO.builder()
                .feedbackId(feedback.getId())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
