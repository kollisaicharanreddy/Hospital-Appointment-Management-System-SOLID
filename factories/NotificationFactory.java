package factories;

import implementations.notifications.ConsoleNotificationService;
import implementations.notifications.EmailNotificationService;
import implementations.notifications.SMSNotificationService;
import implementations.notifications.WhatsAppNotificationService;
import interfaces.NotificationService;

public class NotificationFactory {
    public static NotificationService create(String type) {
        if (type == null) {
            return new ConsoleNotificationService();
        }

        if (type.equalsIgnoreCase("email")) {
            return new EmailNotificationService();
        }

        if (type.equalsIgnoreCase("sms")) {
            return new SMSNotificationService();
        }

        if (type.equalsIgnoreCase("whatsapp")) {
            return new WhatsAppNotificationService();
        }

        return new ConsoleNotificationService();
    }
}
