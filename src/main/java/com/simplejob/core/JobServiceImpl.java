package com.simplejob.core;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final StorageProvider storageProvider;
    private final ScheduledExecutorService pollingScheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final BackgroundJobService backgroundJobService;
    @Value("${job-service.polling-interval}")
    @Setter
    private int pollingInterval;

    public void start() {
        backgroundJobService.rescheduleRunningJobs();
        pollingScheduledExecutorService.scheduleWithFixedDelay(backgroundJobService, 0, pollingInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        backgroundJobService.shutdown();
        stop(pollingScheduledExecutorService);
    }

    private void stop(ExecutorService executorService) {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("executor service is interrupted during shutdown", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String addJob(Job job) {
        job.setId(UUID.randomUUID().toString());
        job.setState(JobStates.QUEUED);
        storageProvider.addJob(job);
        return job.getId();
    }

    @Override
    public boolean cancelJob(String jobId) {
        return backgroundJobService.cancelJob(jobId);
    }
}
