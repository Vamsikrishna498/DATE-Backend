package com.farmer.Form.Entity;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
 
@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    // Employee Details
    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String nationality;
    private LocalDate dob;
    private String photoFileName;
 
    // Contact Details
    private String contactNumber;
    private String email;
 
    // Other Details
    private String relationType;
    private String relationName;
    private String altNumber;
    private String altNumberType;
 
    // Address
    private String country;
    private String state;
    private String district;
    private String block;
    private String village;
    private String zipcode;
    private String sector;
 
    // Professional
    private String education;
    private String experience;
 
    // Bank
    private String bankName;
    private String accountNumber;
    private String branchName;
    private String ifscCode;
    private String passbookFileName;
 
    // Documents
    private String documentType;
    private String documentNumber;
    private String documentFileName;
 
    // Portal Access
    private String role;
    private String accessStatus;
}
 