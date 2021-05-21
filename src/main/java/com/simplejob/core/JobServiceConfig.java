package com.simplejob.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;

@Configuration
@PropertySource("classpath:application.properties")
public class JobServiceConfig {
    @Value("${job-service.job-pool-size}")
    private int jobPoolSize;

    @Bean
    public TaskScheduler scheduledExecutorService() {
        return new TaskSchedulerBuilder().poolSize(jobPoolSize).threadNamePrefix("job-service").build();
    }
}
