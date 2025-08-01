package com.farmer.Form.Controller;

import com.farmer.Form.Entity.User;
import com.farmer.Form.Entity.Farmer;
import com.farmer.Form.Entity.Employee;
import com.farmer.Form.DTO.DashboardStatsDTO;
import com.farmer.Form.DTO.EmployeeStatsDTO;
import com.farmer.Form.DTO.TodoItemsDTO;
import com.farmer.Form.Service.UserService;
import com.farmer.Form.Service.FarmerService;
import com.farmer.Form.Service.EmployeeService;
import com.farmer.Form.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.farmer.Form.Entity.Role;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserService userService;
    private final FarmerService farmerService;
    private final EmployeeService employeeService;
    private final DashboardService dashboardService;

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Super Admin Controller is working!");
    }

    // Simple test endpoint for dashboard stats
    @GetMapping("/dashboard/stats-simple")
    public ResponseEntity<Map<String, Object>> getDashboardStatsSimple() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFarmers", 0);
        stats.put("totalEmployees", 0);
        stats.put("pendingUsers", 0);
        stats.put("approvedUsers", 0);
        stats.put("totalFPO", 0);
        return ResponseEntity.ok(stats);
    }

    // --- USER CRUD ---
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersRaw());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserRawById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUserBySuperAdmin(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUserBySuperAdmin(id, user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUserBySuperAdmin(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting user: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/bulk")
    public ResponseEntity<String> deleteUsersBulk(@RequestBody List<Long> userIds) {
        try {
            for (Long id : userIds) {
                userService.deleteUserBySuperAdmin(id);
            }
            return ResponseEntity.ok(userIds.size() + " users deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting users: " + e.getMessage());
        }
    }

    // --- REGISTRATION LIST ENDPOINTS ---
    @GetMapping("/registration-list")
    public ResponseEntity<List<User>> getRegistrationList() {
        return ResponseEntity.ok(userService.getAllUsersRaw());
    }

    @GetMapping("/registration-list/filter")
    public ResponseEntity<List<User>> getRegistrationListByStatus(@RequestParam(required = false) String status) {
        if (status == null || status.isEmpty() || status.equalsIgnoreCase("ALL")) {
            return ResponseEntity.ok(userService.getAllUsersRaw());
        } else if (status.equalsIgnoreCase("PENDING")) {
            return ResponseEntity.ok(userService.getPendingUsersRaw());
        } else if (status.equalsIgnoreCase("APPROVED")) {
            return ResponseEntity.ok(userService.getApprovedUsersRaw());
        } else if (status.equalsIgnoreCase("REJECTED")) {
            return ResponseEntity.ok(userService.getRejectedUsersRaw());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/registration-list/search")
    public ResponseEntity<List<User>> searchRegistrations(@RequestParam String query) {
        // This would need to be implemented in UserService
        // For now, return all users and let frontend filter
        return ResponseEntity.ok(userService.getAllUsersRaw());
    }

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

    // --- DASHBOARD ENDPOINTS ---
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getSuperAdminDashboardStats());
    }
    
    @GetMapping("/dashboard/employee-stats")
    public ResponseEntity<List<EmployeeStatsDTO>> getEmployeeStats() {
        return ResponseEntity.ok(dashboardService.getEmployeeStats());
    }
    
    @GetMapping("/dashboard/todo")
    public ResponseEntity<TodoItemsDTO> getTodoItems() {
        return ResponseEntity.ok(dashboardService.getTodoItems());
    }

    @GetMapping("/pending-registrations")
    public ResponseEntity<List<User>> getPendingRegistrations() {
        return ResponseEntity.ok(userService.getPendingUsersRaw());
    }

    @GetMapping("/approved-users")
    public ResponseEntity<List<User>> getApprovedUsers() {
        return ResponseEntity.ok(userService.getApprovedUsersRaw());
    }

    // ✅ Get users by role for dashboard
    @GetMapping("/users/by-role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            List<User> users = userService.getUsersByRoleRaw(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Get pending users by role
    @GetMapping("/pending-users/by-role/{role}")
    public ResponseEntity<List<User>> getPendingUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            List<User> users = userService.getPendingUsersByRoleRaw(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Assign farmers to employees (for super admin dashboard)
    @PostMapping("/dashboard/assign-farmers")
    public ResponseEntity<String> assignFarmersToEmployee(@RequestBody Map<String, Object> request) {
        try {
            List<Long> farmerIds = (List<Long>) request.get("farmerIds");
            Long employeeId = Long.valueOf(request.get("employeeId").toString());
            
            for (Long farmerId : farmerIds) {
                farmerService.assignFarmerToEmployee(farmerId, employeeId);
            }
            
            return ResponseEntity.ok(farmerIds.size() + " farmers assigned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error assigning farmers: " + e.getMessage());
        }
    }
} 