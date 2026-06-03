package com.pm.doctor_service.service;

import com.pm.doctor_service.model.*;

import java.util.List;
import java.util.UUID;

public interface DoctorService {
    // Specialization & Department
    Specialization createSpecialization(Specialization specialization);
    List<Specialization> getAllSpecializations();
    
    Department createDepartment(Department department);
    List<Department> getAllDepartments();

    // Doctor CRUD
    Doctor createDoctor(Doctor doctor, Long specializationId, Long departmentId);
    Doctor getDoctorById(UUID doctorId);
    List<Doctor> getAllDoctors(Long specializationId, Long departmentId, String status, String availabilityStatus);
    Doctor updateDoctor(UUID doctorId, Doctor doctor, Long specializationId, Long departmentId);
    void deleteDoctor(UUID doctorId);
    Doctor updateDoctorStatus(UUID doctorId, String status, String availabilityStatus);

    // Availability & Leaves
    DoctorAvailability addAvailability(UUID doctorId, DoctorAvailability availability);
    List<DoctorAvailability> getAvailabilityByDoctorId(UUID doctorId);
    
    DoctorLeave requestLeave(UUID doctorId, DoctorLeave leave);
    DoctorLeave approveLeave(Long leaveId, String status, String approvedBy);
    List<DoctorLeave> getAllLeaves();
    List<DoctorLeave> getLeavesByDoctorId(UUID doctorId);
    DoctorLeave requestEarlyReturn(Long leaveId, java.time.LocalDate returnDate, String reason);
    DoctorLeave approveEarlyReturn(Long leaveId, boolean approve, String approvedBy);

    // Education, Experience, & Documents
    DoctorEducation addEducation(UUID doctorId, DoctorEducation education);
    DoctorExperience addExperience(UUID doctorId, DoctorExperience experience);
    DoctorDocument addDocument(UUID doctorId, DoctorDocument document);
    List<DoctorDocument> getDocumentsByDoctorId(UUID doctorId);

    // Doctor Stats
    DoctorStats getStatsByDoctorId(UUID doctorId);
    DoctorStats updateStats(UUID doctorId, int totalAppointmentsDelta, int completedAppointmentsDelta, 
                           int cancelledAppointmentsDelta, int patientsHandledDelta, double revenueDelta, Double ratingScore);
}
