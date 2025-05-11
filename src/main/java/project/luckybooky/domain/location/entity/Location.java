package project.luckybooky.domain.location.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.domain.location.entity.type.AvailableMediaType;
import project.luckybooky.domain.location.entity.type.LocationKeyword;
import project.luckybooky.global.entity.BaseEntity;

import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Location extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_name", length = 50, nullable = false)
    private String locationName;

    @Column(name = "address", length = 50, nullable = false)
    private String address;

    @Column(name = "location_image_url", length = 512)
    private String locationImageUrl;

    @Column(name = "price_per_hour", nullable = false)
    private Integer pricePerHour;

    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @Column(name = "has_disabled_seat", nullable = false)
    private Boolean hasDisabledSeat;

    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(name = "location_keyword",
            joinColumns = @JoinColumn(name = "location_id")
    )
    private Set<LocationKeyword> locationKeywordList;

    @Enumerated(EnumType.STRING)
    @Column(name = "available_media_type", nullable = false)
    private AvailableMediaType availableMediaType;

    @Column(name = "available_times", nullable = false)
    private Integer availableTimes;

    @Column(name = "is_Start_time_restricted", nullable = false)
    private Boolean IsStartTimeRestricted;

    @ElementCollection
    @CollectionTable(name = "allowed_Start_time",
            joinColumns = @JoinColumn(name = "location_id")
    )
    private Set<String> allowedStartTimes;
}
