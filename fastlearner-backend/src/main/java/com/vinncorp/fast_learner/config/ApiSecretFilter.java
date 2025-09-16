package com.vinncorp.fast_learner.config;

import com.vinncorp.fast_learner.models.api_client.ApiClient;
import com.vinncorp.fast_learner.repositories.api_client.ApiClientRepository;
import com.vinncorp.fast_learner.util.Constants.APIUrls;
import com.vinncorp.fast_learner.util.api_client.ApiClientValidator;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Component
public class ApiSecretFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new HashMap<>();
    private static final List<String> TRUSTED_ORIGINS = List.of(
            "staging.fastlearner.ai",
            "fastlearner.ai",
            "localhost"
    );

    public static final String CLIENT_ID_HEADER = "X-Client-ID";
    private static final String CLIENT_SECRET_HEADER = "X-Client-Secret";

    @Autowired
    private ApiClientRepository repo;

    public ApiSecretFilter() {
        // Initialize rate limits for specific API URLs
        buckets.put(APIUrls.UPLOADER + APIUrls.UPLOAD, createBucket(20, Duration.ofMinutes(1))); // 20 requests per minute
        buckets.put(APIUrls.UPLOADER + APIUrls.UPLOAD_FOR_REGENERATION, createBucket(20, Duration.ofMinutes(1))); // 20 requests per minute
        buckets.put(APIUrls.TOPIC_TYPE_API + APIUrls.GET_ALL_TOPIC_TYPE, createBucket(100, Duration.ofMinutes(1))); // 50 requests per minute
        buckets.put(APIUrls.COURSE_LEVEL_API + APIUrls.GET_ALL_COURSE_LEVEL, createBucket(100, Duration.ofMinutes(1))); // 50 requests per minute
        buckets.put(APIUrls.COURSE_CATEGORY_API + APIUrls.GET_ALL_COURSE_CATEGORY, createBucket(100, Duration.ofMinutes(1))); // 50 requests per minute
        buckets.put(APIUrls.COURSE_API + APIUrls.UNIQUE_COURSE_TITLE, createBucket(100, Duration.ofMinutes(1))); // 100 requests per minute
        buckets.put(APIUrls.COURSE_API + APIUrls.COURSE_URL, createBucket(100, Duration.ofMinutes(1))); // 100 requests per minute
        buckets.put(APIUrls.COURSE_API + APIUrls.CREATE_COURSE, createBucket(10, Duration.ofMinutes(1))); // 10 requests per minute
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        String userAgent = request.getHeader("User-Agent");
        String token = request.getHeader("Authorization");

        // Allow requests from trusted origins
//        if ("Payment".equalsIgnoreCase(userAgent) || (origin != null && TRUSTED_ORIGINS.stream().anyMatch(origin::contains))) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        filterChain.doFilter(request, response);
        return;
//        if (!isPublicRequest(request)) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Unauthorized: You can only access the APIs for creating a course.");
//        }

        // Validate Client ID and Secret for non-trusted origins
//        String clientId = request.getHeader(CLIENT_ID_HEADER);
//        String clientSecret = request.getHeader(CLIENT_SECRET_HEADER);
//
//        ApiClient apiClient = repo.findByClientId(clientId);
//
//        if (Objects.nonNull(apiClient) && ApiClientValidator.encrypt(token, clientSecret, apiClient.getApiSecret())) {
//            Bucket bucket = buckets.get(request.getRequestURI());
//            if (bucket != null) {
//                if (bucket.tryConsume(1)) {
//                    filterChain.doFilter(request, response);
//                } else {
//                    // Too many requests
//                    response.setStatus(HttpStatus.SC_TOO_MANY_REQUESTS);
//                    response.getWriter().write("Too many requests. Please try again later.");
//                    return;
//                }
//            }
//        } else {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Unauthorized: Invalid client credentials.");
//        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // List of public API paths
        List<String> publicPaths = Arrays.asList(
                APIUrls.UPLOADER,
                APIUrls.TOPIC_TYPE_API + APIUrls.GET_ALL_TOPIC_TYPE ,
                APIUrls.COURSE_LEVEL_API + APIUrls.GET_ALL_COURSE_LEVEL,
                APIUrls.COURSE_CATEGORY_API + APIUrls.GET_ALL_COURSE_CATEGORY,
                APIUrls.COURSE_API + APIUrls.UNIQUE_COURSE_TITLE,
                APIUrls.COURSE_API + APIUrls.COURSE_URL,
                APIUrls.COURSE_API + APIUrls.CREATE_COURSE,
                APIUrls.UPLOADER + APIUrls.UPLOAD_FOR_REGENERATION
        );

        // Check if the URI matches any public path
        for (String publicPath : publicPaths) {
            if (uri.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private Bucket createBucket(long capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, refillDuration));
        return Bucket.builder().addLimit(limit).build();
    }
}