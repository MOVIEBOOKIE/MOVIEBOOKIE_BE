package project.luckybooky.domain.discordBot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.admin.service.AdminEventUserInfoService;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.service.EventService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordInteractionService {

    private final ObjectMapper objectMapper;
    private final EventService eventService;
    private final AdminEventUserInfoService adminEventUserInfoService;

    public ResponseEntity<?> handleInteraction(String body, HttpServletRequest request) {
        try {
            if (!verifySignature(request, body)) {
                log.warn("Discord signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            JsonNode root = objectMapper.readTree(body);
            int type = root.path("type").asInt();

            // 1) PING 요청에 대한 PONG 응답
            if (type == 1) {
                Map<String, Object> pong = new HashMap<>();
                pong.put("type", 1);
                return ResponseEntity.ok(pong);
            }

            // 2) Application Command 처리
            if (type == 2) {
                return handleApplicationCommand(root);
            }

            // 그 외 타입은 일단 NO_CONTENT
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Discord interaction 처리 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<?> handleApplicationCommand(JsonNode root) {
        JsonNode data = root.path("data");
        String name = data.path("name").asText("");

        if (!"event-users".equals(name)) {
            return ephemeralMessage("지원하지 않는 명령입니다.");
        }

        // 옵션에서 title 추출
        String title = null;
        JsonNode options = data.path("options");
        if (options.isArray()) {
            for (JsonNode opt : options) {
                if ("title".equals(opt.path("name").asText(""))) {
                    title = opt.path("value").asText("");
                    break;
                }
            }
        }

        if (title == null || title.isBlank()) {
            return ephemeralMessage("이벤트 제목(title) 옵션이 필요합니다.");
        }

        try {
            // EventService 를 통해 검색 (페이지 0, size 1)
            EventResponse.ReadEventListWithPageResultDTO result =
                    eventService.readEventListBySearch(title, 0, 1);

            if (result.getEventList() == null || result.getEventList().isEmpty()) {
                return ephemeralMessage("해당 제목으로 검색된 이벤트가 없습니다.");
            }

            Long eventId = result.getEventList().getFirst().getEventId();

            // 참가자 정보 Webhook 전송
            adminEventUserInfoService.sendEventUserInfoWebhook(eventId);

            return ephemeralMessage(
                    "이벤트(ID: " + eventId + ") 참가자 정보가 관리자 디스코드 채널로 전송되었습니다."
            );
        } catch (Exception ex) {
            log.error("event-users 명령 처리 중 예외 발생", ex);
            return ephemeralMessage("요청 처리 중 오류가 발생했습니다. 서버 로그를 확인해주세요.");
        }
    }

    private ResponseEntity<Map<String, Object>> ephemeralMessage(String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("flags", 64); // EPHEMERAL

        Map<String, Object> resp = new HashMap<>();
        resp.put("type", 4); // CHANNEL_MESSAGE_WITH_SOURCE
        resp.put("data", data);

        return ResponseEntity.ok(resp);
    }

    private boolean verifySignature(HttpServletRequest request, String body) {
        String signature = request.getHeader("X-Signature-Ed25519");
        String timestamp = request.getHeader("X-Signature-Timestamp");
        if (signature == null || timestamp == null) {
            log.warn("Discord signature headers missing");
            return false;
        }
        // TODO: Ed25519 서명 검증 로직을 추가하여 보안을 강화할 수 있습니다.
        // 현재는 Discord 에서 온 요청임을 전제로 동작합니다.
        return true;
    }
}

