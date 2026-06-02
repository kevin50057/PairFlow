package com.pairflow.ai;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Minimal safety net for the repair / softening flows. When a message signals violence
 * or self-harm, the app must stop "helping reword" and point to real human help
 * (spec 7.10 limits).
 */
@Component
public class SafetyGuard {

    private static final List<String> RISK_TERMS = List.of(
            "自殺", "自殘", "傷害自己", "想死", "不想活", "結束生命",
            "殺了你", "殺了我", "打死", "家暴", "暴力相向", "威脅你", "威脅我");

    public boolean isHighRisk(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase();
        return RISK_TERMS.stream().anyMatch(lower::contains);
    }

    public String helpNotice() {
        return "如果你或對方正面臨立即的危險或傷害，請尋求真人協助：可聯絡信任的親友，或撥打台灣 1925 安心專線（24 小時）或 110。"
                + "PairFlow 是溝通輔助工具，無法取代專業協助。";
    }
}
