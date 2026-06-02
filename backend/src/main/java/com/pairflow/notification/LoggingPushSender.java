package com.pairflow.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** Default push channel: logs only. Replaced by {@link FcmPushSender} when FCM is configured. */
@Slf4j
@Component
@ConditionalOnMissingBean(FcmPushSender.class)
public class LoggingPushSender implements PushSender {

    @Override
    public void send(String recipientId, String title, String body) {
        log.info("[push] -> {} | {} — {}", recipientId, title, body);
    }
}
