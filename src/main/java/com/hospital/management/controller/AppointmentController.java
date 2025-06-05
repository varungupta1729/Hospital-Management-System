package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.model.User;
import com.hospital.management.repository.AppointmentRepository;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.PatientRepository;
import com.hospital.management.repository.UserRepository;
import com.hospital.management.service.AppointmentService;

import com.hospital.management.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.AppointmentRepository;
import com.hospital.management.repository.DoctorRepository;
import com.hospital.management.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.List;

@Controller
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/patient/appointment/{id}")
    public String showForm(Model model, @PathVariable("id") int doctorId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorService.getDoctorById(doctorId);


        model.addAttribute("patientName", patient.getName());
        model.addAttribute("doctorName", doctor.getName());
        model.addAttribute("id", doctor.getDoctorId());
        model.addAttribute("specialization", doctor.getSpecialization());
        model.addAttribute("availability", doctor.getAvailabilitySchedule());
        model.addAttribute("appointment", new Appointment());

        return "appointment-form";
    }

    @PostMapping("/schedule-appointment/{id}")
    public String scheduleAppointment(@PathVariable("id") int doctorId,
                                      @ModelAttribute("appointment") Appointment appointment,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean isAvailable = appointmentRepository
                .findByDoctorAndAppointmentDateAndTimeSlot(doctor.getDoctorId(), appointment.getAppointmentDate(), appointment.getTimeSlot())
                .isEmpty();

        if (!isAvailable) {
            model.addAttribute("error", "This time slot is already booked.");
            model.addAttribute("doctorName", doctor.getName());
            model.addAttribute("specialization", doctor.getSpecialization());
            model.addAttribute("availability", doctor.getAvailabilitySchedule());
            model.addAttribute("id", doctor.getDoctorId());
            return "appointment-form";
        }

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointmentRepository.save(appointment);

        redirectAttributes.addFlashAttribute("appointment", appointment);
        redirectAttributes.addFlashAttribute("doctorId", doctorId);

        return "redirect:/appointment-recipt/";
    }

    @GetMapping("/appointment-recipt/")
    public String finalRecipt(@ModelAttribute("appointment") Appointment appointment,
                              @ModelAttribute("doctorId") int doctorId,
                              Model model) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        model.addAttribute("appointment", appointment);
        model.addAttribute("doctorName", doctor.getName());
        model.addAttribute("specialization", doctor.getSpecialization());
        model.addAttribute("availability", doctor.getAvailabilitySchedule());

        return "appointment-recipt";
    }

    @GetMapping("/appointment-update/{id}")
    public String updateAppointment(@PathVariable("id") int appointmentId, Model model) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        Doctor doctor = appointment.getDoctor();
        Patient patient = appointment.getPatient();

        model.addAttribute("appointment", appointment);
        model.addAttribute("doctorName", doctor.getName());
        model.addAttribute("specialization", doctor.getSpecialization());
        model.addAttribute("availability", doctor.getAvailabilitySchedule());
        model.addAttribute("patientName", patient.getName());
        model.addAttribute("id", doctor.getDoctorId());

        return "appointment-form";
    }


    @GetMapping("/allappointment")
    public String showAllAppointments(Model model) {
        List<Appointment> appointments = appointmentRepository.findAll();
        model.addAttribute("appointments", appointments);
        return "allappointments";
    }

    @GetMapping("/appointment-delete/{id}")
    public String deleteAppointment(@PathVariable("id") int appointmentId) {
        appointmentRepository.deleteById(appointmentId);
        return "redirect:/allappointment";
    }

    @GetMapping("/patient/viewAppointments")
    public String viewPatientAppointment(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patient.getPatientId());
        model.addAttribute("appointments", appointments);

        return "patient-appointment";
    }

    @GetMapping("/doctor/viewAppointments")
    public String viewDoctorAppointment(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Doctor doctor = doctorRepository.findByUser(user);
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }

        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctor.getDoctorId());
        model.addAttribute("appointments", appointments);

        return "doctor-appointment";
    }
}
