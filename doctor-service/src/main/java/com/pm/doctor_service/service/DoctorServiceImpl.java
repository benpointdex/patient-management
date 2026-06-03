package com.pm.doctor_service.service;

import com.pm.doctor_service.kafka.DoctorKafkaProducer;
import com.pm.doctor_service.model.*;
import com.pm.doctor_service.model.enums.*;
import com.pm.doctor_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private SpecializationRepository specializationRepository;


    private final DoctorKafkaProducer doctorKafkaProducer;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Autowired
    private DoctorEducationRepository doctorEducationRepository;

    @Autowired
    private DoctorExperienceRepository doctorExperienceRepository;

    @Autowired
    private DoctorDocumentRepository doctorDocumentRepository;

    @Autowired
    private DoctorLeaveRepository doctorLeaveRepository;

    @Autowired
    private DoctorStatsRepository doctorStatsRepository;

    public DoctorServiceImpl(DoctorKafkaProducer doctorKafkaProducer) {
        this.doctorKafkaProducer = doctorKafkaProducer;
    }

    // Specialization & Department
    @Override
    public Specialization createSpecialization(Specialization specialization) {
        return specializationRepository.save(specialization);
    }

    @Override
    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    @Override
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Doctor CRUD
    @Override
    public synchronized Doctor createDoctor(Doctor doctor, Long specializationId, Long departmentId) {
        if (doctor.getEmployeeId() == null || doctor.getEmployeeId().trim().isEmpty()) {
            doctor.setEmployeeId(generateNextEmployeeId());
        }

        if (specializationId != null) {
            Specialization spec = specializationRepository.findById(specializationId)
                    .orElseThrow(() -> new IllegalArgumentException("Specialization not found with id: " + specializationId));
            doctor.setSpecialization(spec);
        }
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));
            doctor.setDepartment(dept);
        }
        
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Publish event asynchronously via your Kafka Package!
        doctorKafkaProducer.sendDoctorEvent(savedDoctor, "DOCTOR_CREATED");
        // Initialize Stats record
        DoctorStats stats = new DoctorStats();
        stats.setDoctor(savedDoctor);
        stats.setTotalAppointments(0);
        stats.setCompletedAppointments(0);
        stats.setCancelledAppointments(0);
        stats.setPatientsHandled(0);
        stats.setTotalRevenueGenerated(BigDecimal.ZERO);
        stats.setAverageRating(0.0);
        doctorStatsRepository.save(stats);

        return savedDoctor;
    }

    private String generateNextEmployeeId() {
        int year = java.time.LocalDate.now().getYear();
        String pattern = "DR-" + year + "-%";
        List<String> employeeIds = doctorRepository.findEmployeeIdsMatchingPattern(pattern);
        
        int maxSeq = 0;
        for (String id : employeeIds) {
            try {
                String[] parts = id.split("-");
                if (parts.length == 3) {
                    int seq = Integer.parseInt(parts[2]);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                }
            } catch (Exception e) {
                // Ignore malformed ids
            }
        }
        
        int nextSeq = maxSeq + 1;
        return String.format("DR-%d-%03d", year, nextSeq);
    }

    @Override
    public Doctor getDoctorById(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + doctorId));
    }

    @Override
    public List<Doctor> getAllDoctors(Long specializationId, Long departmentId, String status, String availabilityStatus) {
        List<Doctor> doctors = doctorRepository.findAll();

        return doctors.stream()
                .filter(doc -> specializationId == null || (doc.getSpecialization() != null && doc.getSpecialization().getSpecializationId().equals(specializationId)))
                .filter(doc -> departmentId == null || (doc.getDepartment() != null && doc.getDepartment().getDepartmentId().equals(departmentId)))
                .filter(doc -> status == null || doc.getStatus().name().equalsIgnoreCase(status))
                .filter(doc -> availabilityStatus == null || doc.getAvailabilityStatus().name().equalsIgnoreCase(availabilityStatus))
                .collect(Collectors.toList());
    }

    @Override
    public Doctor updateDoctor(UUID doctorId, Doctor doctorDetails, Long specializationId, Long departmentId) {
        Doctor doctor = getDoctorById(doctorId);

        doctor.setFirstName(doctorDetails.getFirstName());
        doctor.setLastName(doctorDetails.getLastName());
        doctor.setEmail(doctorDetails.getEmail());
        doctor.setPhoneNumber(doctorDetails.getPhoneNumber());
        doctor.setGender(doctorDetails.getGender());
        doctor.setDateOfBirth(doctorDetails.getDateOfBirth());
        doctor.setProfileImageUrl(doctorDetails.getProfileImageUrl());
        doctor.setQualification(doctorDetails.getQualification());
        doctor.setExperienceYears(doctorDetails.getExperienceYears());
        doctor.setBio(doctorDetails.getBio());
        doctor.setLanguagesSpoken(doctorDetails.getLanguagesSpoken());
        doctor.setMedicalLicenseNumber(doctorDetails.getMedicalLicenseNumber());
        doctor.setLicenseExpiryDate(doctorDetails.getLicenseExpiryDate());
        doctor.setConsultationFee(doctorDetails.getConsultationFee());
        doctor.setEmploymentType(doctorDetails.getEmploymentType());
        doctor.setJoiningDate(doctorDetails.getJoiningDate());
        doctor.setUpdatedBy(doctorDetails.getUpdatedBy());

        if (specializationId != null) {
            Specialization spec = specializationRepository.findById(specializationId)
                    .orElseThrow(() -> new IllegalArgumentException("Specialization not found with id: " + specializationId));
            doctor.setSpecialization(spec);
        } else {
            doctor.setSpecialization(null);
        }

        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));
            doctor.setDepartment(dept);
        } else {
            doctor.setDepartment(null);
        }

        return doctorRepository.save(doctor);
    }

    @Override
    public void deleteDoctor(UUID doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        doctor.setStatus(Status.INACTIVE);
        doctor.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctorStatus(UUID doctorId, String statusStr, String availabilityStatusStr) {
        Doctor doctor = getDoctorById(doctorId);
        if (statusStr != null) {
            doctor.setStatus(Status.valueOf(statusStr.toUpperCase()));
        }
        if (availabilityStatusStr != null) {
            doctor.setAvailabilityStatus(AvailabilityStatus.valueOf(availabilityStatusStr.toUpperCase()));
        }
        Doctor updatedDoctor = doctorRepository.save(doctor);
        // Publish status update event!
        doctorKafkaProducer.sendDoctorEvent(updatedDoctor, "STATUS_CHANGED");
        return updatedDoctor;
    }

    // Availability & Leaves
    @Override
    public DoctorAvailability addAvailability(UUID doctorId, DoctorAvailability availability) {
        Doctor doctor = getDoctorById(doctorId);
        availability.setDoctor(doctor);
        return doctorAvailabilityRepository.save(availability);
    }

    @Override
    public List<DoctorAvailability> getAvailabilityByDoctorId(UUID doctorId) {
        return doctorAvailabilityRepository.findByDoctor_DoctorId(doctorId);
    }

    @Override
    public DoctorLeave requestLeave(UUID doctorId, DoctorLeave leave) {
        Doctor doctor = getDoctorById(doctorId);
        if (leave.getStartDate().isAfter(leave.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        // Check for overlapping leaves (status PENDING or APPROVED)
        List<DoctorLeave> existingLeaves = doctorLeaveRepository.findByDoctor_DoctorId(doctorId);
        for (DoctorLeave existing : existingLeaves) {
            if (existing.getStatus() == LeaveStatus.REJECTED) {
                continue;
            }
            if (!leave.getStartDate().isAfter(existing.getEndDate()) && !existing.getStartDate().isAfter(leave.getEndDate())) {
                throw new IllegalArgumentException("Overlap Detected: There is already a leave request (" + existing.getStatus() + ") scheduled during this time (" + existing.getStartDate() + " to " + existing.getEndDate() + ").");
            }
        }

        leave.setDoctor(doctor);
        leave.setStatus(LeaveStatus.PENDING);
        return doctorLeaveRepository.save(leave);
    }

    @Override
    public DoctorLeave approveLeave(Long leaveId, String statusStr, String approvedBy) {
        DoctorLeave leave = doctorLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave record not found with id: " + leaveId));

        LeaveStatus targetStatus = LeaveStatus.valueOf(statusStr.toUpperCase());
        leave.setStatus(targetStatus);
        leave.setApprovedBy(approvedBy);

        DoctorLeave savedLeave = doctorLeaveRepository.save(leave);

        // If leave is approved, toggle doctor status automatically
        if (targetStatus == LeaveStatus.APPROVED) {
            Doctor doctor = leave.getDoctor();
            doctor.setStatus(Status.ON_LEAVE);
            doctor.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
            doctorRepository.save(doctor);
        }

        return savedLeave;
    }

    @Override
    public List<DoctorLeave> getAllLeaves() {
        return doctorLeaveRepository.findAll();
    }

    @Override
    public List<DoctorLeave> getLeavesByDoctorId(UUID doctorId) {
        return doctorLeaveRepository.findByDoctor_DoctorId(doctorId);
    }

    @Override
    public DoctorLeave requestEarlyReturn(Long leaveId, java.time.LocalDate returnDate, String reason) {
        DoctorLeave leave = doctorLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave record not found with id: " + leaveId));
        if (leave.getStatus() != LeaveStatus.APPROVED) {
            throw new IllegalStateException("Can only request early return for approved leaves");
        }
        if (returnDate.isBefore(leave.getStartDate()) || returnDate.isAfter(leave.getEndDate())) {
            throw new IllegalArgumentException("Return date must be within the leave period: " + leave.getStartDate() + " to " + leave.getEndDate());
        }
        leave.setEarlyReturnRequested(true);
        leave.setEarlyReturnDate(returnDate);
        leave.setEarlyReturnReason(reason);
        return doctorLeaveRepository.save(leave);
    }

    @Override
    public DoctorLeave approveEarlyReturn(Long leaveId, boolean approve, String approvedBy) {
        DoctorLeave leave = doctorLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave record not found with id: " + leaveId));
        if (!Boolean.TRUE.equals(leave.getEarlyReturnRequested())) {
            throw new IllegalStateException("No pending early return request for this leave");
        }

        if (approve) {
            java.time.LocalDate returnDate = leave.getEarlyReturnDate();
            if (returnDate.isAfter(leave.getStartDate())) {
                leave.setEndDate(returnDate.minusDays(1));
            } else {
                leave.setStatus(LeaveStatus.REJECTED);
            }

            if (!returnDate.isAfter(java.time.LocalDate.now())) {
                Doctor doctor = leave.getDoctor();
                doctor.setStatus(Status.ACTIVE);
                doctor.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
                doctorRepository.save(doctor);
            }
        }

        leave.setEarlyReturnRequested(false);
        leave.setEarlyReturnDate(null);
        leave.setEarlyReturnReason(null);

        return doctorLeaveRepository.save(leave);
    }

    // Education, Experience, & Documents
    @Override
    public DoctorEducation addEducation(UUID doctorId, DoctorEducation education) {
        Doctor doctor = getDoctorById(doctorId);
        education.setDoctor(doctor);
        return doctorEducationRepository.save(education);
    }

    @Override
    public DoctorExperience addExperience(UUID doctorId, DoctorExperience experience) {
        Doctor doctor = getDoctorById(doctorId);
        experience.setDoctor(doctor);
        return doctorExperienceRepository.save(experience);
    }

    @Override
    public DoctorDocument addDocument(UUID doctorId, DoctorDocument document) {
        Doctor doctor = getDoctorById(doctorId);
        document.setDoctor(doctor);
        return doctorDocumentRepository.save(document);
    }

    @Override
    public List<DoctorDocument> getDocumentsByDoctorId(UUID doctorId) {
        return doctorDocumentRepository.findByDoctor_DoctorId(doctorId);
    }

    // Doctor Stats
    @Override
    public DoctorStats getStatsByDoctorId(UUID doctorId) {
        return doctorStatsRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Stats not found for doctor with id: " + doctorId));
    }

    @Override
    public DoctorStats updateStats(UUID doctorId, int totalAppointmentsDelta, int completedAppointmentsDelta,
                                   int cancelledAppointmentsDelta, int patientsHandledDelta, double revenueDelta, Double ratingScore) {
        DoctorStats stats = getStatsByDoctorId(doctorId);

        int oldCompleted = stats.getCompletedAppointments();
        stats.setTotalAppointments(stats.getTotalAppointments() + totalAppointmentsDelta);
        stats.setCompletedAppointments(stats.getCompletedAppointments() + completedAppointmentsDelta);
        stats.setCancelledAppointments(stats.getCancelledAppointments() + cancelledAppointmentsDelta);
        stats.setPatientsHandled(stats.getPatientsHandled() + patientsHandledDelta);
        stats.setTotalRevenueGenerated(stats.getTotalRevenueGenerated().add(BigDecimal.valueOf(revenueDelta)));

        // Compute moving rating average
        if (ratingScore != null && ratingScore >= 0.0 && ratingScore <= 5.0) {
            Double currentAvg = stats.getAverageRating();
            if (currentAvg == null || currentAvg == 0.0) {
                stats.setAverageRating(ratingScore);
            } else {
                int totalCompleted = stats.getCompletedAppointments();
                if (totalCompleted > 0) {
                    double calculatedAvg = ((currentAvg * oldCompleted) + ratingScore) / totalCompleted;
                    stats.setAverageRating(calculatedAvg);
                } else {
                    stats.setAverageRating(ratingScore);
                }
            }
        }

        stats.setUpdatedAt(LocalDateTime.now());
        return doctorStatsRepository.save(stats);
    }


}
