package implementations.notifications;

import interfaces.NotificationService;

public class SMSNotificationService implements NotificationService {
    public void notify(String recipient, String message) {
        System.out.println("[SMS] to:" + recipient + " msg:" + message);
    }
}
