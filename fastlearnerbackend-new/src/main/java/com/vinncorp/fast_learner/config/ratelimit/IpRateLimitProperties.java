package com.vinncorp.fast_learner.config.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ip-rate-limit")
public class IpRateLimitProperties {

    private LimitConfig login = new LimitConfig(5, 60);
    private LimitConfig copilot = new LimitConfig(10, 60);
    private LimitConfig grader = new LimitConfig(2, 10);

    @Data
    public static class LimitConfig {
        private int limitForPeriod;
        private int refreshPeriodSeconds;

        public LimitConfig() {
        }

        public LimitConfig(int limitForPeriod, int refreshPeriodSeconds) {
            this.limitForPeriod = limitForPeriod;
            this.refreshPeriodSeconds = refreshPeriodSeconds;
        }
    }
}
