package project.luckybooky.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.user.converter.AuthConverter;
import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.jwt.JwtUtil;
import project.luckybooky.global.oauth.dto.KakaoDTO;
import project.luckybooky.global.oauth.handler.AuthFailureHandler;
import project.luckybooky.global.oauth.util.CookieUtil;
import project.luckybooky.global.oauth.util.KakaoUtil;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public User oAuthLogin(String accessCode, String redirectUri, HttpServletResponse httpServletResponse, boolean isLocal) {
        try {
            KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode, redirectUri);
            KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

            String email = kakaoProfile.getKakao_account().getEmail();
            String nickname = kakaoProfile.getKakao_account().getProfile().getNickname();
            String profileImage = kakaoProfile.getKakao_account().getProfile().getProfile_image_url();

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createNewUser(email, nickname, profileImage));

            // JWT 토큰 생성
            String accessToken = jwtUtil.createAccessToken(user.getEmail());
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setProfileImage(profileImage);
            userRepository.save(user);

            // 쿠키 저장
            CookieUtil.addCookie(httpServletResponse, "accessToken", accessToken, (int) jwtUtil.getAccessTokenValidity(), isLocal);
            CookieUtil.addCookie(httpServletResponse, "refreshToken", refreshToken, (int) jwtUtil.getRefreshTokenValidity(), isLocal);

            httpServletResponse.setHeader("Authorization", accessToken);

            return user;

        } catch (AuthFailureHandler e) {
            throw e;
        } catch (Exception e) {
            throw new AuthFailureHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private User createNewUser(String email, String nickname, String profileImage) {
        try {
            return userRepository.save(AuthConverter.toUser(email, nickname, profileImage));
        } catch (Exception e) {
            throw new AuthFailureHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDTO.JoinInfoResultDTO getUserInfo(String email) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.warn("사용자를 찾을 수 없음, 기본 목업 데이터 반환");
                    return createMockUser();
                });

        // User 엔티티 -> DTO 변환
        return new UserResponseDTO.JoinInfoResultDTO(user.getId(), user.getEmail(), user.getUsername(), user.getProfileImage());
    }

    private User createMockUser() {
        return User.builder()
                .id(0L)
                .email("mockuser@example.com")
                .username("Mock User")
                .profileImage("mock_profile.jpg")
                .build();
    }

}
