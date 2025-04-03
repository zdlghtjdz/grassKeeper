package com.github.soh.autocommit.grassKeeper.batch.tasklet;


import com.github.soh.autocommit.grassKeeper.batch.property.BatchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutoCommitTasklet implements Tasklet {


    private final BatchProperties batchProperties;
    // application.yml 설정으로 이관
    //private static final String QUEUE_DIR = "C:\\queue";
    private static final String EMERGENCY_FILE = "emergency_commit.txt";
    private String REPO_DIR;
    private String QUEUE_DIR;
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        QUEUE_DIR = batchProperties.getQueueDir();
        REPO_DIR = batchProperties.getRepoDir();
        File queueDir = new File(QUEUE_DIR);

        if(!queueDir.exists()) {
            System.out.println("queueDir.exist?");
        }
        if("".equals(REPO_DIR)) {
            System.out.println("REPO exist?");
            return RepeatStatus.FINISHED;
        }

        File[] files = queueDir.listFiles((dir, name) -> name.endsWith(".txt"));

        if(files == null || files.length == 0) {
            String fileName = "auto-" + System.currentTimeMillis() + ".txt";
            File newFile = new File(queueDir, fileName);

            try (FileWriter writer = new FileWriter(newFile)){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar c1 = Calendar.getInstance();

                writer.write( sdf.format(c1.getTime()) + " : emergency commit !");
            } catch (IOException exception) {
                throw new RuntimeException("파일 생성 실패!", exception);
            }

            files = new File[]{newFile};
        }

        Arrays.sort(files);
        File latestTask = files[files.length - 1];
        Files.copy(latestTask.toPath(), Paths.get(REPO_DIR, EMERGENCY_FILE), StandardCopyOption.REPLACE_EXISTING);

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
        //Runtime.getRuntime().exec("git add " + EMERGENCY_FILE);
        File repoDir = new File(REPO_DIR);

        Process add = Runtime.getRuntime().exec(new String[]{"git", "add", "."}, null, repoDir);
        int addExit = add.waitFor();

        try (BufferedReader err = new BufferedReader(new InputStreamReader(add.getErrorStream()))) {
            String line;
            while ((line = err.readLine()) != null) {
                System.err.println("git add error: " + line);
            }
        }

        //Runtime.getRuntime().exec("git commit -m \"긴급 자동 커밋: " + timestamp + "\"");
        Process commit = Runtime.getRuntime().exec(new String[]{"git", "commit", "-m", "긴급 자동 커밋: ", timestamp}, null, repoDir);
        int commitExit = commit.waitFor();

        try (BufferedReader err = new BufferedReader(new InputStreamReader(commit.getErrorStream()))) {
            String line;
            while ((line = err.readLine()) != null) {
                System.err.println("git commit error: " + line);
            }
        }

        Process push = Runtime.getRuntime().exec(new String[]{"git", "push"}, null, repoDir);
        int pushExit = push.waitFor();

        System.out.println("addExit : " + addExit);
        System.out.println("commitExit : " + commitExit);
        System.out.println("pushExit : " + pushExit);
        //Runtime.getRuntime().exec("git push");
    }
}
