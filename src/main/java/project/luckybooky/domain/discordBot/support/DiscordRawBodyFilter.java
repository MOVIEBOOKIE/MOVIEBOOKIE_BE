package project.luckybooky.domain.discordBot.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Profile({"dev", "prod"})
public class DiscordRawBodyFilter extends OncePerRequestFilter {

  public static final String ATTR_DISCORD_RAW_BODY = "DISCORD_RAW_BODY";

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !(request.getRequestURI().equals("/discord/interactions")
        && "POST".equalsIgnoreCase(request.getMethod()));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    byte[] bodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
    String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);

    CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request, bodyBytes);
    wrapped.setAttribute(ATTR_DISCORD_RAW_BODY, bodyString);

    filterChain.doFilter(wrapped, response);
  }
}