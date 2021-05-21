package com.simplejob.core;

import java.util.List;
import java.util.Optional;

public interface StorageProvider {
    Optional<Job> getJobById(String id);

    void addJob(Job job);

    List<Job> getJobList();

    List<Job> getJobByStateAndSortedByPriority(JobStates jobStates);

    List<Job> getJobByStateAndSortedByPriority(JobStates jobState, int pageSize);

    void updateJobState(String id, JobStates jobState);
}
