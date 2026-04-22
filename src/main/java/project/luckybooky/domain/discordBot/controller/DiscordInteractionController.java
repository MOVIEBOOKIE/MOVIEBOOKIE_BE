package project.luckybooky.domain.discordBot.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.discordBot.service.DiscordInteractionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discord")
@Profile({"dev", "prod"})
public class DiscordInteractionController {

  private final DiscordInteractionService discordInteractionService;

  @PostMapping(
      value = "/interactions",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<?> handleInteraction(HttpServletRequest request) {
    return discordInteractionService.handleInteraction(request);
  }
}