package project.luckybooky.domain.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EventUserInfoWebhookDTO {

  private final String eventTitle;
  private final String date;
  private final String hostUsername;
  private final int participantCount;
  private final List<EventUserInfoDetail> participants;

  @Getter
  @AllArgsConstructor
  @Builder
  public static class EventUserInfoDetail {

    private final String username;
    private final String phoneNumber;
    private final String certificationEmail;
    private final String groupType;
  }
}

