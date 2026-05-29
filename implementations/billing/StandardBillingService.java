package implementations.billing;

import interfaces.BillingService;

public class StandardBillingService implements BillingService {
	public double generateCharge(int appointmentId) {
		return 100.0;
	}
}
