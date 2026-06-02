package com.patient_service.serivce;

import com.patient_service.Dto.PatientRequest;
import com.patient_service.Dto.PatientResponse;
import com.patient_service.Model.Patient;
import com.patient_service.grpc.BillingServiceGrpcClient;
import com.patient_service.grpc.DoctorServiceGrpcClient;
import com.patient_service.kafka.KafkaProducer;
import com.patient_service.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final DoctorServiceGrpcClient doctorServiceGrpcClient;
    private final KafkaProducer kafkaProducer;
    public PatientResponse mapToDto(Patient patient){

        PatientResponse patientResponse=new PatientResponse();

        patientResponse.setId(patient.getId().toString());
        patientResponse.setName(patient.getName());
        patientResponse.setAddress(patient.getAddress());
        patientResponse.setEmail(patient.getEmail());
        patientResponse.setDateOfBirth(patient.getDateOfBirth().toString());
        patientResponse.setGender(patient.getGender());
        patientResponse.setRegisteredDate(patient.getRegisteredDate().toString());

        return patientResponse;
    }

    public Patient requestToPatient(PatientRequest patientRequest){

        Patient patient = new Patient();
        patient.setName(patientRequest.getName());
        patient.setEmail(patientRequest.getEmail());
        patient.setAddress(patientRequest.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequest.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientRequest.getRegisteredDate()));
        patient.setGender(patientRequest.getGender());
        patient.setPassword(patientRequest.getPassword());
        return patient;
    }

    public List<PatientResponse> getPatients(){

        PatientResponse patientResponse= new PatientResponse();
        List<Patient> patientList = patientRepository.findAll();
        return patientList.stream().map(this::mapToDto).toList();
    }

    @CacheEvict(value = "patients", key = "#patientRequest.email")

    public PatientResponse createPatient(PatientRequest patientRequest){
        if (patientRequest.getPassword() == null || patientRequest.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        if(patientRepository.existsByEmail(patientRequest.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT , "Patient with this email already exists");
        }
        Patient patient1 = patientRepository.save(requestToPatient(patientRequest));

        try {
            // Provision secure user credentials in auth-service
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, String> authRequest = new java.util.HashMap<>();
            authRequest.put("email", patient1.getEmail());
            authRequest.put("password", patientRequest.getPassword());
            authRequest.put("role", "USER");

            restTemplate.postForEntity("http://auth-service:4005/register", authRequest, String.class);
            System.out.println("Patient user account provisioned successfully in auth-service");
        } catch (Exception e) {
            System.err.println("auth-service provisioning skipped/failed (likely already exists): " + e.getMessage());
        }

        try {
            billingServiceGrpcClient.createBillingAccount(patient1.getId().toString(), patient1.getEmail(), patient1.getName());
            kafkaProducer.sendEvent(patient1);
        } catch (Exception e) {
            System.err.println("Post-registration tasks failed: " + e.getMessage());
            // We log the error but still return success to the user since the patient is saved
        }

        return mapToDto(patient1);
    }

    @CacheEvict(value = "patients", key = "#patientRequest.email")

    public PatientResponse updatePatient(UUID id, PatientRequest patientRequest){
        Patient patient = patientRepository.findById(id).orElseThrow(()-> new RuntimeException("Patient not found"));
        if(patientRepository.existsByEmailAndIdNot(patientRequest.getEmail() , id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT , "Patient with this email already exists");
        }

        patient.setName(patientRequest.getName());
        patient.setEmail(patientRequest.getEmail());
        patient.setAddress(patientRequest.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequest.getDateOfBirth()));
        patient.setGender(patientRequest.getGender());

        Patient saved = patientRepository.save(patient);

        return mapToDto(saved);
    }

    @Cacheable(value = "patients", key = "#email")

    public PatientResponse getPatientByEmail(String email) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with email: " + email));
        return mapToDto(patient);
    }

    public java.util.Map<String, Object> checkDoctorVerification(String doctorId) {
        var response = doctorServiceGrpcClient.verifyDoctorActive(doctorId);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("doctorId", doctorId);
        result.put("isActive", response.getIsActive());
        result.put("fullName", response.getFullName());
        result.put("specialization", response.getSpecializationName());
        result.put("department", response.getDepartmentName());
        return result;
    }
}
