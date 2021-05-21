package com.simplejob.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class BackgroundJobServiceImpl implements BackgroundJobService {
    private final StorageProvider storageProvider;
    private final TaskScheduler taskScheduler;
    private final ConcurrentMap<String, Future<?>> runningJobs = new ConcurrentHashMap<>();

    @Override
    public void run() {
        processJobs();
        updateRunningJobs();
    }

    public void rescheduleRunningJobs() {
        List<Job> jobList = storageProvider.getJobByStateAndSortedByPriority(JobStates.RUNNING);
        processJobs(jobList);
    }

    public void shutdown() {
        ((ExecutorConfigurationSupport) taskScheduler).shutdown();
    }

    public boolean cancelJob(String jobId) {
        Future<?> future = runningJobs.get(jobId);
        if (future == null) {
            return false;
        }
        return future.cancel(true);
    }

    public void cancelAllJobs() {
        runningJobs.values().forEach(future -> future.cancel(true));
    }

    private void processJobs() {
        int availableWorkers = getAvailableWorkers();
        // cannot handle a new load
        if (availableWorkers <= 0) {
            return;
        }
        List<Job> jobList = storageProvider.getJobByStateAndSortedByPriority(JobStates.QUEUED, availableWorkers);
        processJobs(jobList);
    }

    private void processJobs(List<Job> jobList) {
        for (Job job : jobList) {
            try {
                Class<?> jobClass = Class.forName(job.getJobClass());
                ScheduledFuture<?> scheduledFuture;
                if (Strings.isEmpty(job.getCronExpression())) {
                    scheduledFuture = taskScheduler.schedule((Runnable) jobClass.getConstructor().newInstance(), taskScheduler.getClock().instant());
                } else {
                    scheduledFuture = taskScheduler.schedule((Runnable) jobClass.getConstructor().newInstance(), new CronTrigger(job.getCronExpression()));
                }
                runningJobs.put(job.getId(), scheduledFuture);
                storageProvider.updateJobState(job.getId(), JobStates.RUNNING);
            } catch (ClassNotFoundException ex) {
                log.error("job class is not found", ex);
            } catch (ReflectiveOperationException ex) {
                log.error("error during class instantiation", ex);
            }
        }
    }

    private int getAvailableWorkers() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = (ThreadPoolTaskScheduler) taskScheduler;
        return threadPoolTaskScheduler.getScheduledThreadPoolExecutor().getCorePoolSize() -
                threadPoolTaskScheduler.getActiveCount();
    }

    private void updateRunningJobs() {
        for (Map.Entry<String, Future<?>> entry : runningJobs.entrySet()) {
            try {
                if (entry.getValue().isDone()) {
                    entry.getValue().get();
                    completeJobWithStatus(entry.getKey(), JobStates.SUCCESS);
                }
            } catch (CancellationException ex) {
                log.warn("job {} was cancelled", entry.getKey(), ex);
                completeJobWithStatus(entry.getKey(), JobStates.FAILED);
            } catch (ExecutionException ex) {
                log.warn("job {} was failed", entry.getKey(), ex);
                completeJobWithStatus(entry.getKey(), JobStates.FAILED);
            } catch (InterruptedException ex) {
                log.warn("job {} was interrupted", entry.getKey(), ex);
                Thread.currentThread().interrupt();
                completeJobWithStatus(entry.getKey(), JobStates.FAILED);
            }
        }
    }

    private void completeJobWithStatus(String jobId, JobStates state) {
        storageProvider.updateJobState(jobId, state);
        runningJobs.remove(jobId);
    }
}
