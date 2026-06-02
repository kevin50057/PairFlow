package com.pairflow.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Default push channel: logs only. Swap for an FCM implementation in prod. */
@Slf4j
@Component
public class LoggingPushSender implements PushSender {

    @Override
    public void send(String recipientId, String title, String body) {
        log.info("[push] -> {} | {} — {}", recipientId, title, body);
    }
}
