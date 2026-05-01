package project.luckybooky.global.messaging.model.payload;

public record VenueConfirmedMailPayload(
        Long eventId,
        Long hostUserId
) {
}
