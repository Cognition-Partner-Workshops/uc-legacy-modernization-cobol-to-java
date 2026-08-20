package com.aws.carddemo.batch;

import com.aws.carddemo.domain.PendingAuthDetail;
import com.aws.carddemo.domain.PendingAuthDetailRepository;
import com.aws.carddemo.domain.PendingAuthSummary;
import com.aws.carddemo.domain.PendingAuthSummaryRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthorizationPurgeTasklet implements Tasklet {
  private final PendingAuthDetailRepository details;
  private final PendingAuthSummaryRepository summaries;
  private final int expiryDays;

  public AuthorizationPurgeTasklet(
      PendingAuthDetailRepository details,
      PendingAuthSummaryRepository summaries,
      @Value("${carddemo.authorization.expiry-days:5}") int expiryDays) {
    this.details = details;
    this.summaries = summaries;
    this.expiryDays = expiryDays;
  }

  @Override
  @Transactional
  public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
    String cutoff =
        LocalDate.now().minusDays(expiryDays).format(DateTimeFormatter.ofPattern("yyMMdd"));
    for (PendingAuthDetail detail : details.findAll()) {
      if (detail.getAuthDate().compareTo(cutoff) < 0
          && !("00".equals(detail.getAuthRespCode()) || "P".equals(detail.getMatchStatus()))) {
        details.delete(detail);
      }
    }
    for (PendingAuthSummary summary : summaries.findAll()) {
      if (summary.getApprovedAuthCount() == null || summary.getApprovedAuthCount() <= 0) {
        summaries.delete(summary);
      }
    }
    return RepeatStatus.FINISHED;
  }
}
