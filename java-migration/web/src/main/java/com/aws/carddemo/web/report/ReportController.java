package com.aws.carddemo.web.report;

import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
  private final ReportService service;

  public ReportController(ReportService service) {
    this.service = service;
  }

  @PostMapping
  public Response<ReportService.ReportSubmission> submit(
      @RequestBody ReportService.ReportRequest request) {
    return service.submit(request);
  }
}
