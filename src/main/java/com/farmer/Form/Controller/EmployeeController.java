package com.farmer.Form.Controller;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
 
import com.farmer.Form.DTO.EmployeeDTO;
import com.farmer.Form.DTO.PincodeApiResponse.PostOffice;
import com.farmer.Form.Entity.Employee;
import com.farmer.Form.Mapper.EmployeeMapper;
import com.farmer.Form.Service.AddressService;
import com.farmer.Form.Service.EmployeeService;
import com.farmer.Form.Service.FileStorageService;
import com.farmer.Form.Entity.Farmer;
import com.farmer.Form.Service.FarmerService;
 
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
 
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
 
    private final EmployeeService employeeService;
    private final AddressService addressService;
    private final FileStorageService fileStorageService;
    private final FarmerService farmerService;
 
    // Debug endpoint to check form data
    @PostMapping("/debug")
    public ResponseEntity<?> debugEmployeeData(@ModelAttribute EmployeeDTO dto) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("message", "Form data received");
        debug.put("salutation", dto.getSalutation());
        debug.put("firstName", dto.getFirstName());
        debug.put("lastName", dto.getLastName());
        debug.put("email", dto.getEmail());
        debug.put("contactNumber", dto.getContactNumber());
        debug.put("altNumber", dto.getAltNumber());
        debug.put("altNumberLength", dto.getAltNumber() != null ? dto.getAltNumber().length() : 0);
        debug.put("altNumberTrimmed", dto.getAltNumber() != null ? dto.getAltNumber().trim() : null);
        debug.put("dob", dto.getDob());
        debug.put("role", dto.getRole());
        debug.put("accessStatus", dto.getAccessStatus());
        debug.put("education", dto.getEducation());
        debug.put("experience", dto.getExperience());
        debug.put("bankName", dto.getBankName());
        debug.put("accountNumber", dto.getAccountNumber());
        debug.put("branchName", dto.getBranchName());
        debug.put("documentType", dto.getDocumentType());
        debug.put("documentNumber", dto.getDocumentNumber());
        debug.put("photoFile", dto.getPhoto() != null ? dto.getPhoto().getOriginalFilename() : "null");
        debug.put("passbookFile", dto.getPassbook() != null ? dto.getPassbook().getOriginalFilename() : "null");
        debug.put("documentFile", dto.getDocumentFile() != null ? dto.getDocumentFile().getOriginalFilename() : "null");
        
        // Print to console for debugging
        System.out.println("=== Debug Endpoint Called ===");
        System.out.println("AltNumber: '" + dto.getAltNumber() + "'");
        System.out.println("AltNumber length: " + (dto.getAltNumber() != null ? dto.getAltNumber().length() : 0));
        System.out.println("AltNumber trimmed: '" + (dto.getAltNumber() != null ? dto.getAltNumber().trim() : null) + "'");
        System.out.println("=============================");
        
        return ResponseEntity.ok(debug);
    }
 
    // ✅ Create employee with file upload
    @PostMapping
    public ResponseEntity<?> createEmployee(@ModelAttribute @Valid EmployeeDTO dto, BindingResult bindingResult) {
        try {
            // Debug logging
            System.out.println("=== Employee Creation Debug ===");
            System.out.println("Salutation: " + dto.getSalutation());
            System.out.println("FirstName: " + dto.getFirstName());
            System.out.println("LastName: " + dto.getLastName());
            System.out.println("Email: " + dto.getEmail());
            System.out.println("ContactNumber: " + dto.getContactNumber());
            System.out.println("AltNumber: '" + dto.getAltNumber() + "'");
            System.out.println("Role: " + dto.getRole());
            System.out.println("AccessStatus: " + dto.getAccessStatus());
            System.out.println("DOB: " + dto.getDob());
            System.out.println("================================");
            
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                System.out.println("=== Validation Errors ===");
                bindingResult.getFieldErrors().forEach(error -> {
                    System.out.println(error.getField() + ": " + error.getDefaultMessage());
                });
                System.out.println("========================");
                
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Validation Error");
                error.put("message", "Please check the form data and try again");
                error.put("details", bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .toList());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Handle file uploads
            String photoFile = fileStorageService.storeFile(dto.getPhoto(), "photos");
            String passbookFile = fileStorageService.storeFile(dto.getPassbook(), "passbooks");
            String docFile = fileStorageService.storeFile(dto.getDocumentFile(), "documents");
 
            Employee employee = EmployeeMapper.toEntity(dto, photoFile, passbookFile, docFile);
            Employee saved = employeeService.saveEmployee(employee);
            
            // Format photo URLs for response
            formatPhotoUrls(saved);
            
            // Return the exact format expected by frontend
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
 
        } catch (IOException | MultipartException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File upload failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create employee");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
 
    // ✅ Get all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            // Format photo URLs for all employees
            employees.forEach(this::formatPhotoUrls);
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
 
    // ✅ Get one employee by ID with proper photo URL formatting
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        try {
            System.out.println("🔍 Fetching employee with ID: " + id);
            
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                System.out.println("❌ Employee not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("✅ Employee found: " + employee.getFirstName() + " " + employee.getLastName());
            System.out.println("📸 Photo fields before formatting:");
            System.out.println("  - photoFileName: " + employee.getPhotoFileName());
            System.out.println("  - photoUrl: " + employee.getPhotoUrl());
            
            // Format photo URLs for response
            formatPhotoUrls(employee);
            
            System.out.println("📸 Photo fields after formatting:");
            System.out.println("  - photoFileName: " + employee.getPhotoFileName());
            System.out.println("  - photoUrl: " + employee.getPhotoUrl());
            
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            System.out.println("❌ Error fetching employee with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
 
    // ✅ Update employee with file support
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @ModelAttribute EmployeeDTO dto) {
        try {
            Employee existing = employeeService.getEmployeeById(id);
            if (existing == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Employee not found");
                error.put("message", "Employee with ID " + id + " does not exist");
                return ResponseEntity.notFound().build();
            }
 
            // Handle file uploads - only update if new files are provided
            String photoFile = dto.getPhoto() != null && !dto.getPhoto().isEmpty() 
                ? fileStorageService.storeFile(dto.getPhoto(), "photos") 
                : existing.getPhotoFileName();
            String passbookFile = dto.getPassbook() != null && !dto.getPassbook().isEmpty() 
                ? fileStorageService.storeFile(dto.getPassbook(), "passbooks") 
                : existing.getPassbookFileName();
            String docFile = dto.getDocumentFile() != null && !dto.getDocumentFile().isEmpty() 
                ? fileStorageService.storeFile(dto.getDocumentFile(), "documents") 
                : existing.getDocumentFileName();
 
            Employee updated = EmployeeMapper.toEntity(dto, photoFile, passbookFile, docFile);
            updated.setId(id);
            Employee saved = employeeService.saveEmployee(updated);
            
            // Format photo URLs for response
            formatPhotoUrls(saved);
 
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Employee updated successfully");
            response.put("employee", saved);
            
            return ResponseEntity.ok(response);
 
        } catch (IOException | MultipartException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File update failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update employee");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
 
    // ✅ Delete employee
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            Employee existing = employeeService.getEmployeeById(id);
            if (existing == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Employee not found");
                error.put("message", "Employee with ID " + id + " does not exist");
                return ResponseEntity.notFound().build();
            }
            
            employeeService.deleteEmployee(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Employee deleted successfully");
            response.put("id", id.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete employee");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
 
    // ✅ Get address by pincode (tested endpoint)
    @GetMapping("/address-by-pincode/{pincode}")
    public ResponseEntity<PostOffice> getAddressByPincode(@PathVariable String pincode) {
        try {
            System.out.println("📍 Pincode requested: " + pincode); // Debug log
            PostOffice postOffice = addressService.fetchAddressByPincode(pincode);
            return postOffice != null ? ResponseEntity.ok(postOffice) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // View all farmers assigned to the logged-in employee
    @GetMapping("/assigned-farmers")
    public ResponseEntity<List<Farmer>> getAssignedFarmers(Authentication authentication) {
        try {
            String employeeEmail = authentication.getName();
            List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employeeEmail);
            return ResponseEntity.ok(assignedFarmers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Approve KYC for a farmer
    @PutMapping("/approve-kyc/{farmerId}")
    public ResponseEntity<?> approveKyc(@PathVariable Long farmerId) {
        try {
            farmerService.approveKyc(farmerId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "KYC approved for farmer");
            response.put("farmerId", farmerId.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to approve KYC");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Helper method to format photo URLs
    private void formatPhotoUrls(Employee employee) {
        // Only set photoFileName to the file name (not a path or URL)
        if (employee.getPhotoFileName() != null) {
            String fileName = employee.getPhotoFileName();
            // Remove any leading slashes or paths
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            employee.setPhotoFileName(fileName);
            employee.setPhotoUrl("/uploads/photos/" + fileName);
        } else {
            employee.setPhotoUrl(null);
        }
        // Passbook
        if (employee.getPassbookFileName() != null) {
            String fileName = employee.getPassbookFileName();
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            employee.setPassbookFileName(fileName);
        }
        // Document
        if (employee.getDocumentFileName() != null) {
            String fileName = employee.getDocumentFileName();
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            employee.setDocumentFileName(fileName);
        }
    }
}
 
 