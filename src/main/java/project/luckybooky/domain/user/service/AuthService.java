package project.luckybooky.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.participation.service.ParticipationService;
import project.luckybooky.domain.user.converter.AuthConverter;
import project.luckybooky.domain.user.converter.UserConverter;
import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.ticket.repository.TicketRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.common.BaseResponse;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.jwt.JwtUtil;
import project.luckybooky.global.jwt.TokenService;
import project.luckybooky.global.oauth.dto.KakaoDTO;
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
    private final ParticipationService participationService;
    private final TicketRepository ticketRepository;

    private static final ThreadLocal<Boolean> isUserWithdrawal = new ThreadLocal<>();

    /**
     * 현재 스레드에서 회원탈퇴 중인지 확인
     */
    public static boolean isUserWithdrawalInProgress() {
        return Boolean.TRUE.equals(isUserWithdrawal.get());
    }

    /**
     * 회원탈퇴 상태 설정
     */
    private void setUserWithdrawalInProgress(boolean inProgress) {
        if (inProgress) {
            isUserWithdrawal.set(true);
        } else {
            isUserWithdrawal.remove();
        }
    }

    private boolean isLocalRequest(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");
        String host = request.getHeader("Host");

        return containsLocalHost(referer)
                || containsLocalHost(origin)
                || containsLocalHost(host);
    }

    private boolean containsLocalHost(String value) {
        return value != null
                && (value.contains("localhost") || value.contains("127.0.0.1"));
    }


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

            // 기존 토큰 정리 (재로그인 시 이전 토큰 삭제)
            if (user.getId() != null) {
                log.info("🔹 [Login] 기존 토큰 정리 시작. userId={}", user.getId());
                tokenService.deleteAllRefreshTokens(user.getId());
            }

            // JWT 토큰 생성
            String accessToken = jwtUtil.createAccessToken(user.getEmail());
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

            long rtTtl = jwtUtil.getRemainingSeconds(refreshToken);
            log.info("🔹 [Login] Redis에 리프레시 토큰 저장 시작. userId={}, ttl={}s", user.getId(), rtTtl);
            tokenService.storeRefreshToken(user.getId(), refreshToken, rtTtl);
            log.info("🔹 [Login] Redis에 저장된 리프레시 토큰 = {}", tokenService.getStoredRefreshToken(user.getId()));

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

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("🌐 OAuth 로그인 처리 중 예외 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private User createNewUser(String email, String nickname, String profileImage) {
        try {
            return userRepository.save(AuthConverter.toUser(email, nickname, profileImage));
        } catch (BusinessException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
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

        // 1) 이메일로 유저 조회
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();

        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isLocal = isLocalRequest(request);

        // 5. 쿠키 삭제
        CookieUtil.deleteCookie(response, "accessToken", isLocal);
        CookieUtil.deleteCookie(response, "refreshToken", isLocal);

        SecurityContextHolder.clearContext();

        return BaseResponse.onSuccess("로그아웃 성공");
    }

    @Transactional
    public BaseResponse<Void> reissueTokens(HttpServletRequest request,
                                            HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (!"refresh".equals(jwtUtil.extractCategory(refreshToken))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN_TYPE);
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Long userId = user.getId();

        // 4) Redis 에 저장된 토큰과 비교
        String saved = tokenService.getStoredRefreshToken(userId);
        if (saved == null) {
            log.warn("🔹 [Token Reissue] Redis에 저장된 리프레시 토큰이 없음. userId={}", userId);
            tokenService.deleteAllRefreshTokens(userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!saved.equals(refreshToken)) {
            log.warn("🔹 [Token Reissue] Redis 토큰과 쿠키 토큰 불일치. userId={}", userId);
            tokenService.deleteAllRefreshTokens(userId);
            throw new BusinessException(ErrorCode.MULTI_ENV_LOGIN);
        }

        String newAccessToken = jwtUtil.createAccessToken(email);
        String newRefreshToken = jwtUtil.createRefreshToken(email);

        long newRtTtl = jwtUtil.getRemainingSeconds(newRefreshToken);
        boolean rotated = tokenService.compareAndSetRefreshToken(
                userId,
                refreshToken,
                newRefreshToken,
                newRtTtl
        );
        if (!rotated) {
            log.warn("🔹 [Token Reissue] 동시 재발급 경쟁 감지. userId={}", userId);
            throw new BusinessException(ErrorCode.MULTI_ENV_LOGIN);
        }

        user.setAccessToken(newAccessToken);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        boolean isLocal = isLocalRequest(request);
        CookieUtil.addCookie(response,
                "accessToken",
                newAccessToken,
                (int) jwtUtil.getAccessTokenValidity(),
                isLocal);
        CookieUtil.addCookie(response,
                "refreshToken",
                newRefreshToken,
                (int) jwtUtil.getRefreshTokenValidity(),
                isLocal);
        response.setHeader("Authorization", newAccessToken);

        return BaseResponse.onSuccess(null);
    }

    @Transactional
    public BaseResponse<String> deleteUser(HttpServletRequest request,
                                           HttpServletResponse response) {

        // 1) 현재 유저 조회
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Long userId = user.getId();

        try {
            // 회원탈퇴 상태 설정
            setUserWithdrawalInProgress(true);

            // 2) 연관된 이벤트 취소 처리
            participationService.cancelParticipation(userId);

            // 2-1) 티켓 조인 테이블에서 사용자 연관 데이터 삭제
            ticketRepository.deleteUserTicketsByUserId(userId);

            // 3) User 삭제 (cascade 설정으로 Feedback, Notification, Participation 모두 함께 삭제)
            userRepository.delete(user);

            // 4) Redis에 남은 리프레시 토큰 삭제
            tokenService.deleteAllRefreshTokens(userId);

            boolean isLocal = isLocalRequest(request);

            // 5) 클라이언트 쿠키 만료
            CookieUtil.deleteCookie(response, "accessToken", isLocal);
            CookieUtil.deleteCookie(response, "refreshToken", isLocal);

            return BaseResponse.onSuccess("정상 처리되었습니다.");
        } finally {
            // 회원탈퇴 상태 정리
            setUserWithdrawalInProgress(false);
        }
    }
}