package project.luckybooky.domain.user.entity;

import lombok.Getter;

@Getter
public enum Step1Question {

    STRESSED      ("💼", "하루하루가 전쟁이에요."),
    LAZY    ("🔁", "똑같은 하루의 반복, 좀 지루해요."),
    HAPPY("🌍", "매일이 새롭고 행복해요."),
    SMILE        ("🗣", "누군가랑 웃고 수다 떨고 싶어요."),
    RELAXED       ("☕", "한가하고 마음도 꽤 여유로워요.");

    private final String emoji;
    private final String answerText;

    Step1Question(String emoji, String answerText) {
        this.emoji      = emoji;
        this.answerText = answerText;
    }
}

