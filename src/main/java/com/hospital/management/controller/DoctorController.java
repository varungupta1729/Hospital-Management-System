package com.hospital.management.controller;

import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.model.User;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.UserRepository;
import com.hospital.management.service.AppointmentService;
import com.hospital.management.service.DoctorService;
import com.hospital.management.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    // View all doctors
    @GetMapping("/doctors")
    public String listDoctors(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "doctor-list";
    }

    // Register doctor (with user)
    @PostMapping("/doctor/register")
    public String registerDoctor(@ModelAttribute Doctor doctor) {
        doctorService.saveDoctorWithUser(doctor);
        return "redirect:/doctors";
    }

    // Show add doctor form
    @GetMapping("/doctors/add")
    public String showAddDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "add-doctor";
    }

    // Save new doctor
    @PostMapping("/doctors/save")
    public String saveDoctor(@ModelAttribute Doctor doctor) {
        doctorService.saveDoctor(doctor);
        return "redirect:/doctors";
    }

    // Doctor details
    @GetMapping("/doctors/{id}")
    public String getDoctorDetails(@PathVariable int id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        return "doctor-details";
    }

    // Show edit form
    @GetMapping("/doctors/edit/{id}")
    public String showEditDoctorForm(@PathVariable int id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        return "edit-doctor";
    }

    // Update doctor
    @PostMapping("/doctors/update/{id}")
    public String updateDoctor(@PathVariable int id, @ModelAttribute Doctor doctor) {
        doctorService.updateDoctor(id, doctor);
        return "redirect:/doctors";
    }

    // Show availability update form
    @GetMapping("/doctor/add-availability")
    public String showAvailabilityForm(Model model, Principal principal) {
        User user = userRepository.findByUserName(principal.getName()).orElse(null);
        if (user == null) return "error"; // Prevent null pointer

        Doctor doctor = doctorRepository.findByUser(user);
        model.addAttribute("doctor", doctor);

        Map<String, String> availabilityMap = new LinkedHashMap<>();
        if (doctor.getAvailabilitySchedule() != null) {
            String[] lines = doctor.getAvailabilitySchedule().split("\\r?\\n");
            for (String line : lines) {
                if (line.contains(":") && line.contains("-")) {
                    String daysRange = line.split(":")[0].trim();
                    String timeRange = line.split(":")[1].trim();
                    availabilityMap.put(daysRange, timeRange);
                }
            }
        }
        model.addAttribute("availabilityMap", availabilityMap);
        return "add-availability";
    }

    // Save availability update
    @PostMapping("/doctor/add-availability")
    public String saveAvailability(@RequestParam Map<String, String> params, Principal principal) {
        User user = userRepository.findByUserName(principal.getName()).orElse(null);
        if (user == null) return "error"; // Prevent null pointer

        Doctor doctor = doctorRepository.findByUser(user);
        String availability = params.getOrDefault("availability", "").trim();
        doctor.setAvailabilitySchedule(availability);
        doctorRepository.save(doctor);

        return "redirect:/doctorDashboard";
    }

    // Retrieve patients
    @GetMapping("/doctor/patients")
    public String getExpiredPatients(Model model, Principal principal) {
        User user = userRepository.findByUserName(principal.getName()).orElse(null);
        if (user == null) return "error"; // Prevent null pointer

        Doctor doctor = doctorRepository.findByUser(user);
        List<Patient> patients = appointmentService.getAllPatientsForDoctor(doctor.getDoctorId());
        //System.out.println(patients);
        model.addAttribute("patients", patients);

        return "patients-seen";
    }

    // Retrieve today's appointments for the doctor
    @GetMapping("/doctor/todays-appointments")
    public String getTodaysAppointments(Model model, Principal principal) {
        User user = userRepository.findByUserName(principal.getName()).orElse(null);
        if (user == null) return "error"; // Prevent null pointer

        Doctor doctor = doctorRepository.findByUser(user);
        model.addAttribute("appointments", appointmentService.getTodaysAppointmentsForDoctor(doctor.getDoctorId()));

        return "todays-appointments";
    }

    // Patient details view
    @GetMapping("/patient/{id}")
    public String showPatientDetails(@PathVariable int id, Model model) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) return "error"; // Prevent null pointer

        model.addAttribute("patient", patient);
        return "singlePatientDetails";
    }
}
