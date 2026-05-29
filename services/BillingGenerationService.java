package services;

import interfaces.BillingService;
import models.Bill;
import models.Appointment;

public class BillingGenerationService {
    private final BillingService billingService;
    private final HospitalStore store;

    public BillingGenerationService(HospitalStore store, BillingService billingService) {
        this.store = store;
        this.billingService = billingService;
    }

    public Bill generate(int appointmentId) {
        Appointment appointment = store.findAppointment(appointmentId);
        if (appointment == null) {
            return null;
        }

        double amount = billingService.generateCharge(appointmentId);
        return store.addBill(appointmentId, amount);
    }
}
