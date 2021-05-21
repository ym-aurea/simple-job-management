package com.simplejob.core.jpa;

import com.simplejob.core.JobStates;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JobInfoRepository extends PagingAndSortingRepository<JobEntity, String> {
    Iterable<JobEntity> findJobEntityListByState(JobStates jobState);

    Iterable<JobEntity> findJobEntityListByState(JobStates jobState, Sort sort);

    Page<JobEntity> findJobEntityListByState(JobStates jobState, Pageable pageable);
}
