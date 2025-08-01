package com.farmer.Form.DTO;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private String gender;
    private String role;
    private String dateOfBirth;
    private String email;
    private String status;
    private boolean forcePasswordChange;

    public static UserResponseDTO fromEntity(com.farmer.Form.Entity.User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setGender(user.getGender());
        dto.setRole(user.getRole().name());
        dto.setDateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        dto.setForcePasswordChange(user.isForcePasswordChange());
        return dto;
    }
}
 
