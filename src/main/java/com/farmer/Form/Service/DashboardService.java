package com.farmer.Form.Service;

import com.farmer.Form.DTO.DashboardStatsDTO;
import com.farmer.Form.DTO.EmployeeStatsDTO;
import com.farmer.Form.DTO.TodoItemsDTO;
import com.farmer.Form.DTO.AssignmentRequestDTO;
import com.farmer.Form.Entity.User;
import com.farmer.Form.Entity.Role;
import com.farmer.Form.Entity.UserStatus;
import com.farmer.Form.Entity.KycStatus;
import com.farmer.Form.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final UserRepository userRepository;
    
    // Super Admin Dashboard Stats
    public DashboardStatsDTO getSuperAdminDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalFarmers(userRepository.countByRole(Role.FARMER));
        stats.setTotalEmployees(userRepository.countByRole(Role.EMPLOYEE));
        stats.setPendingUsers(userRepository.countByStatus(UserStatus.PENDING));
        stats.setApprovedUsers(userRepository.countByStatus(UserStatus.APPROVED));
        stats.setTotalFPO(userRepository.countByRole(Role.FPO));
        stats.setPendingEmployees(userRepository.countByRoleAndStatus(Role.EMPLOYEE, UserStatus.PENDING));
        stats.setPendingFarmers(userRepository.countByRoleAndStatus(Role.FARMER, UserStatus.PENDING));
        stats.setApprovedEmployees(userRepository.countByRoleAndStatus(Role.EMPLOYEE, UserStatus.APPROVED));
        stats.setApprovedFarmers(userRepository.countByRoleAndStatus(Role.FARMER, UserStatus.APPROVED));
        return stats;
    }
    
    // Admin Dashboard Stats
    public DashboardStatsDTO getAdminDashboardStats() {
        return getSuperAdminDashboardStats(); // Same logic for now
    }
    
    // Employee Performance Stats
    public List<EmployeeStatsDTO> getEmployeeStats() {
        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        List<EmployeeStatsDTO> stats = new ArrayList<>();
        
        for (User employee : employees) {
            EmployeeStatsDTO empStats = new EmployeeStatsDTO();
            empStats.setId(employee.getId());
            empStats.setName(employee.getName());
            
            List<User> assignedFarmers = userRepository.findByAssignedEmployeeId(employee.getId());
            empStats.setTotalAssigned((long) assignedFarmers.size());
            
            empStats.setApproved(assignedFarmers.stream()
                .filter(f -> KycStatus.APPROVED.equals(f.getKycStatus()))
                .count());
            empStats.setPending(assignedFarmers.stream()
                .filter(f -> KycStatus.PENDING.equals(f.getKycStatus()))
                .count());
            empStats.setReferBack(assignedFarmers.stream()
                .filter(f -> KycStatus.REFER_BACK.equals(f.getKycStatus()))
                .count());
            empStats.setRejected(assignedFarmers.stream()
                .filter(f -> KycStatus.REJECTED.equals(f.getKycStatus()))
                .count());
            
            stats.add(empStats);
        }
        
        return stats;
    }
    
    // Todo Items
    public TodoItemsDTO getTodoItems() {
        TodoItemsDTO todo = new TodoItemsDTO();
        todo.setUnassignedFarmers(userRepository.countByRoleAndAssignedEmployeeIdIsNull(Role.FARMER));
        
        // Overdue KYC (pending for 3+ days)
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        todo.setOverdueKyc(userRepository.countByKycStatusAndCreatedAtBefore(KycStatus.PENDING, threeDaysAgo));
        
        // Employees with high pending (more than 5 pending)
        todo.setEmployeesWithHighPending(userRepository.countEmployeesWithHighPending(Role.EMPLOYEE, KycStatus.PENDING, 5L));
        
        return todo;
    }
    
    // Assign Farmers to Employees
    @Transactional
    public void assignFarmersToEmployee(AssignmentRequestDTO request) {
        List<User> farmers = userRepository.findAllById(request.getFarmerIds());
        User employee = userRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        for (User farmer : farmers) {
            farmer.setAssignedEmployeeId(employee.getId());
            userRepository.save(farmer);
        }
    }
} 