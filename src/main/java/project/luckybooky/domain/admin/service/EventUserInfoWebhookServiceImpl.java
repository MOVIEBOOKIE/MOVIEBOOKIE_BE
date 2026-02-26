package project.luckybooky.domain.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.luckybooky.domain.admin.dto.EventUserInfoWebhookDTO;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"prod", "dev"})
public class EventUserInfoWebhookServiceImpl implements EventUserInfoWebhookService {

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${discord.webhook.event-user-info-url}")
  private String eventUserInfoWebhookUrl;

  @Override
  public void sendEventUserInfo(EventUserInfoWebhookDTO dto) {
    log.info("운영용 환경에서 이벤트 참가자 유저 정보 웹훅을 전송합니다. eventTitle={}", dto.getEventTitle());

    try {
      Map<String, Object> embed = new LinkedHashMap<>();
      embed.put("title", "👥 이벤트 참가자 정보");
      embed.put("color", 15158332);

      List<Map<String, String>> fields = new ArrayList<>();
      fields.add(field("이벤트", dto.getEventTitle(), false));
      fields.add(field("날짜", dto.getDate(), true));
      fields.add(field("주최자", dto.getHostUsername(), true));
      fields.add(field("참여 인원", dto.getParticipantCount() + "명", true));

      for (EventUserInfoWebhookDTO.EventUserInfoDetail p : dto.getParticipants()) {
        fields.add(field(
            p.getUsername(),
            String.join("\n",
                "📧 " + p.getCertificationEmail(),
                "📱 " + p.getPhoneNumber(),
                "👥 그룹 타입: " + p.getGroupType()
            ),
            false
        ));
      }

      embed.put("fields", fields);
      Map<String, Object> payload = Map.of("embeds", List.of(embed));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> req = new HttpEntity<>(
          objectMapper.writeValueAsString(payload),
          headers
      );

      ResponseEntity<String> resp = restTemplate.postForEntity(eventUserInfoWebhookUrl, req,
          String.class);
      log.info("Discord webhook 전송 (이벤트 참가자 유저 정보): status={}, body={}", resp.getStatusCode(),
          resp.getBody());
    } catch (Exception ex) {
      log.error("Discord webhook 전송 실패 (이벤트 참가자 유저 정보)", ex);
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

