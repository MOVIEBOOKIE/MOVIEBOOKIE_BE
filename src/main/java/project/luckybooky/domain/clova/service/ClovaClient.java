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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import project.luckybooky.domain.clova.dto.ChatCompletionRequestDTO;
import project.luckybooky.domain.clova.dto.ChatCompletionResponseDTO;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClovaClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${clova.chat.api.url}")
    private String apiUrl;

    @Value("${clova.chat.api.key}")
    private String apiKey;

    public String chat(List<ChatCompletionRequestDTO.Message> messages) {
        try {
            ChatCompletionRequestDTO req = new ChatCompletionRequestDTO();
            req.setMessages(messages);
            // set defaults or override as needed
            req.setTopP(0.8);
            req.setTopK(0);
            req.setMaxTokens(256);
            req.setTemperature(0.5);
            req.setRepeatPenalty(5.0);
            req.setStopBefore(List.of());
            req.setIncludeAiFilters(true);
            req.setSeed(0);

            String bodyJson = objectMapper.writeValueAsString(req);
            log.info("[ClovaClient] Request: {}", bodyJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);
            headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));

            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, String.class);
            String sse = resp.getBody();
            log.info("[ClovaClient] Raw SSE: {}", sse);

            return extractContent(sse);
        } catch (Exception e) {
            log.error("[ClovaClient] Error calling API", e);
            throw new RuntimeException("Clova API error");
        }
    }

    private String extractContent(String sse) throws Exception {
        String finalData = null;
        String curEvent = null;
        for (String line : sse.split("\n")) {
            if (line.startsWith("event:")) {
                curEvent = line.substring(6).trim();
            } else if (line.startsWith("data:") && "result".equals(curEvent)) {
                finalData = line.substring(5).trim();
            }
        }
        ChatCompletionResponseDTO dto = objectMapper.readValue(finalData, ChatCompletionResponseDTO.class);
        return dto.getMessage().getContent().trim();
    }
}
