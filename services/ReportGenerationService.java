package services;

import factories.ReportFactory;
import interfaces.ReportService;

public class ReportGenerationService {
    private final HospitalStore store;

    public ReportGenerationService(HospitalStore store) {
        this.store = store;
    }

    public String generateDailyRevenueReport() {
        ReportService reportService = ReportFactory.create("daily", store, 0);
        return reportService.generate();
    }

    public String generateDoctorReport() {
        ReportService reportService = ReportFactory.create("doctor", store, 0);
        return reportService.generate();
    }

    public String generatePatientHistoryReport(int patientId) {
        ReportService reportService = ReportFactory.create("patient", store, patientId);
        return reportService.generate();
    }
}
