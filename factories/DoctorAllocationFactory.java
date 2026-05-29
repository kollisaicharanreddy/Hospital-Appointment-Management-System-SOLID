package factories;

import implementations.allocation.EmergencyDoctorAllocationStrategy;
import implementations.allocation.LeastBusyDoctorAllocationStrategy;
import implementations.allocation.SpecialtyBasedAllocationStrategy;
import interfaces.DoctorAllocationStrategy;
import services.HospitalStore;

public class DoctorAllocationFactory {
    public static DoctorAllocationStrategy create(String type, HospitalStore store) {
        if (type == null) {
            return new LeastBusyDoctorAllocationStrategy(store);
        }

        if (type.equalsIgnoreCase("specialty")) {
            return new SpecialtyBasedAllocationStrategy(store);
        }

        if (type.equalsIgnoreCase("emergency")) {
            return new EmergencyDoctorAllocationStrategy(store);
        }

        return new LeastBusyDoctorAllocationStrategy(store);
    }
}
