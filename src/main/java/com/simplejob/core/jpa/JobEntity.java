package com.simplejob.core.jpa;

import com.simplejob.core.Job;
import com.simplejob.core.JobStates;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "job")
public class JobEntity {
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "class_name")
    private String className;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private JobStates state;

    @Column(name = "cronExpression")
    private String cronExpression;

    @Column(name = "priority")
    private int priority;

    public static JobEntity of(Job job) {
        JobEntity jobEntity = new JobEntity();
        jobEntity.setId(job.getId());
        jobEntity.setName(job.getName());
        jobEntity.setState(job.getState());
        jobEntity.setClassName(job.getJobClass());
        jobEntity.setCronExpression(job.getCronExpression());
        jobEntity.setPriority(job.getPriority());
        return jobEntity;
    }

    public static Job toJobInfo(JobEntity jobEntity) {
        return Job.builder()
                .id(jobEntity.getId())
                .name(jobEntity.name)
                .jobClass(jobEntity.getClassName())
                .cronExpression(jobEntity.getCronExpression())
                .state(jobEntity.getState())
                .priority(jobEntity.getPriority())
                .build();
    }
}
