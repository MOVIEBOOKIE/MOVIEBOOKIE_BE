package project.luckybooky.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserType {

    /*  🎬 영화  */
    MOVIE_DETAIL_COLLECTOR (ContentCategory.MOVIE,   GroupType.A, "🎞 디테일 수집형 영화 덕후러"),
    MOVIE_TRENDY_VIEWER    (ContentCategory.MOVIE,   GroupType.B, "🍿 핫플릭스만 골라보는 감각 감상러"),

    /*  📺 드라마  */
    DRAMA_STORY_IMMERSER   (ContentCategory.DRAMA,   GroupType.A, "💜 대사에 숨멎하는 ‘서사 몰입러’"),
    DRAMA_HIGHLIGHT_HUNTER (ContentCategory.DRAMA,   GroupType.B, "💡 레전드 회차 ‘명장면 추적러’"),

    /*  😂 예능  */
    VARIETY_DETAIL_ANALYST (ContentCategory.VARIETY, GroupType.A, "🤣 레전드 찾아 삼만리 예능러"),
    VARIETY_MEME_COLLECTOR (ContentCategory.VARIETY, GroupType.B, "⚡ 짤줍 맛집 밈 사냥 헌터"),

    /*  🏀 스포츠  */
    SPORTS_FULL_SUPPORTER  (ContentCategory.SPORTS,  GroupType.A, "🏃 승부에 인생 건 몰입 응원러"),
    SPORTS_HIGHLIGHT_RABBIT(ContentCategory.SPORTS,  GroupType.B, "🧊 알잘딱깔센 스포츠러"),

    /*  🎤 콘서트  */
    CONCERT_STAGE_DIVER    (ContentCategory.CONCERT, GroupType.A, "🎤 떼창까지 준비된 '공연 몰입러'"),
    CONCERT_FANCAM_LOVER   (ContentCategory.CONCERT, GroupType.B, "🪩 레전드 직캠 모으는 요약 감상요정");

    private final ContentCategory category;
    private final GroupType group;
    private final String label;

    UserType(ContentCategory category, GroupType group, String label) {
        this.category = category;
        this.group    = group;
        this.label    = label;
    }
}
