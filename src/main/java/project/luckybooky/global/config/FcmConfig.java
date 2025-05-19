package project.luckybooky.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Configuration
@Slf4j
public class FcmConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount =
                        new FileInputStream(
                                "src/main/resources/secret/moviebooky-2009d-firebase-adminsdk-fbsvc-1527730d9b.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("✅ FirebaseApp 초기화 완료");
            }
        } catch (IOException e) {
            log.error("❌ FirebaseApp 초기화 실패", e);
            throw new BusinessException(ErrorCode.FCM_INITIALIZATION_FAILED);
        }
    }
}
