package project.luckybooky.domain.user.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import project.luckybooky.domain.ticket.entity.Ticket;
import project.luckybooky.global.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "certification_email")
    private String certificationEmail;

    @Column(name = "username")
    private String username;

    @Column(name = "profile_image")
    private String profileImage;

    @Lob
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Lob
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "host_experience_count", nullable = false)
    private int hostExperienceCount = 0;

    @Column(name = "participation_experience_count", nullable = false)
    private int participationExperienceCount = 0;

    @Column(name = "phone_number", length = 20, unique = true)
    @Pattern(regexp = "^\\+?[0-9\\-]{8,20}$", message = "유효한 전화번호 형식이 아닙니다.")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type")
    private GroupType groupType;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "recruitment", nullable = false)
    @Builder.Default
    private Integer recruitment = 0;

    @Column(name = "participation", nullable = false)
    @Builder.Default
    private Integer participation = 0;

    public void updateExperience(Integer type) {
        if (type == 0) {
            recruitment += 1;
        } else {
            participation += 1;
        }
    }

}