package com.vinncorp.fast_learner.config.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.Message;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final IpRateLimitService rateLimitService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<RateLimitRule> rule = resolveRule(request);
        if (rule.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = IpAddressResolver.resolve(request);
        if (!rateLimitService.tryConsume(rule.get().type(), clientIp)) {
            writeTooManyRequests(response, rule.get().message());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Optional<RateLimitRule> resolveRule(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return Optional.empty();
        }

        String path = normalizePath(request.getRequestURI());

        if (path.equals(APIUrls.AUTHENTICATION_MAIN + APIUrls.LOCAL_LOGIN)
                || path.equals(APIUrls.AUTHENTICATION_MAIN + APIUrls.SOCIAL_LOGIN)) {
            return Optional.of(new RateLimitRule(
                    RateLimitType.LOGIN,
                    "Too many login attempts, please try again later."
            ));
        }

        if (path.equals(APIUrls.CHAT) || path.equals(APIUrls.CHAT + "/")) {
            return Optional.of(new RateLimitRule(
                    RateLimitType.COPILOT,
                    "Too many requests, please try again later."
            ));
        }

        if (path.equals(APIUrls.AI_RESULT + APIUrls.CREATE_AI_GRADER)) {
            return Optional.of(new RateLimitRule(
                    RateLimitType.GRADER,
                    "Too many requests, please try again later."
            ));
        }

        return Optional.empty();
    }

    private String normalizePath(String uri) {
        if (uri == null || uri.isBlank()) {
            return "";
        }
        if (uri.length() > 1 && uri.endsWith("/")) {
            return uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    private void writeTooManyRequests(HttpServletResponse response, String message) throws IOException {
        Message<String> body = new Message<String>()
                .setStatus(HttpStatus.TOO_MANY_REQUESTS.value())
                .setCode(HttpStatus.TOO_MANY_REQUESTS.name())
                .setMessage(message);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }

    private record RateLimitRule(RateLimitType type, String message) {
    }
}
