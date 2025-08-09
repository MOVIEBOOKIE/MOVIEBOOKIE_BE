package project.luckybooky.domain.clova.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import project.luckybooky.domain.clova.dto.ClovaRequestDTO;
import project.luckybooky.domain.clova.dto.ClovaResponseDTO;

public class ClovaConverter {

    public static List<ClovaRequestDTO.Message> buildPhrasePrompt(String userType) {
        String system = "사용자의 타입에 어울리는 장르 및 컨텐츠를 추천해줘. 타입: " + userType;
        return List.of(
                new ClovaRequestDTO.Message("system", system),
                new ClovaRequestDTO.Message("assistant", "")
        );
    }

    public static ClovaRequestDTO buildRequestDto(
            List<ClovaRequestDTO.Message> messages,
            int maxTokens,
            double temperature,
            double repeatPenalty) {
        ClovaRequestDTO dto = new ClovaRequestDTO();
        dto.setMessages(messages);
        dto.setTopP(0.8);
        dto.setTopK(0);
        dto.setMaxTokens(maxTokens);
        dto.setTemperature(temperature);
        dto.setRepeatPenalty(repeatPenalty);
        dto.setStopBefore(List.of());
        dto.setIncludeAiFilters(true);
        dto.setSeed(0);
        return dto;
    }

    public static String extractContent(String sse, ObjectMapper mapper) throws Exception {
        String finalData = null;
        String curEvent = null;
        for (String line : sse.split("\n")) {
            if (line.startsWith("event:")) {
                curEvent = line.substring(6).trim();
            } else if (line.startsWith("data:") && "result".equals(curEvent)) {
                finalData = line.substring(5).trim();
            }
        }
        ClovaResponseDTO respDto = mapper.readValue(
                finalData, ClovaResponseDTO.class);
        return respDto.getMessage().getContent().trim();
    }
}