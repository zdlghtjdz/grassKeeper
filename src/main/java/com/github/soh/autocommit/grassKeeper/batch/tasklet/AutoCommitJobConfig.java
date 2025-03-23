package com.github.soh.autocommit.grassKeeper.batch.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("deprecation")
@Configuration
@RequiredArgsConstructor
public class AutoCommitJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final AutoCommitTasklet autoCommitTasklet;

    @Bean
    public Job autoCommitJob() {
        return jobBuilderFactory.get("autoCommitJob")
                .start(autoCommitStep())
                .build();
    }
    @Bean
    public Step autoCommitStep() {
        return stepBuilderFactory.get("autoCommitStep")
                .tasklet(autoCommitTasklet)
                .build();
    }
}
