package project.luckybooky.domain.participation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import project.luckybooky.domain.participation.dto.ParticipantInfoResult;
import project.luckybooky.domain.participation.service.ParticipantInfoService;
import project.luckybooky.global.service.UserContextService;

@Controller
@RequestMapping("/events/{eventId}")
@RequiredArgsConstructor
public class ParticipantInfoController {
    private final ParticipantInfoService participantInfoService;
    private final UserContextService userContextService;

    @GetMapping("/participants")
    public String showParticipants(@PathVariable Long eventId, Model model) {
        // 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = userContextService.getUserId();

        // 주최자 권한 확인 및 참여자 정보와 이벤트 정보 조회
        ParticipantInfoResult result = participantInfoService.getParticipantInfoForHost(eventId, currentUserId);

        model.addAttribute("participants", result.getParticipants());
        model.addAttribute("dateLabel", result.getViewDate());
        return "participants";
    }
}
