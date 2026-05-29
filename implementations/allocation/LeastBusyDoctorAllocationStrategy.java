package implementations.allocation;

import interfaces.DoctorAllocationStrategy;
import services.HospitalStore;

public class LeastBusyDoctorAllocationStrategy implements DoctorAllocationStrategy {
    private final HospitalStore store;

    public LeastBusyDoctorAllocationStrategy(HospitalStore store) {
        this.store = store;
    }

    public int allocate(int patientId, String specialty) {
        return store.listDoctors().isEmpty() ? -1 : store.listDoctors().get(0).id;
    }
}
