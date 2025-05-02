package project.luckybooky.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserType {
    // 🎬 영화
    MOVIE_DETAIL_COLLECTOR("🎞 디테일 수집형 영화 덕후러",    ContentCategory.MOVIE,   GroupType.A),
    MOVIE_HOTFLIX("🍿 핫플릭스만 골라보는 감각 감상러",      ContentCategory.MOVIE,   GroupType.B),

    // 📺 드라마
    DRAMA_SERIES_IMMERSION("💜 대사에 숨멎하는 ‘서사 몰입러’", ContentCategory.DRAMA,   GroupType.A),
    DRAMA_HIGHLIGHT_TRACKER("💡 명장면 추적러",             ContentCategory.DRAMA,   GroupType.B),

    // 😂 예능
    VARIETY_LEGEND_HUNTER("🤣 레전드 찾아 삼만리 예능러",    ContentCategory.VARIETY, GroupType.A),
    VARIETY_MEME_HUNTER("⚡ 밈 사냥 헌터",                   ContentCategory.VARIETY, GroupType.B),

    // 🏀 스포츠
    SPORTS_FULL_ENGAGER("🏃 승부에 인생 건 몰입 응원러",    ContentCategory.SPORTS,  GroupType.A),
    SPORTS_HIGHLIGHT_RABBIT("🧊 알잘딱깔센 스포츠러",       ContentCategory.SPORTS,  GroupType.B),

    // 🎤 콘서트
    CONCERT_PERFORMANCE_IMMERSION("🎤 공연 몰입러",        ContentCategory.CONCERT, GroupType.A),
    CONCERT_SUMMARY_QUEEN("🪩 요약 감상요정",               ContentCategory.CONCERT, GroupType.B);

    private final String label;
    private final ContentCategory category;
    private final GroupType group;

    UserType(String label, ContentCategory category, GroupType group) {
        this.label = label;
        this.category = category;
        this.group = group;
    }
}
