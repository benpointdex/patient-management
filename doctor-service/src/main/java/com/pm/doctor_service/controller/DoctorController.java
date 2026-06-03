package com.pm.doctor_service.controller;

import com.pm.doctor_service.model.*;
import com.pm.doctor_service.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Specializations
    @PostMapping("/specializations")
    public ResponseEntity<Specialization> createSpecialization(@Valid @RequestBody Specialization specialization) {
        return new ResponseEntity<>(doctorService.createSpecialization(specialization), HttpStatus.CREATED);
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok(doctorService.getAllSpecializations());
    }

    // Departments
    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        return new ResponseEntity<>(doctorService.createDepartment(department), HttpStatus.CREATED);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(doctorService.getAllDepartments());
    }

    // Doctors CRUD
    @PostMapping
    public ResponseEntity<Doctor> createDoctor(
            @Valid @RequestBody Doctor doctor,
            @RequestParam(required = false) Long specializationId,
            @RequestParam(required = false) Long departmentId) {
        return new ResponseEntity<>(doctorService.createDoctor(doctor, specializationId, departmentId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors(
            @RequestParam(required = false) Long specializationId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String availabilityStatus) {
        return ResponseEntity.ok(doctorService.getAllDoctors(specializationId, departmentId, status, availabilityStatus));
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @PutMapping("/{doctorId}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable UUID doctorId,
            @Valid @RequestBody Doctor doctor,
            @RequestParam(required = false) Long specializationId,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorId, doctor, specializationId, departmentId));
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable UUID doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{doctorId}/status")
    public ResponseEntity<Doctor> updateDoctorStatus(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String availabilityStatus) {
        return ResponseEntity.ok(doctorService.updateDoctorStatus(doctorId, status, availabilityStatus));
    }

    // Availability & Leaves
    @PostMapping("/{doctorId}/availability")
    public ResponseEntity<DoctorAvailability> addAvailability(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorAvailability availability) {
        return new ResponseEntity<>(doctorService.addAvailability(doctorId, availability), HttpStatus.CREATED);
    }

    @GetMapping("/{doctorId}/availability")
    public ResponseEntity<List<DoctorAvailability>> getAvailabilityByDoctorId(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getAvailabilityByDoctorId(doctorId));
    }

    @PostMapping("/{doctorId}/leaves")
    public ResponseEntity<DoctorLeave> requestLeave(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorLeave leave) {
        return new ResponseEntity<>(doctorService.requestLeave(doctorId, leave), HttpStatus.CREATED);
    }

    @PatchMapping("/leaves/{leaveId}/approve")
    public ResponseEntity<DoctorLeave> approveLeave(
            @PathVariable Long leaveId,
            @RequestParam String status,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(doctorService.approveLeave(leaveId, status, approvedBy));
    }

    @PostMapping("/leaves/{leaveId}/early-return")
    public ResponseEntity<DoctorLeave> requestEarlyReturn(
            @PathVariable Long leaveId,
            @RequestParam String returnDate,
            @RequestParam String reason) {
        java.time.LocalDate date = java.time.LocalDate.parse(returnDate);
        return ResponseEntity.ok(doctorService.requestEarlyReturn(leaveId, date, reason));
    }

    @PostMapping("/leaves/{leaveId}/early-return/approve")
    public ResponseEntity<DoctorLeave> approveEarlyReturn(
            @PathVariable Long leaveId,
            @RequestParam boolean approve,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(doctorService.approveEarlyReturn(leaveId, approve, approvedBy));
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<DoctorLeave>> getAllLeaves() {
        return ResponseEntity.ok(doctorService.getAllLeaves());
    }

    @GetMapping("/{doctorId}/leaves")
    public ResponseEntity<List<DoctorLeave>> getLeavesByDoctorId(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getLeavesByDoctorId(doctorId));
    }

    // Education, Experience, & Documents
    @PostMapping("/{doctorId}/education")
    public ResponseEntity<DoctorEducation> addEducation(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorEducation education) {
        return new ResponseEntity<>(doctorService.addEducation(doctorId, education), HttpStatus.CREATED);
    }

    @PostMapping("/{doctorId}/experience")
    public ResponseEntity<DoctorExperience> addExperience(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorExperience experience) {
        return new ResponseEntity<>(doctorService.addExperience(doctorId, experience), HttpStatus.CREATED);
    }

    @PostMapping("/{doctorId}/documents")
    public ResponseEntity<DoctorDocument> addDocument(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorDocument document) {
        return new ResponseEntity<>(doctorService.addDocument(doctorId, document), HttpStatus.CREATED);
    }

    @GetMapping("/{doctorId}/documents")
    public ResponseEntity<List<DoctorDocument>> getDocumentsByDoctorId(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getDocumentsByDoctorId(doctorId));
    }

    // Stats
    @GetMapping("/{doctorId}/stats")
    public ResponseEntity<DoctorStats> getStatsByDoctorId(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getStatsByDoctorId(doctorId));
    }

    @PatchMapping("/{doctorId}/stats")
    public ResponseEntity<DoctorStats> updateStats(
            @PathVariable UUID doctorId,
            @RequestParam int totalAppointmentsDelta,
            @RequestParam int completedAppointmentsDelta,
            @RequestParam int cancelledAppointmentsDelta,
            @RequestParam int patientsHandledDelta,
            @RequestParam double revenueDelta,
            @RequestParam(required = false) Double ratingScore) {
        return ResponseEntity.ok(doctorService.updateStats(
                doctorId, totalAppointmentsDelta, completedAppointmentsDelta, 
                cancelledAppointmentsDelta, patientsHandledDelta, revenueDelta, ratingScore));
    }
}
