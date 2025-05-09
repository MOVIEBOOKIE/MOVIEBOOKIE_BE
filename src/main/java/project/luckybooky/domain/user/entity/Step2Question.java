package project.luckybooky.domain.user.entity;

import lombok.Getter;

@Getter
public enum Step2Question {

    /* -------- Group A: 몰입/분위기형 -------- */
    STORY     ("📺", "스토리에 푹 빠져서 정주행하기 좋아해요",            GroupType.A),
    VISUAL    ("🎞", "영상미나 분위기가 예쁘면 끝까지 보게 돼요",          GroupType.A),
    REVIEW    ("🗣", "후기도 꼼꼼히 보고, 평점 높은 거 위주로 봐요",       GroupType.A),

    /* -------- Group B: 간편/트렌드형 -------- */
    DISCUSS   ("🫶", "함께 보고 각자의 감상을 나누는 걸 좋아해요",          GroupType.B),
    FRIEND_TAG("👯‍♀️", "친구들이 추천한 검증된 작품들은 무조건 챙겨봐요", GroupType.B),
    HOT_TREND ("🔥", "요즘 핫한 작품들은 놓칠 수 없어요",                  GroupType.B);

    private final String   emoji;
    private final String   answerText;
    private final GroupType group;   // A · B 판단용

    Step2Question(String emoji, String answerText, GroupType group) {
        this.emoji      = emoji;
        this.answerText = answerText;
        this.group      = group;
    }
}

