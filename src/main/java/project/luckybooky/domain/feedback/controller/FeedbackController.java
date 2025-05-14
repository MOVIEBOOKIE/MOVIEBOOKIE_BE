package project.luckybooky.domain.feedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.feedback.dto.request.FeedbackRequest;
import project.luckybooky.domain.feedback.dto.response.FeedbackResponse;
import project.luckybooky.domain.feedback.service.FeedbackService;
import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.service.AuthService;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.common.BaseResponse;

@Tag(name = "Feedback", description = "피드백 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final AuthService authService;

    /** 로그인 정보로부터 유저 ID 추출 **/
    private Long toUserId() {
        String userEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        UserResponseDTO.AllInfoDTO userInfo = authService.getUserInfo(userEmail);
        return userInfo.getId();
    }

    @Operation(summary = "피드백 생성", description = "해당하는 값을 넣어주세요 !")
    @PostMapping("/{eventId}")
    public BaseResponse<FeedbackResponse.FeedbackCreateResultDTO> createEvent(@PathVariable("eventId") Long eventId, @RequestBody FeedbackRequest.FeedbackCreateRequestDTO request) {
        // 유저 ID 가져오기
        Long userId = toUserId();
        return BaseResponse.onSuccess(feedbackService.createFeedback(request, userId, eventId));
    }
}
