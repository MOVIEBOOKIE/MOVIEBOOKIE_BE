package project.luckybooky.domain.discordBot.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.discordBot.service.DiscordInteractionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discord")
public class DiscordInteractionController {

    private final DiscordInteractionService discordInteractionService;

    @PostMapping("/interactions")
    public ResponseEntity<?> handleInteraction(
            @RequestBody String body,
            HttpServletRequest request
    ) {
        return discordInteractionService.handleInteraction(body, request);
    }
}

