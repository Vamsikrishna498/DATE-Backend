package com.farmer.Form.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "farmers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // File uploads (replacing Base64 fields)
    private String photoFileName;
    private String passbookFileName;
    private String documentFileName;
    private String soilTestCertificateFileName;

    // Personal Info
    @NotBlank private String salutation;
    private String firstName;
    private String middleName;
    @NotBlank private String lastName;
    @NotNull private LocalDate dateOfBirth;
    @NotNull private String gender;
    private String fatherName;
    @Pattern(regexp="^\\d{10}$") private String contactNumber;
    private String alternativeRelationType;
    @Pattern(regexp="^\\d{10}$") private String alternativeContactNumber;
    @NotBlank private String nationality;

    // Address
    @NotBlank private String country;
    private String state;
    private String district;
    private String block;
    private String village;
    @Pattern(regexp="^\\d{6}$") private String pincode;

    // Professional Info
    private String education;
    private String experience;

    // Current Crop
    private String cropPhoto;
    private String currentSurveyNumber;
    private Double currentLandHolding;
    private String currentGeoTag;
    private String currentCrop;
    private Double currentNetIncome;
    private Boolean currentSoilTest;

    // Proposed Crop
    private String proposedSurveyNumber;
    private Double proposedLandHolding;
    private String proposedGeoTag;
    private String proposedCrop;
    private Double proposedNetIncome;
    private Boolean proposedSoilTest;
    private String proposedSoilTestCertificate;

    // Irrigation
    private String currentWaterSource;
    private String currentDischargeLPH;
    private String currentSummerDischarge;
    private String currentBorewellLocation;
    private String proposedWaterSource;
    private String proposedDischargeLPH;
    private String proposedSummerDischarge;
    private String proposedBorewellLocation;

    // Bank Details
    private String bankName;
    private String accountNumber;
    private String branchName;
    private String ifscCode;

    // Document
    private String documentType;
    private String documentNumber;

    // Portal Info
    private String portalRole;
    private String portalAccess;

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
