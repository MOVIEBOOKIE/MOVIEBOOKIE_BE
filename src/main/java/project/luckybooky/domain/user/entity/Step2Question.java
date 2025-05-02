package project.luckybooky.domain.user.entity;

public enum Step2Question {
    // Group A – 몰입형/분위기형
    STORY        (GroupType.A),
    VISUAL       (GroupType.A),
    REVIEW       (GroupType.A),

    // Group B – 간편형/트렌드형
    HOT_TREND    (GroupType.B),
    HIGHLIGHT    (GroupType.B),
    AUTO_PLAY    (GroupType.B);

    private final GroupType group;
    Step2Question(GroupType group) { this.group = group; }
    public GroupType getGroup() { return group; }

}
