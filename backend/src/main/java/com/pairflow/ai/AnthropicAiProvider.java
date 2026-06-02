package com.pairflow.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pairflow.common.error.ApiException;
import com.pairflow.common.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Claude-backed provider, enabled with {@code pairflow.ai.provider=anthropic} and an API key.
 * The guardrails from spec 7.14 are baked into the system prompt.
 */
@Component
@ConditionalOnProperty(name = "pairflow.ai.provider", havingValue = "anthropic")
public class AnthropicAiProvider implements AiProvider {

    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String SYSTEM = """
            You are a gentle assistant inside a private app for couples. Your job is to reduce friction
            and add warmth. Hard rules: assist only — never judge either person's character, never suggest
            controlling, monitoring, or testing a partner, never produce manipulative or guilt-tripping
            scripts, and never claim to be a therapist. Reply in the user's language (default Traditional
            Chinese). Be concise and kind.""";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    public AnthropicAiProvider(ObjectMapper mapper,
                               @Value("${pairflow.ai.anthropic.api-key}") String apiKey,
                               @Value("${pairflow.ai.anthropic.model}") String model) {
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public List<String> breakdownTodo(String input) {
        return lines(call("把這個情侶共同任務拆解成 4-7 個簡短、可執行的子任務，一行一個，不要編號：\n" + input));
    }

    @Override
    public List<String> dateSuggestions(String dateType, String budget, String area, String mood) {
        String prompt = "請給 3 個貼心的約會點子，一行一個，不要編號。"
                + "類型=" + n(dateType) + "，預算=" + n(budget) + "，地區=" + n(area) + "，目前心情=" + n(mood);
        return lines(call(prompt));
    }

    @Override
    public String anniversaryMessage(String occasion, String tone) {
        return call("幫我寫一段給伴侶的紀念日卡片文字，溫暖真誠、不浮誇。場合=" + n(occasion) + "，語氣=" + n(tone));
    }

    @Override
    public String softenText(String text) {
        return call("把下面這段想對伴侶說的話，改寫成『表達感受、不指責、溫和』的版本，保持同一種語言：\n" + text);
    }

    @Override
    public String memorySummary(String context) {
        return call("根據以下素材，用 2-3 句溫暖的話總結這段情侶回憶：\n" + context);
    }

    private String call(String userPrompt) {
        try {
            String body = mapper.writeValueAsString(Map.of(
                    "model", model,
                    "max_tokens", 1024,
                    "system", SYSTEM,
                    "messages", List.of(Map.of("role", "user", "content", userPrompt))));
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new ApiException(ErrorCode.INTERNAL_ERROR, "AI provider returned " + response.statusCode());
            }
            JsonNode content = mapper.readTree(response.body()).path("content");
            StringBuilder sb = new StringBuilder();
            for (JsonNode block : content) {
                if ("text".equals(block.path("type").asText())) {
                    sb.append(block.path("text").asText());
                }
            }
            return sb.toString().strip();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "AI request failed");
        }
    }

    private List<String> lines(String text) {
        return Arrays.stream(text.split("\\r?\\n"))
                .map(s -> s.replaceFirst("^\\s*([-*•]|\\d+[.)])\\s*", "").strip())
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String n(String s) {
        return s == null || s.isBlank() ? "(未指定)" : s;
    }
}
