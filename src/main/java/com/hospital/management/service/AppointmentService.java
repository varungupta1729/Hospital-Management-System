package com.hospital.management.service;


import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.management.model.Appointment;
import com.hospital.management.repository.AppointmentRepository;

@Service
public class AppointmentService {

        @Autowired
        private AppointmentRepository appointmentRepository;
        @Autowired
        private PatientRepository patientRepository;

      @Autowired
      private DoctorRepository doctorRepository;
       public List<Appointment> getAppointmentsByPatientId(int patientId){
        return appointmentRepository.findByPatient_PatientId(patientId);
        }
       
       public List<Appointment> getAppointmentsByDoctorId(int patientId){
           return appointmentRepository.findByDoctor_DoctorId(patientId);
           }
    public List<Appointment> getTodaysAppointmentsForDoctor(int doctorId) {

        System.out.println(doctorId);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return List.of();

        Date today = Date.valueOf(java.time.LocalDate.now()); // optional: truncate time if needed
        return appointmentRepository.findByDoctorAndAppointmentDate(doctor, today);
    }

    public Map<Appointment.Status, Long> getTodayAppointmentStatsForDoctor(int doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return Collections.emptyMap();

        Date today = Date.valueOf(java.time.LocalDate.now());
        List<Appointment> appointments = appointmentRepository.findByDoctorAndAppointmentDate(doctor, today);

        return appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
    }

//    public List<Patient> getExpiredAppointmentsForDoctor(int doctorId) {
//           System.out.println(doctorId +"Inside service");
//        List<Appointment> appointments = appointmentRepository.findByDoctor_DoctorId(doctorId);
//        System.out.println(appointments);
//        List<Patient> patients= appointments.stream()
//                .filter(appointment ->
//                        appointment.getAppointmentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
//                                .isEqual(LocalDate.now()) && hasTimePassed(appointment.getTimeSlot())
//                )
//                .map(Appointment::getPatient)
//                .peek(System.out::println)
//                .distinct()
//                .collect(Collectors.toList());
//        System.out.println(patients);
//         return patients;
//    }
//
//    private boolean hasTimePassed(String timeSlot) {
//        // Extract the first time from the range
//        String startTime = timeSlot.split("-")[0].trim();
//
//        startTime = startTime.replaceAll("\\s+", " ");
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
//        LocalTime appointmentTime = LocalTime.parse(startTime, formatter);
//        return LocalTime.now().isAfter(appointmentTime);
//    }


    public Map<String, Long> getDoctorDashboardStats(int doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctor_DoctorId(doctorId);

        LocalDate today = LocalDate.now();

        long confirmedCount = appointments.stream()
                .filter(appointment -> !appointment.getAppointmentDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate().isBefore(today) // Today and future
                        && appointment.getStatus() == Appointment.Status.CONFIRMED)
                .count();

        long scheduledCount = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate().isEqual(today)) // Only today
                .count();

        long totalPatients = appointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate().isBefore(today)) // Past appointments only
                .map(Appointment::getPatient)
                .distinct()
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("CONFIRMED", confirmedCount);
        stats.put("SCHEDULED", scheduledCount);
        stats.put("TOTAL", totalPatients);

        return stats;
    }

    public List<Patient> getAllPatientsForDoctor(int doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctor_DoctorId(doctorId);
        System.out.println("Fetched Appointments: " + appointments);

        List<Patient> patients = appointments.stream()
                .map(Appointment::getPatient)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Patients List: " + patients);
        return patients;
    }



}
