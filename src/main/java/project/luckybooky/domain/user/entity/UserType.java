package project.luckybooky.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public enum UserType {

    // 🎞 MOVIE
    @Schema(description = "🎞 디테일 수집형 영화 몰입러")
    MOVIE_DETAIL_COLLECTOR(ContentCategory.MOVIE, GroupType.A,
            "디테일 수집형 영화 몰입러",
            "영화의 한 장면, 대사까지 곱씹으며 아주 깊게 보는 스타일이네요!",
            "스토리에 푹 빠져 정주행할 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    @Schema(description = "🍿 함께 웃고 우는 영화 감상러")
    MOVIE_SHARED_EMOTER(ContentCategory.MOVIE, GroupType.B,
            "함께 웃고 우는 영화 감상러",
            "함께 웃고 울 수 있는 영화를 선호하는 스타일이시네요!",
            "모두가 함께 푹 빠져 볼 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    // 📺 DRAMA
    @Schema(description = "💜 대사에 숨 멎는 드라마 몰입러")
    DRAMA_STORY_IMMERSER(ContentCategory.DRAMA, GroupType.A,
            "대사에 숨 멎는 드라마 몰입러",
            "드라마의 감정선 따라 울고 웃고, 여운에 젖는 스타일이시네요!",
            "멜랑꼴리한 기분에 푹 빠질 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    @Schema(description = "💡 함께하면 더 좋은 드라마 덕후러")
    DRAMA_SOCIAL_FAN(ContentCategory.DRAMA, GroupType.B,
            "함께하면 더 좋은 드라마 덕후러",
            "드라마를 함께 보고 대화하며 더 몰입하는 스타일이시네요!",
            "친구와 함께 재밌게 볼 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    // 😂 VARIETY
    @Schema(description = "🤣 레전드 찾아 삼만리 예능 심마니")
    VARIETY_DETAIL_ANALYST(ContentCategory.VARIETY, GroupType.A,
            "레전드 찾아 삼만리 예능 심마니",
            "예능의 웃음 포인트를 분석하며 레전드를 골라 보는 스타일이시네요!",
            "재밌고 유명한 이벤트들을 모아 추천해 드릴게요!"),

    @Schema(description = "⚡ 리액션 보장 예능 공유러")
    VARIETY_REACTION_SHARER(ContentCategory.VARIETY, GroupType.B,
            "리액션 보장 예능 공유러",
            "예능을 주변에 공유하고 함께하며 재미를 더하는 스타일이시네요!",
            "모두가 함께 빠져 즐길 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    // 🏀 SPORTS
    @Schema(description = "🏃 승부에 인생 건 스포츠 응원러")
    SPORTS_FULL_SUPPORTER(ContentCategory.SPORTS, GroupType.A,
            "승부에 인생 건 스포츠 응원러",
            "스포츠에 인생을 거신 몰입 응원러 스타일이시네요!",
            "가히 ‘레전드’라 불릴 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    @Schema(description = "🧊 하나된 순간을 즐기는 스포츠 러버")
    SPORTS_MOMENT_LOVER(ContentCategory.SPORTS, GroupType.B,
            "하나된 순간을 즐기는 스포츠 러버",
            "스포츠를 함께 응원하며, 순간 자체를 즐기는 스타일이시네요!",
            "모두가 함께 빠져 즐길 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    // 🎤 CONCERT
    @Schema(description = "🎤 떼창까지 준비된 콘서트 몰입러")
    CONCERT_STAGE_DIVER(ContentCategory.CONCERT, GroupType.A,
            "떼창까지 준비된 콘서트 몰입러",
            "최애의 눈빛 하나에도 마음이 요동쳐 진심으로 몰입하는 스타일이시네요!",
            "내 최애에게 더 푹 빠질 수 있는 이벤트들을 모아 추천해 드릴게요!"),

    @Schema(description = "🔥 함께 환호하는 콘서트 열정러")
    CONCERT_FANCAM_LOVER(ContentCategory.CONCERT, GroupType.B,
            "함께 환호하는 콘서트 열정러",
            "친구들과 함께 가슴 뛰는 순간을 환호하고 회상하는 스타일이시네요!",
            "모두가 함께 빠져 즐길 수 있는 이벤트들을 모아 추천해 드릴게요!");

    // ===== Fields =====
    private final ContentCategory category;
    private final GroupType group;
    private final String title;       // 카드 타이틀
    private final String label;       // 사용자 성향 설명 (상단 문장)
    private final String description; // 카드 하단 문장

    UserType(ContentCategory category, GroupType group, String title, String label, String description) {
        this.category = category;
        this.group = group;
        this.title = title;
        this.label = label;
        this.description = description;
    }
}
