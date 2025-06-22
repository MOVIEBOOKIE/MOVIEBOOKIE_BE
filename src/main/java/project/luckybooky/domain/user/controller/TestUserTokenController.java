package project.luckybooky.domain.user.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.user.repository.UserRepository;

@RestController
@Profile("dev")
@RequestMapping("/api/test/users")
@RequiredArgsConstructor
public class TestUserTokenController {
    private final UserRepository userRepository;

    @GetMapping("/tokens")
    public List<UserTokenDto> getAllUserTokens() {
        return userRepository.findAll().stream()
                .map(u -> new UserTokenDto(u.getUsername(), u.getAccessToken()))
                .collect(Collectors.toList());
    }

    public record UserTokenDto(String username, String token) {
    }
}
