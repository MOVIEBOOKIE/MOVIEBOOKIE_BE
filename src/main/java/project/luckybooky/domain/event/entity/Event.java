package project.luckybooky.domain.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import project.luckybooky.domain.event.entity.type.RecruitmentStatus;
import project.luckybooky.global.entity.BaseEntity;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@DynamicInsert
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "title", length = 20, nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "recruitment_start", nullable = false)
    private LocalDate recruitmentStart;

    @Column(name = "recruitment_end", nullable = false)
    private LocalDate recruitmentEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "recruitment_status", columnDefinition = "VARCHAR(20) default 'IN_PROGRESS'")
    private RecruitmentStatus recruitmentStatus;

    @Column(name = "estimated_status", columnDefinition = "INT default '0'")
    private Integer estimatedPrice;

    @Column(name = "poster_image_url", nullable = false)
    private String posterImageUrl;

    @Column(name = "min_participants", columnDefinition = "INT default '0'")
    private Integer minParticipants;

    @Column(name = "max_participants", columnDefinition = "INT default '0'")
    private Integer maxParticipants;

    @Column(name = "current_participants", columnDefinition = "INT default '0'")
    private Integer currentParticipants;
}
