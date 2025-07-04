package project.luckybooky.domain.user.repository.init;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.jwt.JwtUtil;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(0)
public class UserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        /* 최초 기동 시 한 번만 삽입 */
        if (userRepository.count() > 0) {
            return;
        }

        List<User> guests = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> buildGuest(
                        "guest" + i + "@example.com",
                        "게스트" + i,
                        "+8210123" + String.format("%04d", i),
                        UserType.MOVIE_DETAIL_COLLECTOR))
                .peek(this::attachJwtTokens)
                .collect(Collectors.toList());

        userRepository.saveAll(guests);
    }

    /**
     * 게스트 회원 기본 빌더
     */
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

    /**
     * Access / Refresh Token 생성 & 주입
     */
    private void attachJwtTokens(User u) {
        String accessToken = jwtUtil.createAccessToken(u.getEmail());     // 15분 만료 등
        String refreshToken = jwtUtil.createAccessToken(u.getEmail());    // 2주 만료 등
        u.setAccessToken(accessToken);
        u.setRefreshToken(refreshToken);
    }
}
