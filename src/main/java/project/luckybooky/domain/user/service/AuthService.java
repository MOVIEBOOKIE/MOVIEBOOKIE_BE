package project.luckybooky.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.user.converter.AuthConverter;
import project.luckybooky.domain.user.converter.UserConverter;
import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.common.BaseResponse;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.jwt.JwtUtil;
import project.luckybooky.global.jwt.TokenService;
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
    private final TokenService tokenService;


    @Transactional
    public User oAuthLogin(String accessCode, String redirectUri, HttpServletResponse httpServletResponse,
                           boolean isLocal) {
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

//            long rtTtl = jwtUtil.getRemainingSeconds(refreshToken);
//            log.info("🔹 [Login] Redis에 리프레시 토큰 저장 시작. userId={}, ttl={}s", user.getId(), rtTtl);
//            tokenService.storeRefreshToken(user.getId(), refreshToken, rtTtl);
//            log.info("🔹 [Login] Redis에 저장된 리프레시 토큰 = {}", tokenService.getStoredRefreshToken(user.getId()));

            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setProfileImage(profileImage);
            userRepository.save(user);

            // 쿠키 저장
            CookieUtil.addCookie(httpServletResponse, "accessToken", accessToken,
                    (int) jwtUtil.getAccessTokenValidity(), isLocal);
            CookieUtil.addCookie(httpServletResponse, "refreshToken", refreshToken,
                    (int) jwtUtil.getRefreshTokenValidity(), isLocal);

            httpServletResponse.setHeader("Authorization", accessToken);

            return user;

        } catch (AuthFailureHandler e) {
            throw e;
        } catch (Exception e) {
            log.error("🌐 OAuth 로그인 처리 중 예외 발생", e);
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
    public UserResponseDTO.AllInfoDTO getUserInfo(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserConverter.toAllInfoDTO(user);
    }

    private User createMockUser() {
        return User.builder()
                .id(0L)
                .email("mockuser@example.com")
                .username("Mock User")
                .profileImage("mock_profile.jpg")
                .build();
    }

    @Transactional
    public BaseResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 현재 로그인된 사용자 이메일 조회
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();

        // 2. 사용자 엔티티 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. Refresh Token 삭제 (DB)
        user.setRefreshToken(null);
        userRepository.save(user);

        // 4. 환경 판별 (로컬인지)
        boolean isLocal = false;
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("localhost:3000")) {
            isLocal = true;
        }

        // 5. 쿠키 삭제
        CookieUtil.deleteCookie(response, "accessToken", isLocal);
        CookieUtil.deleteCookie(response, "refreshToken", isLocal);

        // 6. SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // 7. 성공 응답 반환
        return BaseResponse.onSuccess("로그아웃 성공");
    }

//    @Transactional
//    public ReissueResultDTO reissueTokens(HttpServletRequest request,
//                                          HttpServletResponse response) {
//        // 1) 쿠키에서 refreshToken 꺼내기
//        String refreshToken = CookieUtil.getCookieValue(request, "refreshToken");
//        if (refreshToken == null) {
//            throw new BusinessException(ErrorCode.UNAUTHORIZED);
//        }
//        log.info("🔄 [Reissue] 브라우저에서 받은 refreshToken = {}", refreshToken);
//
//        // 2) 토큰 유효성 및 category 검증
//        jwtUtil.validateToken(refreshToken);
//        String category = jwtUtil.extractCategory(refreshToken);
//        if (!"refresh".equals(category)) {
//            throw new BusinessException(ErrorCode.INVALID_TOKEN_TYPE);
//        }
//
//        // 3) 이메일 추출 & 사용자 조회
//        String email = jwtUtil.extractEmail(refreshToken);
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
//        Long userId = user.getId();
//
//        // 4) Redis에 저장된 토큰과 비교 (다중 환경 로그인 방지)
//        String saved = tokenService.getStoredRefreshToken(userId);
//        log.info("🔄 [Reissue] Redis에 저장된 refreshToken = {}", saved);
//        if (saved == null || !saved.equals(refreshToken)) {
//            tokenService.deleteAllRefreshTokens(userId);
//            throw new BusinessException(ErrorCode.MULTI_ENV_LOGIN);
//        }
//
//        // 5) 토큰 로테이션: 새 토큰 생성
//        String newAccessToken = jwtUtil.createAccessToken(email);
//        String newRefreshToken = jwtUtil.createRefreshToken(email);
//
//        // 6) Redis에 새 리프레시 토큰 저장 (TTL 적용)
//        long newRtTtl = jwtUtil.getRemainingSeconds(newRefreshToken);
//        log.info("🔄 [Reissue] Redis에 새로운 refreshToken 저장 시작. userId={}, ttl={}s", userId, newRtTtl);
//        tokenService.storeRefreshToken(userId, newRefreshToken, newRtTtl);
//        log.info("🔄 [Reissue] Redis에 저장된 새로운 refreshToken = {}", tokenService.getStoredRefreshToken(userId));
//
//        user.setAccessToken(newAccessToken);
//        user.setRefreshToken(newRefreshToken);
//        userRepository.save(user);
//
//        // 7) 쿠키에 새 토큰 심기
//        boolean isLocal = request.getHeader("Referer") != null
//                && request.getHeader("Referer").contains("localhost:3000");
//        CookieUtil.addCookie(response,
//                "accessToken",
//                newAccessToken,
//                jwtUtil.getAccessTokenValidity(),
//                isLocal);
//        CookieUtil.addCookie(response,
//                "refreshToken",
//                newRefreshToken,
//                jwtUtil.getRefreshTokenValidity(),
//                isLocal);
//
//        // 8) 결과 반환
//        return new ReissueResultDTO(
//                newAccessToken,
//                newRefreshToken,
//                jwtUtil.getRemainingSeconds(newAccessToken),
//                newRtTtl
//        );
//    }

}
