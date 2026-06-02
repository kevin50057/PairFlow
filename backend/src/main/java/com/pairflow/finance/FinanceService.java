package com.pairflow.finance;

import com.pairflow.common.error.ApiException;
import com.pairflow.couple.Couple;
import com.pairflow.couple.CoupleContext;
import com.pairflow.finance.dto.CreateExpenseRequest;
import com.pairflow.finance.dto.ExpenseResponse;
import com.pairflow.finance.dto.ExpenseSummary;
import com.pairflow.finance.dto.UpdateExpenseRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceService {

    private final ExpenseRepository repository;
    private final CoupleContext coupleContext;

    public FinanceService(ExpenseRepository repository, CoupleContext coupleContext) {
        this.repository = repository;
        this.coupleContext = coupleContext;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Instant from, Instant to) {
        String coupleId = coupleContext.requireCoupleId();
        List<Expense> expenses = (from != null && to != null)
                ? repository.findByCoupleIdAndSpentAtBetweenOrderBySpentAtDesc(coupleId, from, to)
                : repository.findByCoupleIdOrderBySpentAtDesc(coupleId);
        return expenses.stream().map(ExpenseResponse::from).toList();
    }

    @Transactional
    public ExpenseResponse create(CreateExpenseRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Expense e = new Expense();
        e.setCoupleId(couple.getId());
        e.setCreatedBy(me);
        e.setAmount(req.amount());
        e.setCategory(req.category() != null && !req.category().isBlank() ? req.category() : "OTHER");
        e.setPaidByUserId(resolvePayer(req.paidBy(), couple, me));
        e.setSplitType(req.splitType() != null ? req.splitType() : SplitType.NONE);
        e.setCustomPayerRatio(req.customPayerRatio());
        e.setNote(req.note());
        e.setRelatedEventId(req.relatedEventId());
        e.setSpentAt(req.spentAt() != null ? req.spentAt() : Instant.now());
        return ExpenseResponse.from(repository.save(e));
    }

    @Transactional
    public ExpenseResponse update(String id, UpdateExpenseRequest req) {
        Couple couple = coupleContext.requireCouple();
        String me = coupleContext.currentUserId();
        Expense e = load(id, couple);
        if (req.amount() != null) e.setAmount(req.amount());
        if (req.category() != null) e.setCategory(req.category());
        if (req.paidBy() != null) e.setPaidByUserId(resolvePayer(req.paidBy(), couple, me));
        if (req.splitType() != null) e.setSplitType(req.splitType());
        if (req.customPayerRatio() != null) e.setCustomPayerRatio(req.customPayerRatio());
        if (req.note() != null) e.setNote(req.note());
        if (req.relatedEventId() != null) e.setRelatedEventId(req.relatedEventId());
        if (req.spentAt() != null) e.setSpentAt(req.spentAt());
        return ExpenseResponse.from(e);
    }

    @Transactional
    public void delete(String id) {
        repository.delete(load(id, coupleContext.requireCouple()));
    }

    @Transactional(readOnly = true)
    public ExpenseSummary summary(Instant from, Instant to) {
        List<ExpenseResponse> expenses = list(from, to);
        double total = expenses.stream().mapToDouble(ExpenseResponse::amount).sum();
        Map<String, Double> byCategory = new LinkedHashMap<>();
        Map<String, Double> byPayer = new LinkedHashMap<>();
        for (ExpenseResponse e : expenses) {
            byCategory.merge(e.category(), e.amount(), Double::sum);
            byPayer.merge(e.paidByUserId(), e.amount(), Double::sum);
        }
        return new ExpenseSummary(total, expenses.size(), byCategory, byPayer);
    }

    private String resolvePayer(PayerOption option, Couple couple, String me) {
        PayerOption opt = option != null ? option : PayerOption.ME;
        return opt == PayerOption.PARTNER ? couple.partnerOf(me) : me;
    }

    private Expense load(String id, Couple couple) {
        Expense e = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Expense not found"));
        if (!e.getCoupleId().equals(couple.getId())) {
            throw ApiException.notFound("Expense not found");
        }
        return e;
    }
}
