package project.luckybooky.domain.ticket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.luckybooky.domain.ticket.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t JOIN t.userList u WHERE u.id = :userId")
    Page<Ticket> findTicketsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM user_ticket WHERE user_id = :userId", nativeQuery = true)
    Integer countTicketByUserId(@Param("userId") Long userId);

    Ticket findByEventId(Long eventId);
}
