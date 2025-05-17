package project.luckybooky.domain.user.repository.init;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.jwt.JwtUtil;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class UserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final JwtUtil        jwtUtil;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        /* 최초 기동 시 한 번만 삽입 */
        if (userRepository.count() > 0) return;

        List<User> guests = List.of(
                buildGuest("guest1@example.com", "게스트1", "+821012300001", UserType.MOVIE_DETAIL_COLLECTOR),
                buildGuest("guest2@example.com", "게스트2", "+821012300002", UserType.MOVIE_DETAIL_COLLECTOR),
                buildGuest("guest3@example.com", "게스트3", "+821012300003", UserType.DRAMA_STORY_IMMERSER),
                buildGuest("guest4@example.com", "게스트4", "+821012300004", UserType.SPORTS_FULL_SUPPORTER)
        );

        guests.forEach(this::attachJwtTokens);
        userRepository.saveAll(guests);
    }

    /** 게스트 회원 기본 빌더 */
    private User buildGuest(String email, String username, String phone, UserType type) {
        return User.builder()
                .email(email)
                .username(username)
                .profileImage("https://example.com/avatar/" + username + ".png")
                .hostExperienceCount(0)
                .participationExperienceCount(0)
                .phoneNumber(phone)
                .userType(type)
                .build();
    }

    /** Access / Refresh Token 생성 & 주입 */
    private void attachJwtTokens(User u) {
        String accessToken  = jwtUtil.createAccessToken(u.getEmail());     // 15분 만료 등
        String refreshToken = jwtUtil.createAccessToken(u.getEmail());    // 2주 만료 등
        u.setAccessToken(accessToken);
        u.setRefreshToken(refreshToken);
    }
}
