package com.github.soh.autocommit.grassKeeper.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoCommitScheduler {

    private final JobLauncher jobLauncher;
    private final Job autoCommitJob;


    @Scheduled(cron = "0 55 23 * * *") // 매일 22시 30분
    public void runAutoCommitJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(autoCommitJob, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
