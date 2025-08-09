package project.luckybooky.domain.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.luckybooky.domain.admin.dto.ParticipantInfo;
import project.luckybooky.domain.admin.dto.VenueRequestWebhookDTO;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    /**
     * Discord webhook 에 embeds 형태로 POST
     */
    public void sendVenueRequest(VenueRequestWebhookDTO dto) {
        try {
            Map<String, Object> embed = new LinkedHashMap<>();
            embed.put("title", "📣 새로운 대관 신청");
            embed.put("color", 1127128);

            List<Map<String, String>> fields = new ArrayList<>();
            fields.add(field("날짜", dto.getDate(), true));
            fields.add(field("시간", dto.getTime(), true));
            fields.add(field("장소", dto.getLocationName(), false));
            fields.add(field("주최자", dto.getHostUsername() + " / " + dto.getHostPhoneNumber(), false));
            fields.add(field("참여인원", String.valueOf(dto.getParticipantCount()), true));
            fields.add(field("목적", dto.getPurpose(), false));

            // 각 참여자 추가 필드
            for (ParticipantInfo p : dto.getParticipants()) {
                fields.add(field(p.getUsername(),
                        String.join("\n",
                                "📧 " + p.getCertificationEmail(),
                                "📱 " + p.getPhoneNumber(),
                                "👤 " + p.getUserTypeTitle()
                        ), false));
            }

            embed.put("fields", fields);
            Map<String, Object> payload = Map.of("embeds", List.of(embed));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> req = new HttpEntity<>(
                    objectMapper.writeValueAsString(payload),
                    headers
            );

            ResponseEntity<String> resp = restTemplate.postForEntity(webhookUrl, req, String.class);
            log.info("Discord webhook 전송: status={}, body={}", resp.getStatusCode(), resp.getBody());
        } catch (Exception ex) {
            log.error("Discord webhook 전송 실패", ex);
        }
    }

    private Map<String, String> field(String name, String value, boolean inline) {
        return Map.of(
                "name", name,
                "value", value,
                "inline", String.valueOf(inline)
        );
    }
}
