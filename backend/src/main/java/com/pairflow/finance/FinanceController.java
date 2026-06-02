package com.pairflow.finance;

import com.pairflow.common.error.ApiException;
import com.pairflow.finance.dto.CreateExpenseRequest;
import com.pairflow.finance.dto.ExpenseResponse;
import com.pairflow.finance.dto.ExpenseSummary;
import com.pairflow.finance.dto.UpdateExpenseRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class FinanceController {

    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpenseResponse> list(@RequestParam(required = false) String from,
                                      @RequestParam(required = false) String to) {
        return service.list(parseInstant(from), parseInstant(to));
    }

    @GetMapping("/summary")
    public ExpenseSummary summary(@RequestParam(required = false) String from,
                                  @RequestParam(required = false) String to) {
        return service.summary(parseInstant(from), parseInstant(to));
    }

    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest req) {
        return service.create(req);
    }

    @PatchMapping("/{id}")
    public ExpenseResponse update(@PathVariable String id, @Valid @RequestBody UpdateExpenseRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    private static Instant parseInstant(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (Exception ignore) {
            // fall through
        }
        try {
            return Instant.parse(s);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid datetime: " + s);
        }
    }
}
