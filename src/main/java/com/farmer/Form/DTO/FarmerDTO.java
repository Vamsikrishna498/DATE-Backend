package com.farmer.Form.DTO;

import lombok.*;

import java.time.LocalDate;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FarmerDTO {

    private Long id;
    
    // Personal Information
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String salutation;
    private String nationality;
    private String dateOfBirth;
    private String fatherName;
    private String contactNumber;
    private String alternativeContactNumber;
    private String alternativeRelationType;
    private String alternativeType;
    private String alternativeNumber;
    
    // Address
    private String country;
    private String state;
    private String district;
    private String block;
    private String mandal;
    private String village;
    private String pincode;
    
    // Professional Information
    private String education;
    private String experience;
    
    // Current Crop Information
    private String surveyNumber;
    private String totalLandHolding;
    private String geoTag;
    private String selectCrop;
    private String cropName;
    private String cropCategory;
    private String netIncome;
    private String soilTest;
    
    // Proposed Crop Information
    private String cropType;
    
    // Irrigation Details
    private String waterSource;
    private String borewellDischarge;
    private String summerDischarge;
    private String borewellLocation;
    
    // Bank Details
    private String bankName;
    private String accountNumber;
    private String branchName;
    private String ifscCode;
    
    // Documents
    private String aadharNumber;
    private String panNumber;
    private String voterId;
    private String ppbNumber;
    
    // File names for frontend
    private String photoFileName;
    private String photoUrl;
    private String soilTestCertificateFileName;
    private String passbookFileName;
    private String aadhaarFileName;
    private String panFileName;
    private String voterFileName;
    private String voterIdFileName;
    private String ppbFileName;

    // File uploads (for @ModelAttribute)
    private MultipartFile photo;
    private MultipartFile passbookFile;
    private MultipartFile soilTestCertificate;
    private MultipartFile aadhaarFile;
    private MultipartFile panFile;
    private MultipartFile voterFile;
    private MultipartFile ppbFile;
}