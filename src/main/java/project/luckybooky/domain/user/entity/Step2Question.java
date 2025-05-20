package project.luckybooky.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "STEP 2 – 콘텐츠 선택 기준")
public enum Step2Question {

    /* Group A: 몰입형 */
    @Schema(description = "📺 스토리에 푹 빠져서 정주행하기 좋아해요")
    STORY("📺", "스토리에 푹 빠져서 정주행하기 좋아해요", GroupType.A),

    @Schema(description = "🎞 영상미나 분위기가 예쁘면 끝까지 보게 돼요")
    VISUAL("🎞", "영상미나 분위기가 예쁘면 끝까지 보게 돼요", GroupType.A),

    @Schema(description = "🗣 후기도 꼼꼼히 보고, 평점 높은 거 위주로 봐요")
    REVIEW("🗣", "후기도 꼼꼼히 보고, 평점 높은 거 위주로 봐요", GroupType.A),

    /* Group B: 공유형 */
    @Schema(description = "🫶 함께 보고 각자의 감상을 나누는 걸 좋아해요")
    DISCUSS("🫶", "함께 보고 각자의 감상을 나누는 걸 좋아해요", GroupType.B),

    @Schema(description = "👯‍♀️ 친구들이 추천한 검증된 작품들은 무조건 챙겨봐요")
    FRIEND_TAG("👯‍♀️", "친구들이 추천한 검증된 작품들은 무조건 챙겨봐요", GroupType.B),

    @Schema(description = "🔥 요즘 핫한 작품들은 놓칠 수 없어요")
    HOT_TREND("🔥", "요즘 핫한 작품들은 놓칠 수 없어요", GroupType.B);

    private final String emoji;
    private final String answerText;
    private final GroupType group;

    Step2Question(String emoji, String answerText, GroupType group) {
        this.emoji = emoji;
        this.answerText = answerText;
        this.group = group;
    }
}
