package com.example.limit.semaphoreblocker.model.property;

import com.example.limit.dto.Type;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;



@Data
@Configuration
@ConfigurationProperties(prefix = "internal.semaphore")
public class SemaphoreProperty {

    private List<SemaphoreLimit> limits;

    @Data
    public static class SemaphoreLimit {
        /**
         * name of manager
         */
        private Type name;

        /**
         * Count of permits
         */
        private Integer permit;

        /**
         * Time out in millis
         */
        private Integer timeOutMs;
    }
}
