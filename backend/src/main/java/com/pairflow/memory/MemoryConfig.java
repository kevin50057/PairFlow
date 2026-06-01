package com.pairflow.memory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryConfig {

    /** Fallback used until the Album module provides a @Primary {@link MemoryProvider}. */
    @Bean
    public MemoryProvider noopMemoryProvider() {
        return (coupleId, viewerId) -> null;
    }
}
