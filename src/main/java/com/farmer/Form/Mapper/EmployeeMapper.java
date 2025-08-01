package com.farmer.Form.Mapper;
 
import com.farmer.Form.DTO.EmployeeDTO;
import com.farmer.Form.Entity.Employee;
 
public class EmployeeMapper {
    public static Employee toEntity(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setSalutation(dto.getSalutation());
        employee.setFirstName(dto.getFirstName());
        employee.setMiddleName(dto.getMiddleName());
        employee.setLastName(dto.getLastName());
        employee.setGender(dto.getGender());
        employee.setNationality(dto.getNationality());
        employee.setDob(dto.getDob());
        employee.setContactNumber(dto.getContactNumber());
        employee.setEmail(dto.getEmail());
        employee.setRelationType(dto.getRelationType());
        employee.setRelationName(dto.getRelationName());
        employee.setAltNumber(dto.getAltNumber());
        employee.setAltNumberType(dto.getAltNumberType());
        employee.setCountry(dto.getCountry());
        employee.setState(dto.getState());
        employee.setDistrict(dto.getDistrict());
        employee.setBlock(dto.getBlock());
        employee.setVillage(dto.getVillage());
        employee.setZipcode(dto.getZipcode());
        employee.setSector(dto.getSector());
        employee.setEducation(dto.getEducation());
        employee.setExperience(dto.getExperience());
        employee.setBankName(dto.getBankName());
        employee.setAccountNumber(dto.getAccountNumber());
        employee.setBranchName(dto.getBranchName());
        employee.setIfscCode(dto.getIfscCode());
        employee.setDocumentType(dto.getDocumentType());
        employee.setDocumentNumber(dto.getDocumentNumber());
        employee.setRole(dto.getRole());
        employee.setAccessStatus(dto.getAccessStatus());
        return employee;
    }
}
 
 