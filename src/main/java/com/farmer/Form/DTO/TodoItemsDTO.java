package com.farmer.Form.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemsDTO {
    private Long unassignedFarmers;
    private Long overdueKyc;
    private Long employeesWithHighPending;
} 