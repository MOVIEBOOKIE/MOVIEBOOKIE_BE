package project.luckybooky.domain.user.repository.init;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;
import project.luckybooky.domain.user.repository.UserRepository;

@Component
public class UserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;

    public UserInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            User guest1 = User.builder()
                    .email("guest1@example.com")
                    .username("게스트1")
                    .profileImage("https://example.com/avatar/guest1.png")
                    .accessToken("")
                    .refreshToken("")
                    .userType(UserType.MOVIE_DETAIL_COLLECTOR)
                    .hostExperienceCount(0)
                    .participationExperienceCount(2)
                    .phoneNumber("+821012300001")
                    .build();

            User guest2 = User.builder()
                    .email("guest2@example.com")
                    .username("게스트2")
                    .profileImage("https://example.com/avatar/guest2.png")
                    .accessToken("")
                    .refreshToken("")
                    .userType(UserType.MOVIE_HOTFLIX)
                    .hostExperienceCount(1)
                    .participationExperienceCount(5)
                    .phoneNumber("+821012300002")
                    .build();

            User guest3 = User.builder()
                    .email("guest3@example.com")
                    .username("게스트3")
                    .profileImage("https://example.com/avatar/guest3.png")
                    .accessToken("")
                    .refreshToken("")
                    .userType(UserType.DRAMA_SERIES_IMMERSION)
                    .hostExperienceCount(2)
                    .participationExperienceCount(3)
                    .phoneNumber("+821012300003")
                    .build();

            User guest4 = User.builder()
                    .email("guest4@example.com")
                    .username("게스트4")
                    .profileImage("https://example.com/avatar/guest4.png")
                    .accessToken("")
                    .refreshToken("")
                    .userType(UserType.DRAMA_HIGHLIGHT_TRACKER)
                    .hostExperienceCount(0)
                    .participationExperienceCount(4)
                    .phoneNumber("+821012300004")
                    .build();

            userRepository.saveAll(List.of(guest1, guest2, guest3, guest4));
        }
    }
}