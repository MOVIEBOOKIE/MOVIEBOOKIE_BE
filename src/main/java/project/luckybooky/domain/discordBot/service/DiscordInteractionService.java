package project.luckybooky.domain.discordBot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.luckybooky.domain.admin.service.AdminEventUserInfoService;
import project.luckybooky.domain.discordBot.support.DiscordRawBodyFilter;
import project.luckybooky.domain.event.dto.response.EventResponse;
import project.luckybooky.domain.event.service.EventService;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Slf4j
@Service
@Profile({"dev", "prod"})
@RequiredArgsConstructor
public class DiscordInteractionService {

  private final ObjectMapper objectMapper;
  private final EventService eventService;
  private final AdminEventUserInfoService adminEventUserInfoService;

  @Value("${discord.public-key}")
  private String discordPublicKeyHex;

  public ResponseEntity<?> handleInteraction(HttpServletRequest request) {
    try {
      String rawBody = (String) request.getAttribute(DiscordRawBodyFilter.ATTR_DISCORD_RAW_BODY);
      if (rawBody == null) {
        rawBody = "";
      }

      String ua = request.getHeader("User-Agent");
      String sig = request.getHeader("X-Signature-Ed25519");
      String ts = request.getHeader("X-Signature-Timestamp");

      JsonNode root = objectMapper.readTree(rawBody);
      int type = root.path("type").asInt();

      log.info("Discord Interaction received: type={}, ua={}, sigPresent={}, tsPresent={}",
          type, ua, sig != null, ts != null);

      // ✅ 운영 보안: PING 포함 전체 요청 서명 검증
      if (!verifySignature(request, rawBody)) {
        log.warn("Discord signature verification failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      // 1) PING -> PONG
      if (type == 1) {
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"type\":1}");
      }

      // 2) Application Command
      if (type == 2) {
        return handleApplicationCommand(root);
      }

      return ResponseEntity.noContent().build();

    } catch (Exception e) {
      log.error("Discord interaction 처리 중 예외 발생", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private boolean verifySignature(HttpServletRequest request, String rawBody) {
    String signatureHex = request.getHeader("X-Signature-Ed25519");
    String timestamp = request.getHeader("X-Signature-Timestamp");

    if (signatureHex == null || timestamp == null) {
      log.warn("Discord signature headers missing");
      return false;
    }

    try {
      byte[] sig = Hex.decode(signatureHex);
      byte[] msg = (timestamp + rawBody).getBytes(StandardCharsets.UTF_8);

      byte[] publicKey = Hex.decode(discordPublicKeyHex);
      Ed25519PublicKeyParameters pub = new Ed25519PublicKeyParameters(publicKey, 0);

      Ed25519Signer verifier = new Ed25519Signer();
      verifier.init(false, pub);
      verifier.update(msg, 0, msg.length);

      return verifier.verifySignature(sig);
    } catch (Exception e) {
      return false;
    }
  }

  private ResponseEntity<?> handleApplicationCommand(JsonNode root) {
    JsonNode data = root.path("data");
    String name = data.path("name").asText("");

    if (!"event-users".equals(name)) {
      return ephemeralMessage("지원하지 않는 명령입니다.");
    }

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
      EventResponse.ReadEventListWithPageResultDTO result =
          eventService.readEventListBySearch(title, 0, 1);

      if (result.getEventList() == null || result.getEventList().isEmpty()) {
        return ephemeralMessage("해당 제목으로 검색된 이벤트가 없습니다.");
      }

      Long eventId = result.getEventList().getFirst().getEventId();

      adminEventUserInfoService.sendEventUserInfoWebhook(eventId);

      return ephemeralMessage("이벤트(ID: " + eventId + ") 참가자 정보가 관리자 디스코드 채널로 전송되었습니다.");

    } catch (BusinessException ex) {
      // ✅ 참여자 없음 에러만 별도 처리
      if (ex.getErrorCode() == ErrorCode.NO_EVENT_PARTICIPANTS) {
        return ephemeralMessage("해당 이벤트에는 아직 참여자가 없습니다");
      }

      log.error("event-users 명령 처리 중 비즈니스 예외 발생", ex);
      return ephemeralMessage("요청 처리 중 오류가 발생했습니다. 서버 로그를 확인해주세요.");

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

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp);
  }
}