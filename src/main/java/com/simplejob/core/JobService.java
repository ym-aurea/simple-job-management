package com.simplejob.core;

public interface JobService {
    void start();

    void stop();

    String addJob(Job job);

    boolean cancelJob(String jobId);
}
