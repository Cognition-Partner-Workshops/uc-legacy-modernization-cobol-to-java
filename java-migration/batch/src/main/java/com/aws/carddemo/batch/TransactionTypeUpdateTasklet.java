package com.aws.carddemo.batch;

import com.aws.carddemo.domain.TransactionType;
import com.aws.carddemo.domain.TransactionTypeRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@StepScope
public class TransactionTypeUpdateTasklet implements Tasklet {
  private final TransactionTypeRepository repository;
  private final String input;

  public TransactionTypeUpdateTasklet(
      TransactionTypeRepository repository,
      @Value("#{jobParameters['input'] ?: ''}") String input) {
    this.repository = repository;
    this.input = input;
  }

  @Override
  @Transactional
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws Exception {
    if (input == null || input.isBlank()) return RepeatStatus.FINISHED;
    for (String line : Files.readAllLines(Path.of(input))) {
      String[] values = line.split("\\|", 3);
      if (values.length < 2) continue;
      String operation = values[0].trim().toUpperCase();
      String type = values[1].trim();
      if ("D".equals(operation)) repository.deleteById(type);
      else if (values.length == 3) {
        TransactionType row = repository.findById(type).orElseGet(TransactionType::new);
        row.setTypeCd(type);
        row.setTypeDesc(values[2].trim());
        repository.save(row);
      }
    }
    return RepeatStatus.FINISHED;
  }
}
