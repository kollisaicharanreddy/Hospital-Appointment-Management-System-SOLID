package implementations.billing;

import interfaces.BillingService;

public class FollowUpDiscountBillingService implements BillingService {
    public double generateCharge(int appointmentId) {
        return 70.0;
    }
}
