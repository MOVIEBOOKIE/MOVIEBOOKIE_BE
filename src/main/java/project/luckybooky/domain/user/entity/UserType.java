package project.luckybooky.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public enum UserType {

    /* 🎬  영화  ─────────────────────── Group A(몰입형) / B(공유·트렌드형) */
    @Schema(description = "🎞 디테일 수집형 영화 몰입러")
    MOVIE_DETAIL_COLLECTOR (ContentCategory.MOVIE,   GroupType.A,
            "디테일 수집형 영화 몰입러",
            "영화의 한 장면, 대사까지 곱씹으며 아주 깊게 보는 스타일이네요! 스토리에 푹 빠져 정주행할 수 있는 이벤트들을 추천해 드려요."),

    @Schema(description = "🍿 함께 웃고 우는 영화 감상러")
    MOVIE_SHARED_EMOTER    (ContentCategory.MOVIE,   GroupType.B,
            "함께 웃고 우는 영화 감상러",
            "영화를 같이 보며 웃고 울고, 감정을 나누는 스타일이시네요! 모두가 함께 몰입할 수 있는 이벤트들을 추천해 드려요."),

    /* 📺  드라마  */
    @Schema(description = "💜 대사에 숨 멎는 드라마 몰입러")
    DRAMA_STORY_IMMERSER   (ContentCategory.DRAMA,   GroupType.A,
            "대사에 숨 멎는 드라마 몰입러",
            "감정선 따라 울고 웃고, 여운에 젖는 스타일이시네요! 서사에 빠져들 이벤트들을 추천해 드려요."),

    @Schema(description = "💡 함께하면 더 좋은 드라마 덕후러")
    DRAMA_SOCIAL_FAN       (ContentCategory.DRAMA,   GroupType.B,
            "함께하면 더 좋은 드라마 덕후러",
            "드라마를 친구들과 수다 떨며 보는 걸 좋아하시네요! 함께 즐길 이벤트들을 추천해 드려요."),

    /* 😂  예능  */
    @Schema(description = "🤣 레전드 찾아 삼만리 예능 심마니")
    VARIETY_DETAIL_ANALYST (ContentCategory.VARIETY, GroupType.A,
            "레전드 찾아 삼만리 예능 심마니",
            "웃음 포인트를 찾아 분석하는 열정파 스타일이시네요! 레전드 예능 이벤트를 추천해 드려요."),

    @Schema(description = "⚡ 리액션 보장 예능 공유러")
    VARIETY_REACTION_SHARER(ContentCategory.VARIETY, GroupType.B,
            "리액션 보장 예능 공유러",
            "예능을 주변에 공유하며 재미를 더하는 스타일이시네요! 함께 터질 이벤트들을 추천해 드려요."),

    /* 🏀  스포츠  */
    @Schema(description = "🏃 승부에 인생 건 스포츠 응원러")
    SPORTS_FULL_SUPPORTER  (ContentCategory.SPORTS,  GroupType.A,
            "승부에 인생 건 스포츠 응원러",
            "경기에 몰입해 응원하는 열정파 스타일이시네요! 레전드 경기를 추천해 드려요."),

    @Schema(description = "🧊 하나된 순간을 즐기는 스포츠 러버")
    SPORTS_MOMENT_LOVER    (ContentCategory.SPORTS,  GroupType.B,
            "하나된 순간을 즐기는 스포츠 러버",
            "사람들과 함께 응원하며 순간을 즐기는 스타일이시네요! 모두가 함께할 이벤트들을 추천해 드려요."),

    /* 🎤  콘서트  ── A/B 그룹을 기존과 반대로 교정 */
    @Schema(description = "🎤 떼창까지 준비된 콘서트 몰입러")
    CONCERT_STAGE_DIVER    (ContentCategory.CONCERT, GroupType.A,
            "떼창까지 준비된 콘서트 몰입러",
            "무대에 완전히 몰입해 떼창까지 즐기는 스타일이시네요! 최애에 더 빠질 이벤트들을 추천해 드려요."),

    @Schema(description = "🪩 레전드 직캠 모으는 콘서트 열정러")
    CONCERT_FANCAM_LOVER   (ContentCategory.CONCERT, GroupType.B,
            "레전드 직캠 모으는 콘서트 열정러",
            "명장면·직캠을 모아보는 트렌디한 스타일이시네요! 하이라이트 이벤트를 추천해 드려요.");

    private final ContentCategory category;
    private final GroupType       group;
    private final String          label;
    private final String          description;

    UserType(ContentCategory category, GroupType group, String label, String description) {
        this.category    = category;
        this.group       = group;
        this.label       = label;
        this.description = description;
    }
}