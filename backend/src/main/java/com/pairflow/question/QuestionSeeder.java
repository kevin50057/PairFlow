package com.pairflow.question;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Seeds and tops up the question catalog. */
@Component
public class QuestionSeeder implements ApplicationRunner {

    static final int TARGET_COUNT = 1000;

    private final QuestionCardRepository repository;

    public QuestionSeeder(QuestionCardRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() >= TARGET_COUNT) {
            return;
        }

        Set<String> existing = new LinkedHashSet<>();
        repository.findAll().forEach(card -> existing.add(card.getText()));

        List<QuestionCard> additions = new ArrayList<>();
        for (SeedQuestion question : buildCatalog()) {
            if (existing.size() >= TARGET_COUNT) {
                break;
            }
            if (existing.contains(question.text())) {
                continue;
            }
            additions.add(card(question.text(), question.category(), question.sensitivity()));
            existing.add(question.text());
        }

        if (!additions.isEmpty()) {
            repository.saveAll(additions);
        }
    }

    private List<SeedQuestion> buildCatalog() {
        List<SeedQuestion> questions = new ArrayList<>(TARGET_COUNT + 100);
        seedOriginalQuestions(questions);
        seedByTopic(questions);
        return questions;
    }

    private void seedOriginalQuestions(List<SeedQuestion> questions) {
        questions.add(q("你最近最希望我多注意你的哪件事？", "DAILY", Sensitivity.MEDIUM));
        questions.add(q("如果下週只能安排一次約會，你想去哪裡？", "DATE", Sensitivity.LOW));
        questions.add(q("你覺得我們最近最棒的一件事是什麼？", "MEMORY", Sensitivity.LOW));
        questions.add(q("有沒有什麼話你不好意思直接說？", "INTIMACY", Sensitivity.HIGH));
        questions.add(q("我做什麼事情會讓你覺得被愛？", "INTIMACY", Sensitivity.MEDIUM));
        questions.add(q("最近有什麼讓你壓力很大的事嗎？", "DAILY", Sensitivity.MEDIUM));
        questions.add(q("你理想中的週末是什麼樣子？", "VALUES", Sensitivity.LOW));
        questions.add(q("我們五年後會在哪裡、過著什麼樣的生活？", "FUTURE", Sensitivity.MEDIUM));
        questions.add(q("最近哪一刻讓你覺得很幸福？", "MEMORY", Sensitivity.LOW));
        questions.add(q("你希望我們吵架後怎麼和好？", "CONFLICT", Sensitivity.HIGH));
        questions.add(q("如果可以一起學一樣新東西，你想學什麼？", "FUN", Sensitivity.LOW));
        questions.add(q("你最近覺得我哪裡很可愛？", "FUN", Sensitivity.LOW));
        questions.add(q("有什麼是你一直想和我一起做、但還沒做的？", "FUTURE", Sensitivity.LOW));
        questions.add(q("我可以怎麼做，讓你覺得更被支持？", "VALUES", Sensitivity.MEDIUM));
    }

    private void seedByTopic(List<SeedQuestion> questions) {
        for (int prompt = 0; prompt < 20; prompt++) {
            for (Topic topic : TOPICS) {
                questions.add(topicQuestion(prompt, topic));
            }
        }
    }

    private SeedQuestion topicQuestion(int prompt, Topic topic) {
        String name = topic.name();
        return switch (prompt) {
            case 0 -> q("關於" + name + "，你最近最想讓我理解的一件事是什麼？", topic.category(), topic.sensitivity());
            case 1 -> q("在" + name + "這件事上，我做什麼會讓你感覺更被愛？", topic.category(), topic.sensitivity());
            case 2 -> q("如果我們想把" + name + "變得更好，第一個小改變可以是什麼？", topic.category(), topic.sensitivity());
            case 3 -> q("你希望我們在" + name + "上保留哪個習慣？為什麼？", topic.category(), topic.sensitivity());
            case 4 -> q("你覺得我們在" + name + "上最有默契的是哪一刻？", topic.category(), Sensitivity.LOW);
            case 5 -> q("如果用 1 到 10 分形容最近的" + name + "，你會給幾分？差的那幾分是什麼？", topic.category(), topic.sensitivity());
            case 6 -> q("關於" + name + "，你有沒有一個小小的期待還沒說出口？", topic.category(), topic.sensitivity());
            case 7 -> q("當" + name + "不如預期時，你希望我先做什麼，而不是急著解決？", topic.category(), Sensitivity.MEDIUM);
            case 8 -> q("我們以前哪一次" + name + "讓你到現在還記得？", topic.category(), Sensitivity.LOW);
            case 9 -> q("如果下週只能為" + name + "安排 30 分鐘，你想怎麼用？", topic.category(), Sensitivity.LOW);
            case 10 -> q("你覺得我在" + name + "裡最值得被肯定的是什麼？", topic.category(), Sensitivity.LOW);
            case 11 -> q("關於" + name + "，你最怕我們忽略哪個細節？", topic.category(), topic.sensitivity());
            case 12 -> q("如果我們把" + name + "做成一個共同約定，你希望約定內容是什麼？", topic.category(), topic.sensitivity());
            case 13 -> q("在" + name + "這件事上，你需要更多陪伴、更多空間，還是更多具體行動？", topic.category(), Sensitivity.MEDIUM);
            case 14 -> q("關於" + name + "，你最近最想謝謝我的是哪件小事？", topic.category(), Sensitivity.LOW);
            case 15 -> q("如果重來一次，我們可以怎麼處理" + name + "會更舒服？", topic.category(), topic.sensitivity());
            case 16 -> q("你希望我怎麼問你，才比較容易聊到" + name + "的真心話？", topic.category(), Sensitivity.MEDIUM);
            case 17 -> q("關於" + name + "，有什麼界線或底線你希望我更清楚？", topic.category(), Sensitivity.HIGH);
            case 18 -> q("如果" + name + "是一段旅行，你覺得我們現在走到哪裡了？", topic.category(), Sensitivity.LOW);
            default -> q("你希望一年後回頭看" + name + "時，我們會為哪件事感到驕傲？", topic.category(), topic.sensitivity());
        };
    }

    private QuestionCard card(String text, String category, Sensitivity sensitivity) {
        QuestionCard c = new QuestionCard();
        c.setText(text);
        c.setCategory(category);
        c.setSensitivity(sensitivity);
        return c;
    }

    private SeedQuestion q(String text, String category, Sensitivity sensitivity) {
        return new SeedQuestion(text, category, sensitivity);
    }

    private record SeedQuestion(String text, String category, Sensitivity sensitivity) {
    }

    private record Topic(String name, String category, Sensitivity sensitivity) {
    }

    private static final Topic[] TOPICS = {
            new Topic("今天的心情", "DAILY", Sensitivity.MEDIUM),
            new Topic("最近的壓力", "DAILY", Sensitivity.MEDIUM),
            new Topic("睡前相處", "DAILY", Sensitivity.LOW),
            new Topic("早晨的互動", "DAILY", Sensitivity.LOW),
            new Topic("通勤或下班後的陪伴", "DAILY", Sensitivity.LOW),
            new Topic("日常訊息的頻率", "DAILY", Sensitivity.MEDIUM),
            new Topic("需要被安慰的時候", "SUPPORT", Sensitivity.MEDIUM),
            new Topic("需要獨處的時候", "SUPPORT", Sensitivity.MEDIUM),
            new Topic("生病或疲憊時的照顧", "SUPPORT", Sensitivity.MEDIUM),
            new Topic("工作與生活的平衡", "SUPPORT", Sensitivity.MEDIUM),
            new Topic("彼此的安全感", "TRUST", Sensitivity.HIGH),
            new Topic("承諾與信任", "TRUST", Sensitivity.HIGH),
            new Topic("吃醋或不安", "TRUST", Sensitivity.HIGH),
            new Topic("和朋友相處的界線", "TRUST", Sensitivity.HIGH),
            new Topic("社群媒體上的互動", "TRUST", Sensitivity.MEDIUM),
            new Topic("表達愛的方式", "INTIMACY", Sensitivity.MEDIUM),
            new Topic("身體親密的節奏", "INTIMACY", Sensitivity.HIGH),
            new Topic("稱讚與肯定", "INTIMACY", Sensitivity.LOW),
            new Topic("想念彼此的方式", "INTIMACY", Sensitivity.LOW),
            new Topic("讓你覺得被珍惜的瞬間", "INTIMACY", Sensitivity.MEDIUM),
            new Topic("第一次心動的回憶", "MEMORY", Sensitivity.LOW),
            new Topic("最近一起大笑的時候", "MEMORY", Sensitivity.LOW),
            new Topic("難忘的約會", "MEMORY", Sensitivity.LOW),
            new Topic("一起克服過的困難", "MEMORY", Sensitivity.MEDIUM),
            new Topic("我們的小暗號或默契", "MEMORY", Sensitivity.LOW),
            new Topic("週末約會", "DATE", Sensitivity.LOW),
            new Topic("平日短約會", "DATE", Sensitivity.LOW),
            new Topic("一起吃飯的選擇", "DATE", Sensitivity.LOW),
            new Topic("旅行計畫", "DATE", Sensitivity.LOW),
            new Topic("紀念日安排", "DATE", Sensitivity.MEDIUM),
            new Topic("金錢觀", "MONEY", Sensitivity.HIGH),
            new Topic("共同開銷", "MONEY", Sensitivity.HIGH),
            new Topic("儲蓄與消費", "MONEY", Sensitivity.HIGH),
            new Topic("送禮物的期待", "MONEY", Sensitivity.MEDIUM),
            new Topic("未來的生活成本", "MONEY", Sensitivity.HIGH),
            new Topic("家務分工", "HOME", Sensitivity.MEDIUM),
            new Topic("居家整潔標準", "HOME", Sensitivity.MEDIUM),
            new Topic("一起做飯", "HOME", Sensitivity.LOW),
            new Topic("生活儀式感", "HOME", Sensitivity.LOW),
            new Topic("同居或未來住處", "HOME", Sensitivity.HIGH),
            new Topic("和原生家庭的相處", "FAMILY", Sensitivity.HIGH),
            new Topic("節日要怎麼過", "FAMILY", Sensitivity.MEDIUM),
            new Topic("家人對關係的影響", "FAMILY", Sensitivity.HIGH),
            new Topic("未來要不要孩子", "FAMILY", Sensitivity.HIGH),
            new Topic("照顧長輩的想像", "FAMILY", Sensitivity.HIGH),
            new Topic("人生目標", "FUTURE", Sensitivity.MEDIUM),
            new Topic("五年後的我們", "FUTURE", Sensitivity.MEDIUM),
            new Topic("想一起完成的願望", "FUTURE", Sensitivity.LOW),
            new Topic("職涯變動", "FUTURE", Sensitivity.HIGH),
            new Topic("搬家或遠距的可能", "FUTURE", Sensitivity.HIGH),
            new Topic("吵架後的修復", "CONFLICT", Sensitivity.HIGH),
            new Topic("道歉的方式", "CONFLICT", Sensitivity.MEDIUM),
            new Topic("冷戰時的處理", "CONFLICT", Sensitivity.HIGH),
            new Topic("情緒上來的時候", "CONFLICT", Sensitivity.HIGH),
            new Topic("不同意見的協調", "CONFLICT", Sensitivity.MEDIUM),
            new Topic("彼此的價值觀", "VALUES", Sensitivity.MEDIUM),
            new Topic("自由與陪伴的比例", "VALUES", Sensitivity.MEDIUM),
            new Topic("公平感", "VALUES", Sensitivity.HIGH),
            new Topic("生活優先順序", "VALUES", Sensitivity.MEDIUM),
            new Topic("對幸福的定義", "VALUES", Sensitivity.LOW),
            new Topic("一起學新東西", "GROWTH", Sensitivity.LOW),
            new Topic("想改掉的小習慣", "GROWTH", Sensitivity.MEDIUM),
            new Topic("最近的自我成長", "GROWTH", Sensitivity.LOW),
            new Topic("想被提醒的事", "GROWTH", Sensitivity.MEDIUM),
            new Topic("彼此給的勇氣", "GROWTH", Sensitivity.MEDIUM)
    };
}
