package com.farmer.Form.Controller;

import com.farmer.Form.Entity.Farmer;
import com.farmer.Form.Entity.Employee;
import com.farmer.Form.Service.FarmerService;
import com.farmer.Form.Service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FarmerService farmerService;
    private final EmployeeService employeeService;

    // --- FARMER CRUD ---
    @GetMapping("/farmers")
    public ResponseEntity<List<Farmer>> getAllFarmers() {
        return ResponseEntity.ok(farmerService.getAllFarmersRaw());
    }

    @GetMapping("/farmers/{id}")
    public ResponseEntity<Farmer> getFarmerById(@PathVariable Long id) {
        return ResponseEntity.ok(farmerService.getFarmerRawById(id));
    }

    @PostMapping("/farmers")
    public ResponseEntity<Farmer> createFarmer(@RequestBody Farmer farmer) {
        return ResponseEntity.ok(farmerService.createFarmerBySuperAdmin(farmer));
    }

    @PutMapping("/farmers/{id}")
    public ResponseEntity<Farmer> updateFarmer(@PathVariable Long id, @RequestBody Farmer farmer) {
        return ResponseEntity.ok(farmerService.updateFarmerBySuperAdmin(id, farmer));
    }

    @DeleteMapping("/farmers/{id}")
    public ResponseEntity<String> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmerBySuperAdmin(id);
        return ResponseEntity.ok("Farmer deleted successfully");
    }

    // --- EMPLOYEE CRUD ---
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployeesRaw());
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeRawById(id));
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.createEmployeeBySuperAdmin(employee));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.updateEmployeeBySuperAdmin(id, employee));
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployeeBySuperAdmin(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }

    // --- ASSIGN FARMER TO EMPLOYEE ---
    @PostMapping("/assign-farmer")
    public ResponseEntity<String> assignFarmerToEmployee(@RequestParam Long farmerId, @RequestParam Long employeeId) {
        farmerService.assignFarmerToEmployee(farmerId, employeeId);
        return ResponseEntity.ok("Farmer assigned to employee successfully");
    }

    // --- ENHANCED DASHBOARD ENDPOINTS ---
    
    // Get all farmers with KYC status and assignment info
    @GetMapping("/farmers-with-kyc")
    public ResponseEntity<List<Map<String, Object>>> getFarmersWithKycStatus() {
        List<Farmer> farmers = farmerService.getAllFarmersRaw();
        List<Map<String, Object>> farmersWithKyc = farmers.stream().map(farmer -> {
            Map<String, Object> farmerData = new HashMap<>();
            farmerData.put("id", farmer.getId());
            farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
            farmerData.put("contactNumber", farmer.getContactNumber());
            farmerData.put("state", farmer.getState());
            farmerData.put("district", farmer.getDistrict());
            farmerData.put("kycStatus", farmer.getKycApproved() != null ? 
                (farmer.getKycApproved() ? "APPROVED" : "PENDING") : "NOT_STARTED");
            farmerData.put("assignedEmployee", farmer.getAssignedEmployee() != null ? 
                farmer.getAssignedEmployee().getFirstName() + " " + farmer.getAssignedEmployee().getLastName() : "Not Assigned");
            farmerData.put("assignedEmployeeId", farmer.getAssignedEmployee() != null ? 
                farmer.getAssignedEmployee().getId() : null);
            return farmerData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(farmersWithKyc);
    }

    // Get all employees with assignment statistics
    @GetMapping("/employees-with-stats")
    public ResponseEntity<List<Map<String, Object>>> getEmployeesWithStats() {
        List<Employee> employees = employeeService.getAllEmployeesRaw();
        List<Map<String, Object>> employeesWithStats = employees.stream().map(employee -> {
            Map<String, Object> employeeData = new HashMap<>();
            employeeData.put("id", employee.getId());
            employeeData.put("name", employee.getFirstName() + " " + employee.getLastName());
            employeeData.put("email", employee.getEmail());
            employeeData.put("contactNumber", employee.getContactNumber());
            employeeData.put("state", employee.getState());
            employeeData.put("district", employee.getDistrict());
            
            // Get assigned farmers for this employee
            List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employee.getEmail());
            long approvedCount = assignedFarmers.stream().filter(f -> f.getKycApproved() != null && f.getKycApproved()).count();
            long pendingCount = assignedFarmers.stream().filter(f -> f.getKycApproved() == null || !f.getKycApproved()).count();
            
            employeeData.put("totalAssigned", assignedFarmers.size());
            employeeData.put("approvedKyc", approvedCount);
            employeeData.put("pendingKyc", pendingCount);
            
            return employeeData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(employeesWithStats);
    }

    // Get farmers assigned to specific employee
    @GetMapping("/employees/{employeeId}/assigned-farmers")
    public ResponseEntity<List<Map<String, Object>>> getFarmersByEmployee(@PathVariable Long employeeId) {
        Employee employee = employeeService.getEmployeeRawById(employeeId);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employee.getEmail());
        List<Map<String, Object>> farmersData = assignedFarmers.stream().map(farmer -> {
            Map<String, Object> farmerData = new HashMap<>();
            farmerData.put("id", farmer.getId());
            farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
            farmerData.put("contactNumber", farmer.getContactNumber());
            farmerData.put("state", farmer.getState());
            farmerData.put("district", farmer.getDistrict());
            farmerData.put("kycStatus", farmer.getKycApproved() != null ? 
                (farmer.getKycApproved() ? "APPROVED" : "PENDING") : "NOT_STARTED");
            return farmerData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(farmersData);
    }

    // Get to-do list for admin
    @GetMapping("/todo-list")
    public ResponseEntity<Map<String, Object>> getTodoList() {
        List<Farmer> allFarmers = farmerService.getAllFarmersRaw();
        List<Employee> allEmployees = employeeService.getAllEmployeesRaw();
        
        // Farmers needing assignment
        List<Map<String, Object>> unassignedFarmers = allFarmers.stream()
            .filter(farmer -> farmer.getAssignedEmployee() == null)
            .map(farmer -> {
                Map<String, Object> farmerData = new HashMap<>();
                farmerData.put("id", farmer.getId());
                farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
                farmerData.put("contactNumber", farmer.getContactNumber());
                farmerData.put("state", farmer.getState());
                farmerData.put("district", farmer.getDistrict());
                return farmerData;
            }).collect(Collectors.toList());
        
        // Employees with pending KYC tasks
        List<Map<String, Object>> employeesWithPendingTasks = allEmployees.stream()
            .map(employee -> {
                List<Farmer> assignedFarmers = farmerService.getFarmersByEmployeeEmail(employee.getEmail());
                long pendingCount = assignedFarmers.stream()
                    .filter(f -> f.getKycApproved() == null || !f.getKycApproved()).count();
                
                if (pendingCount > 0) {
                    Map<String, Object> employeeData = new HashMap<>();
                    employeeData.put("id", employee.getId());
                    employeeData.put("name", employee.getFirstName() + " " + employee.getLastName());
                    employeeData.put("email", employee.getEmail());
                    employeeData.put("pendingKycCount", pendingCount);
                    return employeeData;
                }
                return null;
            }).filter(employee -> employee != null).collect(Collectors.toList());
        
        Map<String, Object> todoList = new HashMap<>();
        todoList.put("unassignedFarmers", unassignedFarmers);
        todoList.put("employeesWithPendingTasks", employeesWithPendingTasks);
        todoList.put("totalUnassigned", unassignedFarmers.size());
        todoList.put("totalPendingTasks", employeesWithPendingTasks.size());
        
        return ResponseEntity.ok(todoList);
    }

    // Get dashboard statistics
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        List<Farmer> allFarmers = farmerService.getAllFarmersRaw();
        List<Employee> allEmployees = employeeService.getAllEmployeesRaw();
        
        long totalFarmers = allFarmers.size();
        long totalEmployees = allEmployees.size();
        long unassignedFarmers = allFarmers.stream().filter(f -> f.getAssignedEmployee() == null).count();
        long approvedKyc = allFarmers.stream().filter(f -> f.getKycApproved() != null && f.getKycApproved()).count();
        long pendingKyc = allFarmers.stream().filter(f -> f.getKycApproved() == null || !f.getKycApproved()).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFarmers", totalFarmers);
        stats.put("totalEmployees", totalEmployees);
        stats.put("unassignedFarmers", unassignedFarmers);
        stats.put("approvedKyc", approvedKyc);
        stats.put("pendingKyc", pendingKyc);
        stats.put("kycCompletionRate", totalFarmers > 0 ? (double) approvedKyc / totalFarmers * 100 : 0);
        
        return ResponseEntity.ok(stats);
    }

    // Get all users (for admin dashboard)
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        // This endpoint is needed for admin dashboard to show user registrations
        // For now, return empty list as admin shouldn't manage users directly
        return ResponseEntity.ok(new ArrayList<>());
    }

    // Filter farmers by state/district
    @GetMapping("/farmers/filter")
    public ResponseEntity<List<Map<String, Object>>> filterFarmers(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district) {
        
        List<Farmer> allFarmers = farmerService.getAllFarmersRaw();
        List<Farmer> filteredFarmers = allFarmers.stream()
            .filter(farmer -> 
                (state == null || state.isEmpty() || farmer.getState() != null && farmer.getState().equalsIgnoreCase(state)) &&
                (district == null || district.isEmpty() || farmer.getDistrict() != null && farmer.getDistrict().equalsIgnoreCase(district))
            ).collect(Collectors.toList());
        
        List<Map<String, Object>> farmersData = filteredFarmers.stream().map(farmer -> {
            Map<String, Object> farmerData = new HashMap<>();
            farmerData.put("id", farmer.getId());
            farmerData.put("name", farmer.getFirstName() + " " + farmer.getLastName());
            farmerData.put("contactNumber", farmer.getContactNumber());
            farmerData.put("state", farmer.getState());
            farmerData.put("district", farmer.getDistrict());
            farmerData.put("kycStatus", farmer.getKycApproved() != null ? 
                (farmer.getKycApproved() ? "APPROVED" : "PENDING") : "NOT_STARTED");
            farmerData.put("assignedEmployee", farmer.getAssignedEmployee() != null ? 
                farmer.getAssignedEmployee().getFirstName() + " " + farmer.getAssignedEmployee().getLastName() : "Not Assigned");
            return farmerData;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(farmersData);
    }

    // Get unique states and districts for filter dropdowns
    @GetMapping("/locations")
    public ResponseEntity<Map<String, List<String>>> getLocations() {
        List<Farmer> allFarmers = farmerService.getAllFarmersRaw();
        
        List<String> states = allFarmers.stream()
            .map(Farmer::getState)
            .filter(state -> state != null && !state.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        List<String> districts = allFarmers.stream()
            .map(Farmer::getDistrict)
            .filter(district -> district != null && !district.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        Map<String, List<String>> locations = new HashMap<>();
        locations.put("states", states);
        locations.put("districts", districts);
        
        return ResponseEntity.ok(locations);
    }
} 