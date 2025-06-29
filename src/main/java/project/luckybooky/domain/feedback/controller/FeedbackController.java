package project.luckybooky.domain.feedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.feedback.dto.request.FeedbackRequest;
import project.luckybooky.domain.feedback.dto.response.FeedbackResponse;
import project.luckybooky.domain.feedback.dto.response.FeedbackResponse.FeedbackCreateResultDTO;
import project.luckybooky.domain.feedback.service.FeedbackService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;
import project.luckybooky.global.service.UserContextService;

@Tag(name = "Feedback", description = "피드백 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final UserContextService userContextService;

    @Operation(summary = "피드백 생성", description = "해당하는 값을 넣어주세요 !<br><br>" +
            "isSatisfied: true -> 만족, false -> 불만족 <br>" +
            "feedback: 반드시 피드백 보기에 해당하는 문장 그대로 넣어주세요 ! (ex, '내게 꼭 맞는 이벤트를 추천해줘요') <br> comment: 의견 (텍스트) ")
    @PostMapping
    public CommonResponse<FeedbackCreateResultDTO> createFeedback(@RequestBody FeedbackRequest.FeedbackCreateRequestDTO request) {
        Long userId = userContextService.getUserId();
        FeedbackResponse.FeedbackCreateResultDTO resultDto =
                feedbackService.createFeedback(request, userId);

        return CommonResponse.of(ResultCode.CREATED, resultDto);
    }
}
