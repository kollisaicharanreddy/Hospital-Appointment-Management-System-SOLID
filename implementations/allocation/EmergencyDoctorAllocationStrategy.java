package implementations.allocation;

import interfaces.DoctorAllocationStrategy;
import models.Doctor;
import services.HospitalStore;

public class EmergencyDoctorAllocationStrategy implements DoctorAllocationStrategy {
    private final HospitalStore store;

    public EmergencyDoctorAllocationStrategy(HospitalStore store) {
        this.store = store;
    }

    public int allocate(int patientId, String specialty) {
        for (Doctor doctor : store.listDoctors()) {
            if (doctor.specialty != null && doctor.specialty.equalsIgnoreCase("emergency")) {
                return doctor.id;
            }
        }

        return store.listDoctors().isEmpty() ? -1 : store.listDoctors().get(0).id;
    }
}
