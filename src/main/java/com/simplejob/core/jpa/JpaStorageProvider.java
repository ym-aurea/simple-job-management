package com.simplejob.core.jpa;

import com.simplejob.core.Job;
import com.simplejob.core.JobStates;
import com.simplejob.core.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JpaStorageProvider implements StorageProvider {

    private final JobInfoRepository jobInfoRepository;

    @Override
    @Transactional
    public Optional<Job> getJobById(String id) {
        Optional<JobEntity> jobEntity = jobInfoRepository.findById(id);
        return jobEntity.map(JobEntity::toJobInfo);
    }

    @Override
    @Transactional
    public void addJob(Job job) {
        jobInfoRepository.save(JobEntity.of(job));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobList() {
        Iterable<JobEntity> jobEntities = jobInfoRepository.findAll();
        return toJobInfo(jobEntities);
    }

    private static List<Job> toJobInfo(Iterable<JobEntity> jobEntities) {
        List<Job> result = new ArrayList<>();
        for (JobEntity jobEntity : jobEntities) {
            result.add(JobEntity.toJobInfo(jobEntity));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobByStateAndSortedByPriority(JobStates jobStates) {
        Iterable<JobEntity> jobEntities = jobInfoRepository.findJobEntityListByState(jobStates,
                Sort.by(Sort.Direction.DESC, "priority"));
        return toJobInfo(jobEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> getJobByStateAndSortedByPriority(JobStates jobState, int pageSize) {
        Iterable<JobEntity> jobEntities = jobInfoRepository.findJobEntityListByState(jobState,
                PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "priority")));
        return toJobInfo(jobEntities);
    }

    @Override
    @Transactional
    public void updateJobState(String id, JobStates jobStates) {
        Optional<JobEntity> jobEntity = jobInfoRepository.findById(id);
        jobEntity.ifPresent((val) -> {
            val.setState(jobStates);
            jobInfoRepository.save(val);
        });
    }
}
