package interfaces;

public interface DoctorAllocationStrategy {
	int allocate(int patientId, String specialty);
}
