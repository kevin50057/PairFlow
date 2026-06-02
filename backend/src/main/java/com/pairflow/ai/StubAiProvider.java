package com.pairflow.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Deterministic, offline AI stand-in so every AI feature works with zero setup.
 * Swapped out by setting {@code pairflow.ai.provider=anthropic}.
 */
@Component
@ConditionalOnProperty(name = "pairflow.ai.provider", havingValue = "stub", matchIfMissing = true)
public class StubAiProvider implements AiProvider {

    @Override
    public List<String> breakdownTodo(String input) {
        String t = input == null ? "" : input;
        if (containsAny(t, "旅", "旅行", "兩天", "一夜", "過夜", "出遊", "trip")) {
            return List.of("訂住宿", "查交通", "找餐廳", "安排行程", "看天氣", "準備行李");
        }
        if (containsAny(t, "餐廳", "約會", "晚餐", "吃飯", "date")) {
            return List.of("查 3 間餐廳", "確認對方喜歡的料理", "訂位", "查交通", "加到日曆");
        }
        if (containsAny(t, "生日", "禮物", "驚喜", "紀念日")) {
            return List.of("想 3 個驚喜點子", "準備禮物", "訂蛋糕", "寫卡片", "安排當天行程");
        }
        return List.of("列出要做的事", "分配負責人", "設定截止時間");
    }

    @Override
    public List<String> dateSuggestions(String dateType, String budget, String area, String mood) {
        String type = dateType == null ? "" : dateType.toUpperCase();
        return switch (type) {
            case "FOOD", "CAFE" -> List.of("找一間沒去過的餐廳一起嘗鮮", "預約有氣氛的位子", "飯後散步聊聊最近的事");
            case "MOVIE" -> List.of("挑一部你們都想看的電影", "提早買好對位子", "看完去喝杯飲料聊心得");
            case "TRIP" -> List.of("規劃一個兩天一夜的小旅行", "選一個沒去過的城市", "排一個不趕行程的慢活路線");
            case "WALK", "RELAX" -> List.of("找一條河濱或公園散步", "帶杯咖啡邊走邊聊", "傍晚去看夕陽");
            default -> List.of("一起做一件平常沒做過的小事", "重訪你們的第一次約會地點", "在家煮一頓飯配電影");
        };
    }

    @Override
    public String anniversaryMessage(String occasion, String tone) {
        String day = (occasion == null || occasion.isBlank()) ? "這個特別的日子" : occasion;
        return "親愛的，" + day + "快樂。謝謝你一直在我身邊，讓平凡的每一天都變得溫暖。"
                + "未來的日子，也想繼續和你一起走下去。❤️";
    }

    @Override
    public String softenText(String text) {
        String trimmed = text == null ? "" : text.strip();
        return "我想好好跟你說我的感受：\n「" + trimmed + "」\n\n"
                + "我說這些不是要怪你，而是因為我很在乎我們。等你方便的時候，我們可以一起聊聊嗎？";
    }

    @Override
    public String memorySummary(String context) {
        String c = (context == null || context.isBlank()) ? "你們一起度過的時光" : context.strip();
        return "這是你們的一段回憶：" + c + "。這段美好的時光被好好地記錄下來了，值得偶爾回味。";
    }

    private boolean containsAny(String text, String... terms) {
        String lower = text.toLowerCase();
        for (String term : terms) {
            if (lower.contains(term.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
