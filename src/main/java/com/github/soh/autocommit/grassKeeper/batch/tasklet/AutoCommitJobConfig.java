package com.github.soh.autocommit.grassKeeper.batch.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AutoCommitJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AutoCommitTasklet autoCommitTasklet;

    @Bean
    public Job autoCommitJob() {
        return new JobBuilder("autoCommitJob", jobRepository)
                .start(autoCommitStep())
                .build();
    }
    @Bean
    public Step autoCommitStep() {
        return new StepBuilder("autoCommitStep", jobRepository)
                .tasklet(autoCommitTasklet, transactionManager)
                .build();
    }
}
