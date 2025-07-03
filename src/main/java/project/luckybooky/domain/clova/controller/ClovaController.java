package project.luckybooky.domain.clova.controller;

import Kobaco.backend.api.keyword.dto.KeywordRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.luckybooky.global.apiPayload.response.CommonResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords")
public class ClovaController {

    private final ClovaService clovaService;

    public class ClovaController {
        private final ClovaService clovaService;

        @PostMapping
        public CommonResponse<KeywordResponseDTO> generateKeyword(
                @RequestBody KeywordRequestDTO request) {
            KeywordResponseDTO response = clovaKeywordService.generateKeywords(request);
            return ResponseEntity.ok(CommonResponse.ofOK(response));
        }
    }
}