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
import com.farmer.Form.Entity.Farmer;
import com.farmer.Form.Mapper.EmployeeMapper;
import com.farmer.Form.Service.AddressService;
import com.farmer.Form.Service.EmployeeService;
import com.farmer.Form.Service.FileStorageService;
import com.farmer.Form.Service.FarmerService;
 
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
 
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

    // ✅ Get all employees with formatted data for frontend
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesList() {
        List<Employee> employees = employeeService.getAllEmployees();
        
        List<Map<String, Object>> employeeList = employees.stream().map(employee -> {
            Map<String, Object> employeeData = new HashMap<>();
            employeeData.put("id", employee.getId());
            employeeData.put("employeeId", "EMP" + String.format("%06d", employee.getId())); // Format: EMP000001
            employeeData.put("name", employee.getFirstName() + " " + employee.getLastName());
            employeeData.put("designation", employee.getRole() != null ? employee.getRole() : "employee");
            employeeData.put("district", employee.getDistrict());
            employeeData.put("contactNumber", employee.getContactNumber());
            employeeData.put("email", employee.getEmail());
            employeeData.put("status", employee.getAccessStatus() != null ? employee.getAccessStatus() : "ACTIVE");
            return employeeData;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(employeeList);
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

    // --- EMPLOYEE DASHBOARD ENDPOINTS ---

    // Get assigned farmers with KYC status for logged-in employee
    @GetMapping("/dashboard/assigned-farmers")
    public ResponseEntity<List<Map<String, Object>>> getAssignedFarmersWithKyc(Authentication authentication) {
        String employeeEmail = authentication.getName();
        List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employeeEmail);
        
        List<Map<String, Object>> farmersWithKyc = assignedFarmers.stream().map(farmer -> {
            Map<String, Object> farmerData = new HashMap<>();
            farmerData.put("id", farmer.getId());
            farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
            farmerData.put("contactNumber", farmer.getContactNumber());
            farmerData.put("state", farmer.getState());
            farmerData.put("district", farmer.getDistrict());
            farmerData.put("village", farmer.getVillage());
            farmerData.put("kycStatus", farmer.getKycStatus() != null ? farmer.getKycStatus().name() : "PENDING");
            farmerData.put("kycSubmittedDate", farmer.getKycSubmittedDate());
            farmerData.put("kycReviewedDate", farmer.getKycReviewedDate());
            farmerData.put("kycRejectionReason", farmer.getKycRejectionReason());
            farmerData.put("kycReferBackReason", farmer.getKycReferBackReason());
            return farmerData;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(farmersWithKyc);
    }

    // Get dashboard statistics for logged-in employee
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        String employeeEmail = authentication.getName();
        List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employeeEmail);
        
        long totalAssigned = assignedFarmers.size();
        long approved = assignedFarmers.stream()
            .filter(f -> f.getKycStatus() != null && f.getKycStatus() == Farmer.KycStatus.APPROVED)
            .count();
        long referBack = assignedFarmers.stream()
            .filter(f -> f.getKycStatus() != null && f.getKycStatus() == Farmer.KycStatus.REFER_BACK)
            .count();
        long pending = assignedFarmers.stream()
            .filter(f -> f.getKycStatus() == null || f.getKycStatus() == Farmer.KycStatus.PENDING)
            .count();
        long rejected = assignedFarmers.stream()
            .filter(f -> f.getKycStatus() != null && f.getKycStatus() == Farmer.KycStatus.REJECTED)
            .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAssigned", totalAssigned);
        stats.put("approved", approved);
        stats.put("referBack", referBack);
        stats.put("pending", pending);
        stats.put("rejected", rejected);
        stats.put("completionRate", totalAssigned > 0 ? (double) approved / totalAssigned * 100 : 0);
        
        return ResponseEntity.ok(stats);
    }

    // Get to-do list for logged-in employee
    @GetMapping("/dashboard/todo-list")
    public ResponseEntity<Map<String, Object>> getTodoList(Authentication authentication) {
        String employeeEmail = authentication.getName();
        List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employeeEmail);
        
        // Pending KYC verifications
        List<Map<String, Object>> pendingKyc = assignedFarmers.stream()
            .filter(f -> f.getKycStatus() == null || f.getKycStatus() == Farmer.KycStatus.PENDING)
            .map(farmer -> {
                Map<String, Object> farmerData = new HashMap<>();
                farmerData.put("id", farmer.getId());
                farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
                farmerData.put("contactNumber", farmer.getContactNumber());
                farmerData.put("state", farmer.getState());
                farmerData.put("district", farmer.getDistrict());
                farmerData.put("kycSubmittedDate", farmer.getKycSubmittedDate());
                return farmerData;
            }).collect(Collectors.toList());
        
        // Refer-back cases to recheck
        List<Map<String, Object>> referBackCases = assignedFarmers.stream()
            .filter(f -> f.getKycStatus() != null && f.getKycStatus() == Farmer.KycStatus.REFER_BACK)
            .map(farmer -> {
                Map<String, Object> farmerData = new HashMap<>();
                farmerData.put("id", farmer.getId());
                farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
                farmerData.put("contactNumber", farmer.getContactNumber());
                farmerData.put("state", farmer.getState());
                farmerData.put("district", farmer.getDistrict());
                farmerData.put("kycReferBackReason", farmer.getKycReferBackReason());
                farmerData.put("kycReviewedDate", farmer.getKycReviewedDate());
                return farmerData;
            }).collect(Collectors.toList());
        
        // New assigned farmers not yet opened
        List<Map<String, Object>> newAssignments = assignedFarmers.stream()
            .filter(f -> f.getKycStatus() == null)
            .map(farmer -> {
                Map<String, Object> farmerData = new HashMap<>();
                farmerData.put("id", farmer.getId());
                farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
                farmerData.put("contactNumber", farmer.getContactNumber());
                farmerData.put("state", farmer.getState());
                farmerData.put("district", farmer.getDistrict());
                return farmerData;
            }).collect(Collectors.toList());
        
        Map<String, Object> todoList = new HashMap<>();
        todoList.put("pendingKyc", pendingKyc);
        todoList.put("referBackCases", referBackCases);
        todoList.put("newAssignments", newAssignments);
        todoList.put("totalPendingKyc", pendingKyc.size());
        todoList.put("totalReferBack", referBackCases.size());
        todoList.put("totalNewAssignments", newAssignments.size());
        
        return ResponseEntity.ok(todoList);
    }

    // KYC Actions - Approve KYC
    @PutMapping("/kyc/approve/{farmerId}")
    public ResponseEntity<String> approveKyc(@PathVariable Long farmerId, Authentication authentication) {
        try {
            String employeeEmail = authentication.getName();
            farmerService.approveKycByEmployee(farmerId, employeeEmail);
            return ResponseEntity.ok("KYC approved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error approving KYC: " + e.getMessage());
        }
    }

    // KYC Actions - Refer Back with reason
    @PutMapping("/kyc/refer-back/{farmerId}")
    public ResponseEntity<String> referBackKyc(@PathVariable Long farmerId, 
                                             @RequestBody Map<String, String> request,
                                             Authentication authentication) {
        try {
            String employeeEmail = authentication.getName();
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Reason is required for refer back");
            }
            farmerService.referBackKyc(farmerId, employeeEmail, reason);
            return ResponseEntity.ok("KYC referred back successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error referring back KYC: " + e.getMessage());
        }
    }

    // KYC Actions - Reject with reason
    @PutMapping("/kyc/reject/{farmerId}")
    public ResponseEntity<String> rejectKyc(@PathVariable Long farmerId, 
                                          @RequestBody Map<String, String> request,
                                          Authentication authentication) {
        try {
            String employeeEmail = authentication.getName();
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Reason is required for rejection");
            }
            farmerService.rejectKyc(farmerId, employeeEmail, reason);
            return ResponseEntity.ok("KYC rejected successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error rejecting KYC: " + e.getMessage());
        }
    }

    // Filter assigned farmers by KYC status
    @GetMapping("/dashboard/farmers/filter")
    public ResponseEntity<List<Map<String, Object>>> filterAssignedFarmers(
            @RequestParam(required = false) String kycStatus,
            Authentication authentication) {
        
        String employeeEmail = authentication.getName();
        List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employeeEmail);
        
        List<Farmer> filteredFarmers = assignedFarmers.stream()
            .filter(farmer -> {
                if (kycStatus == null || kycStatus.isEmpty()) return true;
                if (farmer.getKycStatus() == null) return kycStatus.equals("PENDING");
                return farmer.getKycStatus().name().equals(kycStatus);
            }).collect(Collectors.toList());
        
        List<Map<String, Object>> farmersData = filteredFarmers.stream().map(farmer -> {
            Map<String, Object> farmerData = new HashMap<>();
            farmerData.put("id", farmer.getId());
            farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
            farmerData.put("contactNumber", farmer.getContactNumber());
            farmerData.put("state", farmer.getState());
            farmerData.put("district", farmer.getDistrict());
            farmerData.put("village", farmer.getVillage());
            farmerData.put("kycStatus", farmer.getKycStatus() != null ? farmer.getKycStatus().name() : "PENDING");
            farmerData.put("kycSubmittedDate", farmer.getKycSubmittedDate());
            farmerData.put("kycReviewedDate", farmer.getKycReviewedDate());
            return farmerData;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(farmersData);
    }

    // Get employee profile
    @GetMapping("/dashboard/profile")
    public ResponseEntity<Employee> getEmployeeProfile(Authentication authentication) {
        String employeeEmail = authentication.getName();
        Employee employee = employeeService.getEmployeeByEmail(employeeEmail);
        return employee != null ? ResponseEntity.ok(employee) : ResponseEntity.notFound().build();
    }

    // ✅ View Employee Details
    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getEmployeeDetails(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> details = new HashMap<>();
            details.put("id", employee.getId());
            details.put("employeeId", "EMP" + String.format("%06d", employee.getId()));
            details.put("name", employee.getFirstName() + " " + employee.getLastName());
            details.put("designation", employee.getRole());
            details.put("email", employee.getEmail());
            details.put("contactNumber", employee.getContactNumber());
            details.put("district", employee.getDistrict());
            details.put("state", employee.getState());
            details.put("status", employee.getAccessStatus());
            details.put("education", employee.getEducation());
            details.put("experience", employee.getExperience());
            details.put("bankName", employee.getBankName());
            details.put("accountNumber", employee.getAccountNumber());
            details.put("ifscCode", employee.getIfscCode());
            details.put("photoFileName", employee.getPhotoFileName());
            details.put("passbookFileName", employee.getPassbookFileName());
            details.put("documentFileName", employee.getDocumentFileName());

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Error fetching employee details: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ✅ Give Login Access to Employee
    @PostMapping("/{id}/give-login-access")
    public ResponseEntity<Map<String, Object>> giveLoginAccess(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }

            // Generate temporary password
            String tempPassword = employeeService.generateTempPassword();
            
            // Create or update user account for employee
            boolean success = employeeService.createOrUpdateUserAccount(employee, tempPassword);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login access granted successfully");
                response.put("employeeId", employee.getId());
                response.put("employeeName", employee.getFirstName() + " " + employee.getLastName());
                response.put("email", employee.getEmail());
                response.put("tempPassword", tempPassword);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "Failed to create login access");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Error giving login access: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ✅ Assign Farmers to Employee
    @PostMapping("/{id}/assign-farmers")
    public ResponseEntity<Map<String, Object>> assignFarmersToEmployee(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> request) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }

            @SuppressWarnings("unchecked")
            List<Long> farmerIds = (List<Long>) request.get("farmerIds");
            
            if (farmerIds == null || farmerIds.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "Farmer IDs are required");
                return ResponseEntity.badRequest().body(error);
            }

            // Assign farmers to employee
            int assignedCount = employeeService.assignFarmersToEmployee(id, farmerIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Farmers assigned successfully");
            response.put("employeeId", id);
            response.put("employeeName", employee.getFirstName() + " " + employee.getLastName());
            response.put("assignedCount", assignedCount);
            response.put("totalRequested", farmerIds.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Error assigning farmers: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ✅ Get Available Farmers for Assignment
    @GetMapping("/{id}/available-farmers")
    public ResponseEntity<List<Map<String, Object>>> getAvailableFarmersForAssignment(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }

            List<Farmer> availableFarmers = employeeService.getAvailableFarmersForAssignment(id);
            
            List<Map<String, Object>> farmersList = availableFarmers.stream().map(farmer -> {
                Map<String, Object> farmerData = new HashMap<>();
                farmerData.put("id", farmer.getId());
                farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
                farmerData.put("contactNumber", farmer.getContactNumber());
                farmerData.put("district", farmer.getDistrict());
                farmerData.put("state", farmer.getState());
                farmerData.put("kycStatus", farmer.getKycStatus() != null ? farmer.getKycStatus().name() : "PENDING");
                return farmerData;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(farmersList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    // ✅ Get Assigned Farmers for Employee
    @GetMapping("/{id}/assigned-farmers")
    public ResponseEntity<List<Map<String, Object>>> getAssignedFarmersForEmployee(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }

            List<Farmer> assignedFarmers = employeeService.getAssignedFarmersForEmployee(id);
            
            List<Map<String, Object>> farmersList = assignedFarmers.stream().map(farmer -> {
                Map<String, Object> farmerData = new HashMap<>();
                farmerData.put("id", farmer.getId());
                farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
                farmerData.put("contactNumber", farmer.getContactNumber());
                farmerData.put("district", farmer.getDistrict());
                farmerData.put("state", farmer.getState());
                farmerData.put("kycStatus", farmer.getKycStatus() != null ? farmer.getKycStatus().name() : "PENDING");
                // Note: assignedDate not available in Farmer entity
                return farmerData;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(farmersList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
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
 
 