package project.luckybooky.domain.clova.converter;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import project.luckybooky.domain.clova.dto.ChatCompletionRequestDTO;

public class ClovaConverter {

    public static List<ChatCompletionRequestDTO.Message> buildKeywordPrompt(String product, String category) {
        String system = "제품: " + product + "\n카테고리: " + category +
                "\n### 지시사항: ..."; // abbreviated instructions
        return List.of(
                new ChatCompletionRequestDTO.Message("system", system),
                new ChatCompletionRequestDTO.Message("assistant", "")
        );
    }

    public static List<ChatCompletionRequestDTO.Message> buildUserTypePrompt(Map<String, String> answers) {
        StringBuilder sb = new StringBuilder();
        sb.append("아래 응답으로 UserType 선택\n");
        // append enum list and answers...
        return List.of(
                new ChatCompletionRequestDTO.Message("system", sb.toString()),
                new ChatCompletionRequestDTO.Message("assistant", "")
        );
    }

    public static List<String> parseLines(String content) {
        return content.lines()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toList());
    }

    public static String parseUserType(String content) {
        return content.split(":")[1].trim();
    }
}

