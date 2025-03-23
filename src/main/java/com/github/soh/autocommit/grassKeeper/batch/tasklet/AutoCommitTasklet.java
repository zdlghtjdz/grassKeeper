package com.github.soh.autocommit.grassKeeper.batch.tasklet;


import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Queue;

@Slf4j
@Component
public class AutoCommitTasklet implements Tasklet {

    private static final String QUEUE_DIR = "C:\\queue";
    private static final String EMERGENCY_FILE = "emergency_commit.txt";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        File queueDir = new File(QUEUE_DIR);
        File[] files = queueDir.listFiles((dir, name) -> name.endsWith(".txt"));

        if(files == null || files.length == 0) {
            log.info("대기 중인 작업 없음");
            return RepeatStatus.FINISHED;
        }

        Arrays.sort(files);
        File latestTask = files[files.length - 1];

        Files.copy(latestTask.toPath(), Paths.get(EMERGENCY_FILE), StandardCopyOption.REPLACE_EXISTING);

        commitToGit();

        boolean deleted = latestTask.delete();
        if(!deleted) {
            log.warn("작업 파일 삭제 실패: " + latestTask.getName());
        }

        log.info("긴급 커밋 완료!");
        return RepeatStatus.FINISHED;

    }

    private void commitToGit() throws Exception {
        String timestamp = LocalDateTime.now().toString();
        Runtime.getRuntime().exec("git add " + EMERGENCY_FILE);
        Runtime.getRuntime().exec("git commit -m \"긴급 자동 커밋: " + timestamp + "\"");
        Runtime.getRuntime().exec("git push");
    }
}
