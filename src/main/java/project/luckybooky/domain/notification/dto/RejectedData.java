package project.luckybooky.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RejectedData {
    private String eventTitle;
    private String hostName;
    private String homeUrl;
}
