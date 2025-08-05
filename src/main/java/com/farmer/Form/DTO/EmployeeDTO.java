package com.farmer.Form.DTO;
 
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonFormat;
 
import java.time.LocalDate;
 
@Data
public class EmployeeDTO {
 
    // Personal Information
    @NotBlank(message = "Salutation is required")
    private String salutation;
 
    @NotBlank(message = "First name is required")
    private String firstName;
 
    private String middleName;
 
    @NotBlank(message = "Last name is required")
    private String lastName;
 
    @NotBlank(message = "Gender is required")
    private String gender;
 
    @NotBlank(message = "Nationality is required")
    private String nationality;
 
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
 
    // Contact Details
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String contactNumber;
 
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;
 
    private String relationType;
 
    private String relationName;
 
    // Make altNumber completely optional (no validation)
    private String altNumber;
 
    private String altNumberType;
 
    // Address
    @NotBlank(message = "Country is required")
    private String country;
 
    @NotBlank(message = "State is required")
    private String state;
 
    @NotBlank(message = "District is required")
    private String district;
 
    private String block;
    private String village;
 
    @Pattern(regexp = "^\\d{6}$", message = "Zipcode must be a 6-digit number")
    private String zipcode;
 
    private String sector;
 
    // Professional Details - Make these optional for now
    private String education;
 
    private String experience;
 
    // Bank Details - Make these optional for now
    private String bankName;
 
    private String accountNumber;
 
    private String branchName;
 
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Enter a valid IFSC code")
    private String ifscCode;
 
    // Document Info - Make these optional for now
    private String documentType;
 
    private String documentNumber;
 
    // Portal Access
    @NotBlank(message = "Role is required")
    private String role;
 
    @NotBlank(message = "Access status is required")
    private String accessStatus;
 
    // File Uploads (for @ModelAttribute)
    private MultipartFile photo;
    private MultipartFile passbook;
    private MultipartFile documentFile;
}
 
 