package com.farmer.Form.Entity;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
 
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    
    private String photoFileName;
    
    // For API response compatibility
    @Transient
    private String photoUrl;

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
    
    // Helper method to set photoUrl from photoFileName
    public void setPhotoUrlFromFileName() {
        if (this.photoFileName != null && !this.photoFileName.startsWith("http")) {
            this.photoUrl = "/uploads/photos/" + this.photoFileName;
        } else if (this.photoFileName != null) {
            this.photoUrl = this.photoFileName;
        }
    }
}
 