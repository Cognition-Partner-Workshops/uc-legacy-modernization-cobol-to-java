package com.carddemo.statement.viewer;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class StatementViewController {
    private final StatementRepository repository;

    public StatementViewController(StatementRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("statements", repository.findAll());
        return "index";
    }

    @GetMapping("/statements/{accountId}")
    public String detail(@PathVariable String accountId, Model model) {
        Statement statement = repository.findByAccountId(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("statement", statement);
        return "detail";
    }
}
