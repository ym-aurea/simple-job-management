package com.simplejob.core.jpa;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableJpaRepositories(basePackages = "com.simplejob.core.jpa")
@EnableTransactionManagement
public class PersistenceConfig {
}
