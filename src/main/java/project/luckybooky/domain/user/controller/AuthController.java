package project.luckybooky.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.user.converter.UserConverter;
import project.luckybooky.domain.user.dto.response.UserResponseDTO;
import project.luckybooky.domain.user.dto.response.UserResponseDTO.JoinResultDTO;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.service.AuthService;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;
import project.luckybooky.global.apiPayload.common.BaseResponse;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "User", description = "회원가입 · 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Operation(summary = "카카오 로그인", description = "사용자에게 직접 카카오 인가코드, redirectUri, isLocal(배포 환경인지 아닌지)를 받아와서 로그인을 처리합니다.")
    @GetMapping("/login/kakao")
    public BaseResponse<JoinResultDTO> kakaoLogin(
            @Parameter(description = "카카오에서 제공한 인가 코드", required = true) @RequestParam("code") String accessCode,
            @Parameter(description = "카카오 OAuth2 인증 후 리다이렉트할 URI (로컬 환경 -> http://localhost:3000/login/kakao / 배포 환경 -> https://movie-bookie.shop/login/kakao)", required = true) String redirectUri,
            HttpServletResponse httpServletResponse,
            @Parameter(description = "directUri가 로컬인지 배포 환경인지에 따라서 true/false 사용") boolean isLocal) {
        User user = authService.oAuthLogin(accessCode, redirectUri, httpServletResponse, isLocal);
        return BaseResponse.onSuccess(UserConverter.toJoinResultDTO(user));
    }

    @Operation(summary = "로그아웃", description = "사용자가 로그인한 계정을 기준으로 리프레시 토큰을 삭제하고 쿠키에 토큰을 삭제합니다.")
    @PostMapping("/logout")
    public BaseResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }

    @Operation(summary = "회원탈퇴",
            description = "현재 로그인된 회원의 모든 연관 데이터를 삭제하고, 계정을 완전 제거합니다.")
    @DeleteMapping("/delete")
    public BaseResponse<Void> deleteUser(HttpServletRequest request,
                                         HttpServletResponse response) {
        return authService.deleteUser(request, response);
    }

    @Operation(summary = "유저 정보 조회")
    @GetMapping("/user")
    public CommonResponse<UserResponseDTO.AllInfoDTO> getUserInfo() {
        String userEmail = AuthenticatedUserUtils.getAuthenticatedUserEmail();
        return CommonResponse.of(
                ResultCode.USER_FETCH_OK,
                authService.getUserInfo(userEmail)
        );
    }

    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용해 새 액세스,리프레시 토큰을 발급합니다")
    @PostMapping("/reissue")
    public BaseResponse<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        authService.reissueTokens(request, response);
        return BaseResponse.onSuccess(null);
    }

}