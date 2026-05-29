package implementations.allocation;

import interfaces.DoctorAllocationStrategy;
import models.Doctor;
import services.HospitalStore;

public class SpecialtyBasedAllocationStrategy implements DoctorAllocationStrategy {
    private final HospitalStore store;

    public SpecialtyBasedAllocationStrategy(HospitalStore store) {
        this.store = store;
    }

    public int allocate(int patientId, String specialty) {
        for (Doctor doctor : store.listDoctors()) {
            if (specialty != null && specialty.equalsIgnoreCase(doctor.specialty)) {
                return doctor.id;
            }
        }

        return store.listDoctors().isEmpty() ? -1 : store.listDoctors().get(0).id;
    }
}
