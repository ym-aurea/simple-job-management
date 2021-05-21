package com.simplejob.core;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Job {
    private String id;
    private final String name;
    private final String jobClass;
    private String cronExpression;
    private JobStates state;
    private int priority;
}
