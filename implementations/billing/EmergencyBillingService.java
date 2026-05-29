package implementations.billing;

import interfaces.BillingService;

public class EmergencyBillingService implements BillingService {
    public double generateCharge(int appointmentId) {
        return 200.0;
    }
}
