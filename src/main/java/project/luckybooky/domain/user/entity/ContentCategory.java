package project.luckybooky.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "STEP 3 – 선호 콘텐츠 유형")
public enum ContentCategory {

    @Schema(description = "🎬 영화")
    MOVIE,

    @Schema(description = "📺 드라마")
    DRAMA,

    @Schema(description = "😂 예능")
    VARIETY,

    @Schema(description = "🏀 스포츠")
    SPORTS,

    @Schema(description = "🎤 콘서트")
    CONCERT
}
