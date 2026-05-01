package project.luckybooky.global.messaging.model.payload;

public record VenueRejectedMailPayload(
        Long eventId,
        Long hostUserId
) {
}
