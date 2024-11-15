package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Import for ResultSet
import java.sql.SQLException;
import java.util.Scanner;

public class HospitalManagementSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "#Supriyo2027";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to the database successfully.");
            Scanner scanner = new Scanner(System.in);
            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            while (true) {
                System.out.println("\nHospital Management System");
                System.out.println("1. Add Patient");
                System.out.println("2. View Patients");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        patient.addPatient();
                        System.out.println();
                        break;
                    case 2:
                        patient.viewPatients();
                        System.out.println();
                        break;
                    case 3:
                        doctor.viewDoctors();
                        System.out.println();
                        break;
                    case 4:
                        bookAppointment(patient, doctor, connection, scanner);
                        System.out.println();
                        break;

                    case 5:
                        System.out.println("Thank you for using the Hospital Management System!");
                        System.out.println();
                        connection.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        System.out.println();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database:");
            e.printStackTrace();
        }
    }

    private static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.print("Enter Patient ID: ");
        int patientId = scanner.nextInt();
        System.out.print("Enter Doctor ID: ");
        int doctorId = scanner.nextInt();
        System.out.print("Enter Appointment date (YYYY-MM-DD): ");
        String appointmentDate = scanner.next();

        if (patient.getPatientById(patientId) && doctor.getDoctorById(doctorId)) {
            // Fixed missing parenthesis
            if (checkDoctorAvailability(doctorId, appointmentDate, connection)) {
                String query = "INSERT INTO appoinments (patient_id, doctor_id, appointment_date) VALUES (?, ?, ?)";
                try {
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, patientId);
                    statement.setInt(2, doctorId);
                    statement.setString(3, appointmentDate);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Appointment booked successfully!");
                    } else {
                        System.out.println("Failed to book appointment. Please try again.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Doctor not available on this date!");
            }
        } else {
            System.out.println("Either patient or doctor not found. Please check the IDs and try again.");
        }
    }

    private static boolean checkDoctorAvailability(int doctorId, String appointmentDate, Connection connection) {
        String query = "SELECT COUNT(*) FROM appoinments WHERE doctor_id = ? AND appointment_date = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, doctorId);
            statement.setString(2, appointmentDate);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                // Assuming a doctor can have a maximum of 10 appointments per day
                if (count == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
