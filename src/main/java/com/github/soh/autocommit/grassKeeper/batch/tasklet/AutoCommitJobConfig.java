package com.github.soh.autocommit.grassKeeper.batch.tasklet;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AutoCommitJobConfig {

    @PostConstruct
    public void init() {
        System.out.println("AutoCommitJobConfig 초기화 완료");
    }
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

    @Bean
    public ApplicationRunner jobLauncherRunner(JobLauncher jobLauncher, Job autoCommitJob) {
        return args -> {
            System.out.println("ApplicationRunner : Job 수동 실행 시작");
            JobExecution execution = jobLauncher.run(autoCommitJob, new JobParameters());
            System.out.println("job 실행 상태 : " + execution.getStatus());
        };
    }
}
