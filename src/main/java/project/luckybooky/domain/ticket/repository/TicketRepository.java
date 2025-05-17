package project.luckybooky.domain.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.ticket.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
