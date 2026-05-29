package factories;

import implementations.reports.DailyRevenueReportService;
import implementations.reports.DoctorAppointmentReportService;
import implementations.reports.PatientHistoryReportService;
import interfaces.ReportService;
import services.HospitalStore;

public class ReportFactory {
    public static ReportService create(String type, HospitalStore store, int patientId) {
        if (type == null) {
            return new DailyRevenueReportService(store);
        }

        if (type.equalsIgnoreCase("doctor")) {
            return new DoctorAppointmentReportService(store);
        }

        if (type.equalsIgnoreCase("patient")) {
            return new PatientHistoryReportService(store, patientId);
        }

        return new DailyRevenueReportService(store);
    }
}
