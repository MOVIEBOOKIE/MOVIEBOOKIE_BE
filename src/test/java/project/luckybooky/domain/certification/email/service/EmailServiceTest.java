package project.luckybooky.domain.certification.email.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import project.luckybooky.domain.certification.email.dto.request.EmailRequestDTO;
import project.luckybooky.domain.certification.email.util.EmailCertificationUtil;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;
import project.luckybooky.global.redis.SmsCertificationCache;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final Duration CODE_TTL = Duration.ofMinutes(3);

    @Mock
    private EmailCertificationUtil mailUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SmsCertificationCache cache;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        TaskExecutor directExecutor = Runnable::run;
        emailService = new EmailService(
                mailUtil,
                directExecutor,
                userRepository,
                new SimpleMeterRegistry(),
                cache
        );
    }

    @Test
    @DisplayName("인증번호 발송 성공 시 메일 유틸이 호출된다")
    void sendCode_success_invokesMailUtil() {
        String email = "test@example.com";
        EmailRequestDTO dto = new EmailRequestDTO();
        dto.setEmail(email);

        when(cache.store(eq("otp:email:" + email + ":lock"), eq("1"), any(Duration.class))).thenReturn(true);

        emailService.sendCode(dto);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(cache).put(eq("otp:email:" + email), codeCaptor.capture(), eq(CODE_TTL));
        verify(mailUtil).sendMail(eq(email), eq(codeCaptor.getValue()));
        assertThat(codeCaptor.getValue()).matches("\\d{4}");
    }

    @Test
    @DisplayName("중복 발송 락 획득 실패 시 CERTIFICATION_DUPLICATED 예외가 발생한다")
    void sendCode_fail_whenDuplicatedLock() {
        String email = "test@example.com";
        EmailRequestDTO dto = new EmailRequestDTO();
        dto.setEmail(email);

        when(cache.store(eq("otp:email:" + email + ":lock"), eq("1"), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> emailService.sendCode(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CERTIFICATION_DUPLICATED);

        verify(cache, never()).put(any(), any(), any(Duration.class));
        verify(mailUtil, never()).sendMail(any(), any());
    }
}
