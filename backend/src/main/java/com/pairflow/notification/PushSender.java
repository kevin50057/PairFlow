package com.pairflow.notification;

/** Abstraction over the push channel (FCM-ready). */
public interface PushSender {

    void send(String recipientId, String title, String body);
}
