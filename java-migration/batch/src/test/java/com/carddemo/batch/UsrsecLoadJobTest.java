package com.carddemo.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.domain.entity.Usrsec;
import com.carddemo.domain.repository.UsrsecRepository;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CardDemoBatchApplication.class)
class UsrsecLoadJobTest {
    private static final Path SEED = Path.of("../app/data/EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS");

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job usrsecLoadJob;
    @Autowired
    private UsrsecRepository usrsecRepository;

    @BeforeEach
    void clearUsers() {
        usrsecRepository.deleteAll();
    }

    @Test
    void loadsAllFixedLengthEbcdicRecords() throws Exception {
        JobExecution execution = jobLauncher.run(
                usrsecLoadJob,
                new JobParametersBuilder()
                        .addString("inputFile", SEED.toAbsolutePath().toString())
                        .addString("run", UUID.randomUUID().toString())
                        .toJobParameters());

        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
        assertThat(usrsecRepository.count()).isEqualTo(10);
        Usrsec admin = usrsecRepository.findById("ADMIN001").orElseThrow();
        assertThat(admin.getSecUsrFname()).isEqualTo("MARGARET");
        assertThat(admin.getSecUsrLname()).isEqualTo("GOLD");
        assertThat(admin.getSecUsrPwd()).isEqualTo("PASSWORD");
        assertThat(admin.getSecUsrType()).isEqualTo("A");
        Usrsec user = usrsecRepository.findById("USER0001").orElseThrow();
        assertThat(user.getSecUsrType()).isEqualTo("U");
    }
}
