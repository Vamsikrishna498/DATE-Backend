package com.farmer.Form.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Personal Information (Step 0)
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String salutation;
    private String nationality;
    private LocalDate dateOfBirth;
    private String fatherName;
    private String contactNumber;
    private String alternativeContactNumber;
    private String alternativeRelationType;
    private String alternativeType;
    private String alternativeNumber;
    
    // Photo fields (support multiple naming conventions)
    private String photoFileName;        // e.g., "farmer_123.jpg"
    private String photoUrl;             // e.g., "/uploads/farmer_123.jpg"
    private String photoFilePath;
    
    // Address (Step 1)
    private String country;
    private String state;
    private String district;
    private String block;
    private String mandal;
    private String village;
    private String pincode;
    
    // Professional Information (Step 2)
    private String education;
    private String experience;
    
    // Current Crop Information (Step 3)
    private String surveyNumber;
    private String totalLandHolding;
    private String geoTag;
    private String selectCrop;
    private String cropName;
    private String cropCategory;
    private String netIncome;
    private String soilTest;
    private String soilTestCertificateFileName;
    
    // Proposed Crop Information (Step 4)
    private String cropType;
    
    // Irrigation Details (Step 5)
    private String waterSource;
    private String borewellDischarge;
    private String summerDischarge;
    private String borewellLocation;
    
    // Bank Details (Step 6)
    private String bankName;
    private String accountNumber;
    private String branchName;
    private String ifscCode;
    private String passbookFileName;
    
    // Documents (Step 7)
    private String aadharNumber;
    private String panNumber;
    private String voterId;
    private String ppbNumber;
    private String aadhaarFileName;
    private String panFileName;
    private String voterFileName;
    private String voterIdFileName;
    private String ppbFileName;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Assignment
    @ManyToOne
    private Employee assignedEmployee;

    // Enhanced KYC Management
    private Boolean kycApproved;
    
    // New KYC fields for detailed status management
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus = KycStatus.PENDING;
    
    private String kycRejectionReason;
    private String kycReferBackReason;
    private LocalDate kycSubmittedDate;
    private LocalDate kycReviewedDate;
    private String kycReviewedBy;
    
    // KYC Status Enum
    public enum KycStatus {
        PENDING,        // Not yet reviewed
        APPROVED,       // KYC approved
        REJECTED,       // KYC rejected
        REFER_BACK      // KYC referred back for more info
    }
}
