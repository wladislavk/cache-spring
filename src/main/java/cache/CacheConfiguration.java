package cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheConfiguration {
    public enum EvictionPolicies {
        OLDEST_FIRST, NEWEST_FIRST, REJECT
    }

    private int numberOfSlots = 10000;
    private int defaultTtl = 3600;
    private EvictionPolicies evictionPolicy = EvictionPolicies.REJECT;

    public void setNumberOfSlots(int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    public void setDefaultTtl(int defaultTTL) {
        this.defaultTtl = defaultTTL;
    }

    public int getDefaultTtl() {
        return defaultTtl;
    }

    public void setEvictionPolicy(EvictionPolicies evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public EvictionPolicies getEvictionPolicy() {
        return evictionPolicy;
    }
}
