package project.luckybooky.domain.feedback.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.domain.feedback.converter.FeedbackConverter;
import project.luckybooky.domain.feedback.dto.request.FeedbackRequest;
import project.luckybooky.domain.feedback.dto.response.FeedbackResponse;
import project.luckybooky.domain.feedback.entity.Feedback;
import project.luckybooky.domain.feedback.entity.type.NegativeFeedback;
import project.luckybooky.domain.feedback.entity.type.PositiveFeedback;
import project.luckybooky.domain.feedback.repository.FeedbackRepository;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.service.UserTypeService;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserTypeService userTypeService;
    private final EventService eventService;

    @Transactional
    public FeedbackResponse.FeedbackCreateResultDTO createFeedback(FeedbackRequest.FeedbackCreateRequestDTO request, Long userId, Long eventId) {
        PositiveFeedback positiveFeedback;
        NegativeFeedback negativeFeedback;

        if (request.getIsSatisfied()) {
            positiveFeedback = PositiveFeedback.fromDescription(request.getFeedback());
            negativeFeedback = NegativeFeedback.NONE;
        }
        else {
            positiveFeedback = PositiveFeedback.NONE;
            negativeFeedback = NegativeFeedback.fromDescription(request.getFeedback());
        }

        User user = userTypeService.findOne(userId);
        Event event = eventService.findOne(eventId);

        Feedback feedback = FeedbackConverter.toFeedback(request, positiveFeedback, negativeFeedback, event, user);
        feedbackRepository.save(feedback);

        return FeedbackConverter.toFeedbackCreateResultDTO(feedback);
    }
}
