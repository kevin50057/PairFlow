package com.pairflow.question;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seeds the question catalog once, on first startup. */
@Component
public class QuestionSeeder implements ApplicationRunner {

    private final QuestionCardRepository repository;

    public QuestionSeeder(QuestionCardRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        List<QuestionCard> seed = List.of(
                card("你最近最希望我多注意你的哪件事？", "DAILY", Sensitivity.MEDIUM),
                card("如果下週只能安排一次約會，你想去哪裡？", "DATE", Sensitivity.LOW),
                card("你覺得我們最近最棒的一件事是什麼？", "MEMORY", Sensitivity.LOW),
                card("有沒有什麼話你不好意思直接說？", "INTIMACY", Sensitivity.HIGH),
                card("我做什麼事情會讓你覺得被愛？", "INTIMACY", Sensitivity.MEDIUM),
                card("最近有什麼讓你壓力很大的事嗎？", "DAILY", Sensitivity.MEDIUM),
                card("你理想中的週末是什麼樣子？", "VALUES", Sensitivity.LOW),
                card("我們五年後會在哪裡、過著什麼樣的生活？", "FUTURE", Sensitivity.MEDIUM),
                card("最近哪一刻讓你覺得很幸福？", "MEMORY", Sensitivity.LOW),
                card("你希望我們吵架後怎麼和好？", "CONFLICT", Sensitivity.HIGH),
                card("如果可以一起學一樣新東西，你想學什麼？", "FUN", Sensitivity.LOW),
                card("你最近覺得我哪裡很可愛？", "FUN", Sensitivity.LOW),
                card("有什麼是你一直想和我一起做、但還沒做的？", "FUTURE", Sensitivity.LOW),
                card("我可以怎麼做，讓你覺得更被支持？", "VALUES", Sensitivity.MEDIUM));
        repository.saveAll(seed);
    }

    private QuestionCard card(String text, String category, Sensitivity sensitivity) {
        QuestionCard c = new QuestionCard();
        c.setText(text);
        c.setCategory(category);
        c.setSensitivity(sensitivity);
        return c;
    }
}
