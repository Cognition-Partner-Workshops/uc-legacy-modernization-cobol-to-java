package com.aws.carddemo.web.report;

import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  private final JobLauncher launcher;
  private final Job transactionReportJob;

  public ReportService(
      JobLauncher launcher, @Qualifier("transactionReportJob") Job transactionReportJob) {
    this.launcher = launcher;
    this.transactionReportJob = transactionReportJob;
  }

  public Response<ReportSubmission> submit(ReportRequest request) {
    String error = validate(request);
    if (!error.isEmpty())
      return Response.error(
          error, error.startsWith("Start") ? "startDate" : "endDate", request.context());
    if (!request.confirm())
      return Response.error("Confirm to submit the report job...", "confirm", request.context());
    try {
      JobParameters parameters =
          new JobParametersBuilder()
              .addString("startDate", request.startDate())
              .addString("endDate", request.endDate())
              .addLong("submittedAt", System.currentTimeMillis())
              .toJobParameters();
      CompletableFuture.runAsync(
          () -> {
            try {
              launcher.run(transactionReportJob, parameters);
            } catch (Exception ignored) {
              // Batch records the failed execution; the REST request has already been submitted.
            }
          });
      return Response.ok(new ReportSubmission(null, "SUBMITTED"), "CORPT00C", request.context());
    } catch (Exception exception) {
      return Response.error("Unable to submit report job...", "report", request.context());
    }
  }

  private static String validate(ReportRequest request) {
    if (request.reportType() == null || request.reportType().isBlank())
      return "Select a report type to print report...";
    if (!request.reportType().matches("(?i)monthly|yearly|custom"))
      return "Select a report type to print report...";
    if (request.startDate() == null || request.startDate().isBlank())
      return "Start Date - Month can NOT be empty...";
    if (request.endDate() == null || request.endDate().isBlank())
      return "End Date - Month can NOT be empty...";
    try {
      if (LocalDate.parse(request.startDate()).isAfter(LocalDate.parse(request.endDate())))
        return "Start Date - Not a valid date...";
    } catch (RuntimeException exception) {
      return "Start Date - Not a valid date...";
    }
    return "";
  }

  public record ReportRequest(
      String reportType, String startDate, String endDate, boolean confirm, Context context) {}

  public record ReportSubmission(Long executionId, String status) {}
}
