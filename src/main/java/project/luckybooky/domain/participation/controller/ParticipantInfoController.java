package project.luckybooky.domain.participation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import project.luckybooky.domain.participation.dto.ParticipantInfoResult;
import project.luckybooky.domain.participation.service.ParticipantInfoService;
import project.luckybooky.domain.secureMail.service.MailLinkTokenService;


@Controller
@RequestMapping("/events/{eventId}")
@RequiredArgsConstructor
public class ParticipantInfoController {

    private final ParticipantInfoService participantInfoService;
    private final MailLinkTokenService mailLinkTokenService;

    @Value("${app.home-url}")
    private String homeUrl;

    @GetMapping("/participants")
    public String showParticipants(@PathVariable Long eventId, Model model) {
        ParticipantInfoResult result = participantInfoService.getParticipantInfoTemplate(eventId);
        model.addAttribute("participants", result.getParticipants());
        model.addAttribute("dateLabel", result.getViewDate());
        return "participants";
    }

    @GetMapping("/participants/link")
    public String issueAndRedirect(@PathVariable Long eventId) {
        String path = "/events/" + eventId + "/participants";
        String url = mailLinkTokenService.newLink(eventId, homeUrl, path); // homeUrl 주입 사용
        return "redirect:" + url;
    }
}
