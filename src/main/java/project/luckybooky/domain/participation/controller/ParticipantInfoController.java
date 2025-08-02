package project.luckybooky.domain.participation.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import project.luckybooky.domain.event.entity.Event;
import project.luckybooky.domain.event.repository.EventRepository;
import project.luckybooky.domain.participation.dto.ParticipantInfoDto;
import project.luckybooky.domain.participation.service.ParticipantInfoService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Controller
@RequestMapping("/events/{eventId}")
@RequiredArgsConstructor
public class ParticipantInfoController {
    private final ParticipantInfoService participantInfoService;
    private final EventRepository eventRepository;

    @GetMapping("/participants")
    public String showParticipants(
            @PathVariable Long eventId,
            Model model
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        List<ParticipantInfoDto> participants
                = participantInfoService.getParticipantInfo(eventId);

        // 뷰에서 사용할 날짜 포맷
        String dateLabel = event.getEventDate()
                .format(DateTimeFormatter.ofPattern("yyyy년 M월 dd일"));

        model.addAttribute("participants", participants);
        model.addAttribute("dateLabel", dateLabel);
        return "participants";  // resources/templates/participants.html
    }
}
