package com.farmer.Form.Controller;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.security.core.Authentication;
 
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
 
    // ✅ Create employee with file upload
    @PostMapping
    public ResponseEntity<?> createEmployee(@ModelAttribute @Valid EmployeeDTO dto) {
        try {
            String photoFile = fileStorageService.storeFile(dto.getPhoto(), "photos");
            String passbookFile = fileStorageService.storeFile(dto.getPassbook(), "passbooks");
            String docFile = fileStorageService.storeFile(dto.getDocumentFile(), "documents");
 
            Employee employee = EmployeeMapper.toEntity(dto);
            employee.setPhotoFileName(photoFile);
            employee.setPassbookFileName(passbookFile);
            employee.setDocumentFileName(docFile);
            
            Employee saved = employeeService.saveEmployee(employee);
            return ResponseEntity.ok(saved);
 
        } catch (IOException | MultipartException e) {
            return ResponseEntity.badRequest().body("❌ File upload failed: " + e.getMessage());
        }
    }
 
    // ✅ Get all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }
 
    // ✅ Get one employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return employee != null ? ResponseEntity.ok(employee) : ResponseEntity.notFound().build();
    }
 
    // ✅ Update employee with file support
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @ModelAttribute EmployeeDTO dto) {
        try {
            Employee existing = employeeService.getEmployeeById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }
 
            String photoFile = dto.getPhoto() != null ? fileStorageService.storeFile(dto.getPhoto(), "photos") : existing.getPhotoFileName();
            String passbookFile = dto.getPassbook() != null ? fileStorageService.storeFile(dto.getPassbook(), "passbooks") : existing.getPassbookFileName();
            String docFile = dto.getDocumentFile() != null ? fileStorageService.storeFile(dto.getDocumentFile(), "documents") : existing.getDocumentFileName();
 
            Employee updated = EmployeeMapper.toEntity(dto);
            updated.setId(id);
            updated.setPhotoFileName(photoFile);
            updated.setPassbookFileName(passbookFile);
            updated.setDocumentFileName(docFile);
            
            Employee saved = employeeService.saveEmployee(updated);
 
            return ResponseEntity.ok(saved);
 
        } catch (IOException | MultipartException e) {
            return ResponseEntity.badRequest().body("❌ File update failed: " + e.getMessage());
        }
    }
 
    // ✅ Delete employee
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("✅ Employee deleted successfully");
    }
 
    // ✅ Get address by pincode (tested endpoint)
    @GetMapping("/address-by-pincode/{pincode}")
    public ResponseEntity<PostOffice> getAddressByPincode(@PathVariable String pincode) {
        System.out.println("📍 Pincode requested: " + pincode); // Debug log
        PostOffice postOffice = addressService.fetchAddressByPincode(pincode);
        return postOffice != null ? ResponseEntity.ok(postOffice) : ResponseEntity.notFound().build();
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
}
 
 