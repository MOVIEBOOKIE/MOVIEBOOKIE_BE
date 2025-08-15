package project.luckybooky.domain.participation.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.mail-link")
public class MailLinkProps {
    private String secret;
    private long ttlSeconds = 86400;
    private String audience = "mail-link";
    private Boolean singleUse = true;
}
