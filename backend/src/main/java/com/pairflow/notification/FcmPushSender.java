package com.pairflow.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Production FCM push sender. Active only when
 * {@code pairflow.push.fcm.service-account-file} is set to a valid
 * Firebase service-account JSON path.
 *
 * Integration steps:
 *   1. Add {@code com.google.firebase:firebase-admin} to pom.xml
 *   2. Download service account JSON from Firebase Console → Project Settings → Service Accounts
 *   3. Set {@code PAIRFLOW_FCM_SA_FILE=/path/to/service-account.json} in env
 *
 * The {@link LoggingPushSender} is used automatically when this bean is absent.
 */
@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "pairflow.push.fcm.service-account-file")
public class FcmPushSender implements PushSender {

    private final UserDeviceRepository deviceRepository;

    public FcmPushSender(UserDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
        // When enabled, initialize FirebaseApp here:
        // FileInputStream sa = new FileInputStream(saFilePath);
        // FirebaseOptions options = FirebaseOptions.builder()
        //         .setCredentials(GoogleCredentials.fromStream(sa))
        //         .build();
        // FirebaseApp.initializeApp(options);
        log.info("[FCM] FcmPushSender active — add firebase-admin dependency and configure service account to enable");
    }

    @Override
    public void send(String recipientId, String title, String body) {
        deviceRepository.findByUserId(recipientId).forEach(device -> {
            try {
                sendToToken(device.getFcmToken(), title, body);
            } catch (Exception e) {
                log.warn("[FCM] Failed to push to device {} for user {}: {}", device.getId(), recipientId, e.getMessage());
                // If token is stale, remove it:
                // if (isInvalidTokenError(e)) deviceRepository.deleteByFcmToken(device.getFcmToken());
            }
        });
    }

    private void sendToToken(String fcmToken, String title, String body) {
        // Uncomment when firebase-admin is on the classpath:
        // Message message = Message.builder()
        //         .setToken(fcmToken)
        //         .setNotification(Notification.builder().setTitle(title).setBody(body).build())
        //         .build();
        // FirebaseMessaging.getInstance().send(message);
        log.info("[FCM] -> token={} | {} — {}", fcmToken.substring(0, Math.min(8, fcmToken.length())), title, body);
    }
}
