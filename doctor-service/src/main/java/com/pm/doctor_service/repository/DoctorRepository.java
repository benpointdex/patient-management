package com.pm.doctor_service.repository;

import com.pm.doctor_service.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    Optional<Doctor> findByEmployeeId(String employeeId);
    Optional<Doctor> findByEmail(String email);
    List<Doctor> findBySpecialization_SpecializationId(Long specializationId);
    List<Doctor> findByDepartment_DepartmentId(Long departmentId);

    @org.springframework.data.jpa.repository.Query("SELECT d.employeeId FROM Doctor d WHERE d.employeeId LIKE :pattern")
    List<String> findEmployeeIdsMatchingPattern(@org.springframework.data.repository.query.Param("pattern") String pattern);
}
