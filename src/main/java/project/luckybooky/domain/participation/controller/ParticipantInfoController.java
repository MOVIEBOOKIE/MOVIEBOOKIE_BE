package project.luckybooky.domain.participation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import project.luckybooky.domain.participation.dto.ParticipantInfoResult;
import project.luckybooky.domain.participation.service.ParticipantInfoService;


@Controller
@RequestMapping("/events/{eventId}")
@RequiredArgsConstructor
public class ParticipantInfoController {
    private final ParticipantInfoService participantInfoService;

    @GetMapping("/participants")
    public String showParticipants(@PathVariable Long eventId, Model model) {
        ParticipantInfoResult result = participantInfoService.getParticipantInfoTemplate(eventId);
        model.addAttribute("participants", result.getParticipants());
        model.addAttribute("dateLabel", result.getViewDate());
        return "participants";
    }
}
