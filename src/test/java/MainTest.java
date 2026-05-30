import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void mainRunsTheFullMenuFlowAndStopsOnZero() {
        String input = String.join(System.lineSeparator(),
                "1", "Mina", "100",
                "2", "Dr. Ray", "cardiology",
                "3", "1", "cardiology", "09:00",
                "4", "1",
                "3", "1", "cardiology", "10:00",
                "5", "2", "11:00",
                "6", "2",
                "7", "1",
                "8",
                "9",
                "0") + System.lineSeparator();

        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(buffer));

        try {
            Main.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String output = buffer.toString().replace("\r\n", "\n");

        assertTrue(output.contains("added:Patient{1;Mina;100}"));
        assertTrue(output.contains("added:Doctor{1;Dr. Ray;cardiology}"));
        assertTrue(output.contains("booked:Appointment{1 p:1 d:1 t:09:00}"));
        assertTrue(output.contains("cancelled"));
        assertTrue(output.contains("booked:Appointment{2 p:1 d:1 t:10:00}"));
        assertTrue(output.contains("rescheduled:Appointment{2 p:1 d:1 t:11:00}"));
        assertTrue(output.contains("bill:Bill{1 a:2 amt:100.0}"));
        assertTrue(output.contains("Daily revenue report: appointments=1, revenue=100.0"));
        assertTrue(output.contains("Doctor appointment report: doctors=1, appointments=1"));
        assertTrue(output.contains("Patient history report: patientId=1, appointments=1"));
        assertTrue(output.contains("Patients:"));
        assertTrue(output.contains("Doctors:"));
        assertTrue(output.contains("Appointments:"));
        assertTrue(output.contains("Bills:"));
        assertTrue(output.contains("unknown"));
    }
}