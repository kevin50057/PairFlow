package com.pairflow.ai;

import java.util.List;

/**
 * Pluggable AI backend. Default is a deterministic {@link StubAiProvider} (no API key
 * needed); set {@code pairflow.ai.provider=anthropic} to use Claude. All implementations
 * must honor the product guardrails (spec 7.14): assist only — no persona judgement,
 * no control/monitoring advice, no manipulative scripting.
 */
public interface AiProvider {

    List<String> breakdownTodo(String input);

    List<String> dateSuggestions(String dateType, String budget, String area, String mood);

    String anniversaryMessage(String occasion, String tone);

    String softenText(String text);

    String memorySummary(String context);
}
