package com.patient_service.serivce;

import com.patient_service.Dto.PatientRequest;
import com.patient_service.Dto.PatientResponse;
import com.patient_service.Model.Patient;
import com.patient_service.grpc.BillingServiceGrpcClient;
import com.patient_service.kafka.KafkaProducer;
import com.patient_service.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    private final KafkaProducer kafkaProducer;
    public PatientResponse mapToDto(Patient patient){

        PatientResponse patientResponse=new PatientResponse();

        patientResponse.setId(patient.getId().toString());
        patientResponse.setName(patient.getName());
        patientResponse.setAddress(patient.getAddress());
        patientResponse.setEmail(patient.getEmail());
        patientResponse.setDateOfBirth(patient.getDateOfBirth().toString());

        return patientResponse;
    }

    public Patient requestToPatient(PatientRequest patientRequest){

        Patient patient = new Patient();
        patient.setName(patientRequest.getName());
        patient.setEmail(patientRequest.getEmail());
        patient.setAddress(patientRequest.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequest.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientRequest.getRegisteredDate()));
        return patient;
    }

    public List<PatientResponse> getPatients(){

        PatientResponse patientResponse= new PatientResponse();
        List<Patient> patientList = patientRepository.findAll();
        return patientList.stream().map(this::mapToDto).toList();
    }

    public PatientResponse createPatient(PatientRequest patientRequest){
        if(patientRepository.existsByEmail(patientRequest.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT , "Patient with this email already exists");
        }
        Patient patient1 = patientRepository.save(requestToPatient(patientRequest));

        billingServiceGrpcClient.createBillingAccount(patient1.getId().toString(),patient1.getEmail(),
                patient1.getName());
        kafkaProducer.sendEvent(patient1);
        return mapToDto(patient1);
    }

    public PatientResponse updatePatient(UUID id, PatientRequest patientRequest){
        Patient patient = patientRepository.findById(id).orElseThrow(()-> new RuntimeException("Patient not found"));
        if(patientRepository.existsByEmailAndIdNot(patientRequest.getEmail() , id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT , "Patient with this email already exists");
        }

        patient.setName(patientRequest.getName());
        patient.setEmail(patientRequest.getEmail());
        patient.setAddress(patientRequest.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequest.getDateOfBirth()));

        Patient saved = patientRepository.save(patient);

        return mapToDto(saved);

    }
}
