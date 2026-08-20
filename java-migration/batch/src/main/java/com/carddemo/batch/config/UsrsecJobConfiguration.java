package com.carddemo.batch.config;

import com.carddemo.batch.ebcdic.FixedLengthEbcdicReader;
import com.carddemo.batch.ebcdic.UsrsecRecord;
import com.carddemo.domain.entity.Usrsec;
import com.carddemo.domain.repository.UsrsecRepository;
import java.nio.file.Path;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UsrsecJobConfiguration {
    @Bean
    @StepScope
    FixedLengthEbcdicReader usrsecReader(
            @Value("#{jobParameters['inputFile']}") String inputFile,
            @Value("${carddemo.usrsec.input-file:}") String defaultInputFile) {
        String resolvedInputFile = inputFile == null || inputFile.isBlank()
                ? defaultInputFile
                : inputFile;
        if (resolvedInputFile == null || resolvedInputFile.isBlank()) {
            throw new IllegalArgumentException(
                    "USRSEC load requires an input path: provide job parameter 'inputFile' "
                            + "or property 'carddemo.usrsec.input-file'.");
        }
        return new FixedLengthEbcdicReader(Path.of(resolvedInputFile));
    }

    @Bean
    ItemProcessor<UsrsecRecord, Usrsec> usrsecProcessor() {
        return record -> new Usrsec(
                record.userId(),
                record.firstName(),
                record.lastName(),
                record.password(),
                record.userType());
    }

    @Bean
    ItemWriter<Usrsec> usrsecWriter(UsrsecRepository repository) {
        return chunk -> repository.saveAll(chunk.getItems());
    }

    @Bean
    Step usrsecLoadStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FixedLengthEbcdicReader usrsecReader,
            ItemProcessor<UsrsecRecord, Usrsec> usrsecProcessor,
            ItemWriter<Usrsec> usrsecWriter) {
        return new StepBuilder("usrsecLoadStep", jobRepository)
                .<UsrsecRecord, Usrsec>chunk(10, transactionManager)
                .reader(usrsecReader)
                .processor(usrsecProcessor)
                .writer(usrsecWriter)
                .build();
    }

    @Bean
    Job usrsecLoadJob(JobRepository jobRepository, Step usrsecLoadStep) {
        return new JobBuilder("usrsecLoadJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(usrsecLoadStep)
                .build();
    }
}
