package com.pm.billing_serivce.controller;

import com.pm.billing_serivce.model.MedicalService;
import com.pm.billing_serivce.repository.MedicalServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/services")
@RequiredArgsConstructor
public class MedicalServiceController {

    private final MedicalServiceRepository medicalServiceRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalService createService(@RequestBody MedicalService medicalService) {
        return medicalServiceRepository.save(medicalService);
    }

    @GetMapping
    public List<MedicalService> getAllServices() {
        return medicalServiceRepository.findAll();
    }

    @GetMapping("/category/{category}")
    public List<MedicalService> getServicesByCategory(@PathVariable String category) {
        return medicalServiceRepository.findByCategory(category);
    }
}
