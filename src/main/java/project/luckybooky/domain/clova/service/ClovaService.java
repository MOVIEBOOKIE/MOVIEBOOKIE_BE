package project.luckybooky.domain.clova.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.luckybooky.domain.clova.converter.ClovaConverter;
import project.luckybooky.domain.clova.dto.ClovaRequestDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClovaService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${clova.keyword.api.url}")
    private String apiUrl;

    @Value("${clova.api.key}")
    private String apiKey;

    public String generateSimplePhrase(String topic) {
        List<ClovaRequestDTO.Message> messages =
                ClovaConverter.buildPhrasePrompt(topic);

        ClovaRequestDTO dto =
                ClovaConverter.buildRequestDto(messages, 64, 0.7, 1.0);

        try {
            String reqJson = objectMapper.writeValueAsString(dto);
            log.info("[ClovaService] Request: {}", reqJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);
            headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));

            HttpEntity<String> entity = new HttpEntity<>(reqJson, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);
            String sse = resp.getBody();
            log.info("[ClovaService] Raw SSE: {}", sse);

            String content = ClovaConverter.extractContent(sse, objectMapper);
            return content.split("\n")[0].trim();
        } catch (Exception e) {
            log.error("[ClovaService] Error calling Clova API", e);
            throw new RuntimeException("Clova API error");
        }
    }
}