package factories;

import implementations.billing.EmergencyBillingService;
import implementations.billing.FollowUpDiscountBillingService;
import implementations.billing.StandardBillingService;
import interfaces.BillingService;

public class BillingFactory {
    public static BillingService create(String type) {
        if (type == null) {
            return new StandardBillingService();
        }

        if (type.equalsIgnoreCase("emergency")) {
            return new EmergencyBillingService();
        }

        if (type.equalsIgnoreCase("followup")) {
            return new FollowUpDiscountBillingService();
        }

        return new StandardBillingService();
    }
}
