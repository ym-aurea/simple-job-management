package com.simplejob.core;

public interface BackgroundJobService extends Runnable {

    void rescheduleRunningJobs();

    void shutdown();

    boolean cancelJob(String jobId);

    void cancelAllJobs();
}
