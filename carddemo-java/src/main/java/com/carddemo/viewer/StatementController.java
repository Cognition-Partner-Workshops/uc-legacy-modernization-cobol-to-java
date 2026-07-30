package com.carddemo.viewer;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatementController {
    private final StatementRepository repository;

    public StatementController(StatementRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("statements", repository.findAll());
        return "index";
    }

    @GetMapping("/statements/{accountId}")
    public String detail(@PathVariable String accountId, Model model, HttpServletResponse response) throws IOException {
        return repository.findByAccountId(accountId)
                .map(statement -> {
                    model.addAttribute("statement", statement);
                    return "detail";
                })
                .orElseGet(() -> {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return "error/404";
                });
    }

    @GetMapping("/api/statements")
    @ResponseBody
    public List<Statement> statements() {
        return repository.findAll();
    }

    @GetMapping("/api/statements/{accountId}")
    @ResponseBody
    public ResponseEntity<Statement> statement(@PathVariable String accountId) {
        return repository.findByAccountId(accountId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
