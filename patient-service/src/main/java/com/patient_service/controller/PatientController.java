package com.patient_service.controller;

import com.patient_service.Dto.PatientRequest;
import com.patient_service.Dto.PatientResponse;
import com.patient_service.serivce.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {


    private final PatientService patientService;

    @GetMapping

    public ResponseEntity<List<PatientResponse>> getPatients() {
        List<PatientResponse> patients = patientService.getPatients();
        return ResponseEntity.ok().body(patients);
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid
            @RequestBody PatientRequest patientRequestDTO) {

        PatientResponse patientResponseDTO = patientService.createPatient(
                patientRequestDTO);

        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatientDetails(@PathVariable UUID id, @RequestBody PatientRequest patientRequest){

        PatientResponse patientResponse = patientService.updatePatient(id, patientRequest);
        return ResponseEntity.ok().body(patientResponse);
    }
}
