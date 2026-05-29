package implementations.notifications;

import interfaces.NotificationService;

public class EmailNotificationService implements NotificationService {
    public void notify(String recipient, String message) {
        System.out.println("[EMAIL] to:" + recipient + " msg:" + message);
    }
}
