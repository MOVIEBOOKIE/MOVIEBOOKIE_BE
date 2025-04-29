package project.luckybooky.domain.event.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.luckybooky.global.entity.BaseEntity;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Location extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String locationName;
    private String address;
    private String locationImageUrl;
}
