package project.luckybooky.domain.notification.event.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EventVenueConfirmedEvent {
    private final Long eventId;
    private final Long hostUserId;
}
