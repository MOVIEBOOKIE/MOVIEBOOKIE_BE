package project.luckybooky.domain.notification.email.event;

import project.luckybooky.domain.event.entity.Event;

public record EventVenueConfirmedEvent(Event event, Long ticketId) {
}

