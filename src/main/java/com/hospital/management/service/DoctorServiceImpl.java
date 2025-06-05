package com.hospital.management.service;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.User;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.UserRepository;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Doctor getDoctorById(int id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + id));
    }

    @Override
    public void updateDoctor(int id, Doctor updatedDoctor) {
        Doctor existingDoctor = getDoctorById(id);
        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setSpecialization(updatedDoctor.getSpecialization());
        existingDoctor.setContactNumber(updatedDoctor.getContactNumber());
        existingDoctor.setAvailabilitySchedule(updatedDoctor.getAvailabilitySchedule());
        doctorRepository.save(existingDoctor);
    }
    @Override
    public void saveDoctorWithUser(Doctor doctor) {
        // 1. Create a new User for the Doctor
        User user = new User();
        user.setUserName(doctor.getName().toLowerCase().replaceAll("\\s+", "") + doctor.getContactNumber());
        user.setPassword("{noop}doctor@123"); // Use BCrypt encoder in production
        user.setRole(User.Role.DOCTOR);

        // 2. Save User first to generate user_id
        User savedUser = userRepository.save(user);

        // 3. Set user in Doctor entity
        doctor.setUser(savedUser);

        // 4. Save Doctor
        doctorRepository.save(doctor);
    }
    @Override
    public List<String> getAvailabilitySlots(int doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getAvailabilitySchedule() == null) {
            return Collections.emptyList();
        }

        // Assuming newline or comma separated values
        return Arrays.stream(doctor.getAvailabilitySchedule().split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }
    @Override
    public void saveDoctor(Doctor doctor) {
        doctorRepository.save(doctor);
    }

    @Override
    public boolean isDoctorAvailableToday(Doctor doctor) {
        System.out.println(doctor.getAvailabilitySchedule());
        if (doctor.getAvailabilitySchedule() == null) return false;

        String today = LocalDate.now().getDayOfWeek().name(); // e.g., "FRIDAY"
        LocalTime now = LocalTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mma");

        System.out.println("Today's day: " + today);
        System.out.println("Current time: " + now);

        return Arrays.stream(doctor.getAvailabilitySchedule().split("\\r?\\n"))
                .map(String::toUpperCase)
                .anyMatch(line -> {
                    System.out.println("Processing line: " + line);
                    String[] parts = line.split(" ");
                    if (parts.length == 3) {
                        String daysRange = parts[0];
                        String startTime = parts[1];
                        String endTime = parts[2];

                        System.out.println("Days range: " + daysRange);
                        System.out.println("Start time: " + startTime);
                        System.out.println("End time: " + endTime);

                        if (daysRange.contains("-")) {
                            String[] days = daysRange.split("-");
                            if (days.length == 2) {
                                String startDay = days[0].trim();
                                String endDay = days[1].trim();
                                if (isTodayInRange(startDay, endDay, today)) {
                                    LocalTime start = LocalTime.parse(startTime, timeFormatter);
                                    LocalTime end = LocalTime.parse(endTime, timeFormatter);
                                    System.out.println("Start time parsed: " + start);
                                    System.out.println("End time parsed: " + end);
                                    return now.isAfter(start) && now.isBefore(end);
                                }
                            }
                        } else if (daysRange.equals(today)) {
                            LocalTime start = LocalTime.parse(startTime, timeFormatter);
                            LocalTime end = LocalTime.parse(endTime, timeFormatter);
                            System.out.println("Start time parsed: " + start);
                            System.out.println("End time parsed: " + end);
                            return now.isAfter(start) && now.isBefore(end);
                        }
                    }
                    return false;
                });
    }

    private boolean isTodayInRange(String start, String end, String today) {
        List<String> days = Arrays.asList(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        );
        int startIndex = days.indexOf(start);
        int endIndex = days.indexOf(end);
        int todayIndex = days.indexOf(today);

        System.out.println("Start index: " + startIndex);
        System.out.println("End index: " + endIndex);
        System.out.println("Today index: " + todayIndex);

        if (startIndex == -1 || endIndex == -1 || todayIndex == -1) return false;

        return todayIndex >= startIndex && todayIndex <= endIndex;
    }


    @Override
    public List<Doctor> searchDoctorsByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
}
