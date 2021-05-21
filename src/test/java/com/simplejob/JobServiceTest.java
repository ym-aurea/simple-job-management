package com.simplejob;

import com.simplejob.core.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
public class JobServiceTest {

    private JobService jobService;

    @Autowired
    private StorageProvider storageProvider;

    @Value("${job-service.polling-interval}")
    private int pollingInterval;

    @Value("${job-service.job-pool-size}")
    private int jobPoolSize;

    @BeforeEach
    public void beforeEach() {
        TaskScheduler taskScheduler = new TaskSchedulerBuilder().poolSize(jobPoolSize).threadNamePrefix("job-service")
                .additionalCustomizers(ExecutorConfigurationSupport::initialize).build();
        BackgroundJobService backgroundJobService = new BackgroundJobServiceImpl(storageProvider, taskScheduler);
        JobServiceImpl jobServiceImpl = new JobServiceImpl(storageProvider, backgroundJobService);
        jobServiceImpl.setPollingInterval(pollingInterval);
        jobService = jobServiceImpl;
        jobService.start();
    }

    @AfterEach
    public void afterEach() {
        jobService.stop();
    }

    @Test
    public void givenSuccessImmediateJobWhenAddJobThenJobCompletedSuccessfully() {
        // Arrange
        Job simpleJob = createSuccessJob();

        // Act
        String jobId = jobService.addJob(simpleJob);

        // Assert
        await().atMost(2, SECONDS).until(() -> isJobStateValid(jobId, JobStates.SUCCESS));
    }

    @Test
    public void givenFailedImmediateJobWhenAddThenJobFailed() {
        // Arrange
        Job failedJob = createFailedJob();

        // Act
        String jobId = jobService.addJob(failedJob);

        // Assert
        await().atMost(2, SECONDS).until(() -> isJobStateValid(jobId, JobStates.FAILED));
    }

    @Test
    public void givenLongRunningJobWhenAddJobThenJobIsRunning() {
        // Arrange
        Job longRunningJob = createLongRunningJob();

        // Act
        String jobId = jobService.addJob(longRunningJob);

        // Assert
        await().atMost(2, SECONDS).until(() -> isJobStateValid(jobId, JobStates.RUNNING));
    }

    @Test
    public void givenSuccessFailedLongRunningJobsWhenAddJobThenExpectedResult() {
        // Arrange
        Job successJob = createSuccessJob();
        Job longRunningJob = createLongRunningJob();
        Job failedJob = createFailedJob();

        // Act
        String longRunningJobId = jobService.addJob(longRunningJob);
        String failedJobId = jobService.addJob(failedJob);
        String successJobId = jobService.addJob(successJob);

        // Assert
        await().atMost(2, SECONDS).until(() -> isJobStateValid(longRunningJobId, JobStates.RUNNING));
        await().atMost(2, SECONDS).until(() -> isJobStateValid(failedJobId, JobStates.FAILED));
        await().atMost(2, SECONDS).until(() -> isJobStateValid(successJobId, JobStates.SUCCESS));
    }

    @Test
    public void givenJobWithScheduleWhenAddJobThenJobIsRunning() {
        // Arrange
        Job cronJob = createCronJob();

        // Act
        String jobId = jobService.addJob(cronJob);

        // Assert
        await().atMost(2, SECONDS).until(() -> isJobStateValid(jobId, JobStates.RUNNING));
    }

    @Test
    public void givenLongRunningJobWhenCancelThenJobIsCancelled() throws Exception {
        // Arrange
        String jobId = jobService.addJob(createLongRunningJob());
        SECONDS.sleep(2);

        // Act
        jobService.cancelJob(jobId);

        // Assert
        await().atMost(5, SECONDS).until(() -> isJobStateValid(jobId, JobStates.FAILED));
    }

    private boolean isJobStateValid(String jobId, JobStates state) {
        Optional<Job> jobInfo = storageProvider.getJobById(jobId);
        return jobInfo.isPresent() && jobInfo.get().getState() == state;
    }

    private Job createSuccessJob() {
        return Job.builder()
                .name("success job")
                .jobClass(SimpleJob.class.getCanonicalName())
                .build();
    }

    private Job createFailedJob() {
        return Job.builder()
                .name("failed job")
                .jobClass(FailedJob.class.getCanonicalName())
                .build();
    }

    private Job createLongRunningJob() {
        return Job.builder()
                .name("long running job")
                .jobClass(LongRunningJob.class.getCanonicalName())
                .build();
    }

    private Job createCronJob() {
        return Job.builder()
                .name("simple job with cron")
                .cronExpression("*/1 * * * * *")
                .jobClass(SimpleJob.class.getCanonicalName())
                .build();
    }

}
