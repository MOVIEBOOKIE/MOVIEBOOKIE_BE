package project.luckybooky.domain.clova.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.domain.clova.dto.GenreRecommendationDTO;
import project.luckybooky.domain.clova.dto.UserTypeRequestDTO;
import project.luckybooky.domain.clova.service.ClovaService;
import project.luckybooky.global.apiPayload.response.CommonResponse;
import project.luckybooky.global.apiPayload.response.ResultCode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/phrases")
public class ClovaController {
    private final ClovaService clovaService;

    @PostMapping
    public CommonResponse<GenreRecommendationDTO> generatePhrase(
            @RequestBody UserTypeRequestDTO request) {
        String phrase = clovaService.generateSimplePhrase(request.getTopic());
        return CommonResponse.of(ResultCode.OK, new GenreRecommendationDTO(phrase));
    }
}