package project.luckybooky.domain.ticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @Column(name = "media_title", length = 50, nullable = false)
    private String mediaTitle;

    @Column(name = "event_title", length = 50, nullable = false)
    private String eventTitle;

    @Column(name = "description")
    private String description;

    @Column(name = "type", length = 20, nullable = false)
    private String type;

    @Column(name = "location", length = 50, nullable = false)
    private String location;

    @Column(name = "address", length = 50, nullable = false)
    private String address;

    @Column(name = "scheduled_at", length = 50, nullable = false)
    private String scheduledAt;

    @Column(name = "time", length = 20, nullable = false)
    private String time;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "host_name", length = 20, nullable = false)
    private String hostName;

    @Column(name = "event_image_url", length = 512)
    private String eventImageUrl;

    @Column(name = "participants", nullable = false)
    private Integer participants;

    @ManyToMany
    @JoinTable(
            name = "user_ticket",
            joinColumns = @JoinColumn(name = "ticket_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> userList;
}
