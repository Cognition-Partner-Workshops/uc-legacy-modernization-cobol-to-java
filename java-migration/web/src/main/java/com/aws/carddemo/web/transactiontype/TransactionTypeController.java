package com.aws.carddemo.web.transactiontype;

import com.aws.carddemo.domain.TransactionType;
import com.aws.carddemo.domain.TransactionTypeRepository;
import com.aws.carddemo.web.common.WebTypes.Response;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/transaction-types")
public class TransactionTypeController {
  private static final int PAGE_SIZE = 7;
  private final TransactionTypeRepository repository;

  public TransactionTypeController(TransactionTypeRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public Response<List<TransactionType>> list(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "direction", defaultValue = "FORWARD") String direction) {
    List<TransactionType> rows = repository.findAllByOrderByTypeCdAsc();
    int target = "BACK".equalsIgnoreCase(direction) ? Math.max(0, page - 1) : Math.max(0, page);
    int from = Math.min(target * PAGE_SIZE, rows.size());
    if (from >= rows.size() && !rows.isEmpty()) {
      return Response.error("No more pages to display", "page", null);
    }
    return Response.ok(
        rows.subList(from, Math.min(from + PAGE_SIZE, rows.size())), "COTRTLIC", null);
  }

  @PostMapping
  public Response<TransactionType> save(
      @RequestBody TransactionTypeRequest request,
      @RequestParam(name = "delete", defaultValue = "false") boolean delete) {
    String type = request.type() == null ? "" : request.type().trim();
    if (!type.matches("[A-Za-z0-9]{2}")) {
      return Response.error("Transaction Type must be 2 characters", "type", null);
    }
    if (delete) {
      if (!repository.existsById(type)) {
        return Response.error("Record not found. Deleted by others ?", "type", null);
      }
      repository.deleteById(type);
      return Response.ok(null, "COTRTLIC", null);
    }
    if (request.description() == null || request.description().isBlank()) {
      return Response.error("Transaction Description can NOT be empty", "description", null);
    }
    TransactionType value = repository.findById(type).orElseGet(TransactionType::new);
    value.setTypeCd(type);
    value.setTypeDesc(request.description());
    return Response.ok(repository.save(value), "COTRTUPC", null);
  }

  public record TransactionTypeRequest(String type, String description) {}
}
