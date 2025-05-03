package project.luckybooky.domain.user.entity;

import lombok.Getter;

@Getter
public enum Step1Question {

    STRESSED      ("💼", "하루하루가 전쟁이에요."),
    REPETITIVE    ("🔁", "똑같은 하루의 반복, 좀 지루해요."),
    SEEKING_THRILL("🌍", "색다른 자극이 그리워요."),
    CHATTY        ("🗣", "그냥 누군가랑 웃고 수다 떨고 싶어요."),
    RELAXED       ("☕", "요즘은 한가하고, 마음도 꽤 여유로워요."),
    LAZY          ("🛋", "아무 생각 없이 쉬고만 싶어요.");

    private final String emoji;
    private final String answerText;

    Step1Question(String emoji, String answerText) {
        this.emoji      = emoji;
        this.answerText = answerText;
    }
}

