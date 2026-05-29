package implementations.notifications;

import interfaces.NotificationService;

public class ConsoleNotificationService implements NotificationService {
    public void notify(String recipient, String message) {
        System.out.println("[NOTIFY] to:" + recipient + " msg:" + message);
    }
}
