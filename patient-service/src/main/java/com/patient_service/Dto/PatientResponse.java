package com.patient_service.Dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PatientResponse implements Serializable {

    private String id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
    private String gender;
    private String registeredDate;
}
