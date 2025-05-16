package project.luckybooky.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.user.dto.response.MypageResponseDTO;
import project.luckybooky.domain.user.service.MypageService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@Tag(name = "User", description = "마이페이지 API")
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    @Operation(summary = "마이페이지 기본 정보 조회")
    @GetMapping
    public CommonResponse<MypageResponseDTO> getMypage() {
        MypageResponseDTO dto = mypageService.getMyPage();
        return CommonResponse.of(ResultCode.OK, dto);
    }
}
