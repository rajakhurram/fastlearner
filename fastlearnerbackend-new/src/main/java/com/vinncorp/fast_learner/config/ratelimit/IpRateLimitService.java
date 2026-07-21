package com.vinncorp.fast_learner.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IpRateLimitService {

    private static final long ENTRY_TTL_MILLIS = Duration.ofHours(2).toMillis();

    private final IpRateLimitProperties properties;
    private final Map<String, BucketHolder> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(RateLimitType type, String clientIp) {
        String key = type.name() + ":" + clientIp;
        BucketHolder holder = buckets.computeIfAbsent(key, ignored -> new BucketHolder(createBucket(type)));
        holder.lastAccessMillis = System.currentTimeMillis();
        return holder.bucket.tryConsume(1);
    }

    @Scheduled(fixedRate = 3_600_000)
    public void evictStaleBuckets() {
        long cutoff = System.currentTimeMillis() - ENTRY_TTL_MILLIS;
        Iterator<Map.Entry<String, BucketHolder>> iterator = buckets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BucketHolder> entry = iterator.next();
            if (entry.getValue().lastAccessMillis < cutoff) {
                iterator.remove();
            }
        }
    }

    private Bucket createBucket(RateLimitType type) {
        IpRateLimitProperties.LimitConfig config = switch (type) {
            case LOGIN -> properties.getLogin();
            case COPILOT -> properties.getCopilot();
            case GRADER -> properties.getGrader();
        };

        Bandwidth limit = Bandwidth.classic(
                config.getLimitForPeriod(),
                Refill.greedy(config.getLimitForPeriod(), Duration.ofSeconds(config.getRefreshPeriodSeconds()))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private static final class BucketHolder {
        private final Bucket bucket;
        private volatile long lastAccessMillis = System.currentTimeMillis();

        private BucketHolder(Bucket bucket) {
            this.bucket = bucket;
        }
    }
}
