package com.hospital.management.controller;

import com.hospital.management.service.PatientService;
import org.springframework.ui.Model;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.model.User;
import com.hospital.management.repository.PatientRepository;
import com.hospital.management.repository.UserRepository;
import com.hospital.management.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @PostMapping("/register")
    public String registerPatient(@RequestParam("dateOfBirth") String dateOfBirthStr, @ModelAttribute Patient patient) {
        // Convert String to LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dob = LocalDate.parse(dateOfBirthStr, formatter);
        patient.setDateOfBirth(dob);

        // Step 1: Create a new User entry for the patient
        User user = new User();
        user.setUserName(patient.getName()); // Set username as patient name (Can modify if needed)
        user.setPassword("test"); // Generate or request a password
        user.setRole(User.Role.PATIENT); // Assign role as PATIENT
        userRepository.save(user); // Save user into the User table

        // Step 2: Associate user_id with patient
        patient.setUser(user);
        patientRepository.save(patient); // Save patient into the database

        return "success"; // Redirect to success page
    }
    @GetMapping("/dashboard")
    public String showDoctorDashboard(@RequestParam(value = "search", required = false) String search,Model model) {

        model.addAttribute("doctors", doctorService.getAllDoctors());



        List<Doctor> doctors;
        if (search != null && !search.isEmpty()) {
            doctors = doctorService.searchDoctorsByName(search);
            model.addAttribute("search", search);
        } else {
            doctors = doctorService.getAllDoctors();
        }
        model.addAttribute("doctors", doctors);

        return "patientDashboard"; // Loads doctorDashboard.html from templates folder
    }

    @GetMapping("/doctorDetails/{id}")
    public String showDoctorDetails(@PathVariable int  id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        return "Onedoctordetails";
    }


}

