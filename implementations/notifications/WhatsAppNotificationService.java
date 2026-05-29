package implementations.notifications;

import interfaces.NotificationService;

public class WhatsAppNotificationService implements NotificationService {
    public void notify(String recipient, String message) {
        System.out.println("[WHATSAPP] to:" + recipient + " msg:" + message);
    }
}
