package com.github.soh.autocommit.grassKeeper.batch.tasklet;


import com.github.soh.autocommit.grassKeeper.batch.property.BatchProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutoCommitTasklet implements Tasklet {

    @PostConstruct
    public void init() {
        System.out.println("AutoCOmmitTasklet 빈 생성됨");
    }

    private final BatchProperties batchProperties;
    // application.yml 설정으로 이관
    //private static final String QUEUE_DIR = "C:\\queue";
    private static final String EMERGENCY_FILE = "emergency_commit.txt";

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String QUEUE_DIR = batchProperties.getQueueDir();
        File queueDir = new File(QUEUE_DIR);
        File[] files = queueDir.listFiles((dir, name) -> name.endsWith(".txt"));

        if (!queueDir.exists()) {
            System.out.println("❌ 디렉터리가 존재하지 않습니다: " + QUEUE_DIR);
            return RepeatStatus.FINISHED;
        }

        if (files == null || files.length == 0) {
            System.out.println("📭 처리할 파일이 없습니다.");
            return RepeatStatus.FINISHED;
        }

        for(File file : files) {
            System.out.println(file.getName());
        }

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
