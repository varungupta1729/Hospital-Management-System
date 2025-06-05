package com.hospital.management.controller;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.PatientRepository;
import com.hospital.management.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.hospital.management.service.PatientService;

import java.util.List;

@Controller
public class AdminController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Autowired
    public AdminController(PatientService patientService, DoctorService doctorService, DoctorRepository doctorRepository, PatientRepository patientRepository){
        this.patientService=patientService;
        this.doctorService=doctorService;
        this.patientRepository=patientRepository;
        this.doctorRepository=doctorRepository;
    }
    @GetMapping("/admin")
    public String adminPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Display a hello message with the username
        model.addAttribute("message", "Hello " + userDetails.getUsername() + ", you are logged in as Admin.");
        model.addAttribute("noOfPatients",patientRepository.count());
        model.addAttribute("noOfDoctors",doctorRepository.count());
        return "admin"; // Thymeleaf will render admin.html
    }
    @GetMapping("/patientRegister")
    public String showPatientForm() {
        return "patientRegister"; // Ensure patientRegister.jsp exists
    }

    @GetMapping("/doctorRegister")
    public String showDoctorForm() {
        return "doctorRegister"; // Ensure doctorRegister.jsp exists
    }

    @GetMapping("/doctorDetails")
    public String showDoctorDetails(Model model){
        List<Doctor> doctors=doctorService.getAllDoctors();
        model.addAttribute("doctors",doctors);
        return "doctorDetails";
    }

    @GetMapping("/patientDetails")
    public String showPatientDetails(Model model){
        List<Patient> patients = patientService.getAllPatients();
        model.addAttribute("patients",patients);
        return "patientDetails";
    }

    @GetMapping("/access-denied")
    public String showAccessDenied(){
        return "access-denied";
    }

}
