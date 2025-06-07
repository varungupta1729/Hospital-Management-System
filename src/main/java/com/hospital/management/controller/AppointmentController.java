package com.hospital.management.controller;

import com.hospital.management.model.Appointment;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.repository.UserRepository;
import com.hospital.management.service.AppointmentService;

import com.hospital.management.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.List;
@Controller
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/patient/appointment/{id}")
    public String showForm(Model model, @PathVariable("id") int doctorId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Patient patient = appointmentService.getPatientByUsername(username);
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

        boolean scheduled = appointmentService.scheduleAppointment(doctorId, username, appointment);

        if (!scheduled) {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            model.addAttribute("error", "This time slot is already booked.");
            model.addAttribute("doctorName", doctor.getName());
            model.addAttribute("specialization", doctor.getSpecialization());
            model.addAttribute("availability", doctor.getAvailabilitySchedule());
            model.addAttribute("id", doctor.getDoctorId());
            return "appointment-form";
        }

        redirectAttributes.addFlashAttribute("appointment", appointment);
        redirectAttributes.addFlashAttribute("doctorId", doctorId);
        return "redirect:/appointment-recipt/";
    }

    @GetMapping("/appointment-recipt/")
    public String finalRecipt(@ModelAttribute("appointment") Appointment appointment,
                              @ModelAttribute("doctorId") int doctorId,
                              Model model) {
        Doctor doctor = doctorService.getDoctorById(doctorId);

        model.addAttribute("appointment", appointment);
        model.addAttribute("doctorName", doctor.getName());
        model.addAttribute("specialization", doctor.getSpecialization());
        model.addAttribute("availability", doctor.getAvailabilitySchedule());

        return "appointment-recipt";
    }

    @GetMapping("/appointment-update/{id}")
    public String updateAppointment(@PathVariable("id") int appointmentId, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        model.addAttribute("appointment", appointment);
        model.addAttribute("doctorName", appointment.getDoctor().getName());
        model.addAttribute("specialization", appointment.getDoctor().getSpecialization());
        model.addAttribute("availability", appointment.getDoctor().getAvailabilitySchedule());
        model.addAttribute("patientName", appointment.getPatient().getName());
        model.addAttribute("id", appointment.getDoctor().getDoctorId());

        return "appointment-form";
    }

    @GetMapping("/allappointment")
    public String showAllAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return "allappointments";
    }

    @GetMapping("/appointment-delete/{id}")
    public String deleteAppointment(@PathVariable("id") int appointmentId) {
        appointmentService.deleteAppointmentById(appointmentId);
        return "redirect:/allappointment";
    }

    @GetMapping("/patient/viewAppointments")
    public String viewPatientAppointment(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Appointment> appointments = appointmentService.getAppointmentsForPatient(username);
        model.addAttribute("appointments", appointments);
        return "patient-appointment";
    }

    @GetMapping("/doctor/viewAppointments")
    public String viewDoctorAppointment(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Appointment> appointments = appointmentService.getAppointmentsForDoctor(username);
        model.addAttribute("appointments", appointments);
        return "doctor-appointment";
    }
}
