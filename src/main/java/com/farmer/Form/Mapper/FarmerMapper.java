package com.farmer.Form.Mapper;

import com.farmer.Form.DTO.FarmerDTO;
import com.farmer.Form.Entity.Farmer;

public class FarmerMapper {

    public static FarmerDTO toDTO(Farmer farmer) {
        FarmerDTO dto = new FarmerDTO();
        dto.setId(farmer.getId());
        dto.setFirstName(farmer.getFirstName());
        dto.setMiddleName(farmer.getMiddleName());
        dto.setLastName(farmer.getLastName());
        dto.setDateOfBirth(farmer.getDateOfBirth());
        dto.setGender(farmer.getGender());
        dto.setContactNumber(farmer.getContactNumber());
        dto.setAlternativeContactNumber(farmer.getAlternativeContactNumber());
        dto.setNationality(farmer.getNationality());
        dto.setCountry(farmer.getCountry());
        dto.setState(farmer.getState());
        dto.setDistrict(farmer.getDistrict());
        dto.setBlock(farmer.getBlock());
        dto.setVillage(farmer.getVillage());
        dto.setPincode(farmer.getPincode());
        dto.setEducation(farmer.getEducation());
        dto.setExperience(farmer.getExperience());
        dto.setCurrentCrop(farmer.getCurrentCrop());
        dto.setProposedCrop(farmer.getProposedCrop());
        dto.setBankName(farmer.getBankName());
        dto.setAccountNumber(farmer.getAccountNumber());
        dto.setBranchName(farmer.getBranchName());
        dto.setIfscCode(farmer.getIfscCode());
        dto.setDocumentType(farmer.getDocumentType());
        dto.setDocumentNumber(farmer.getDocumentNumber());
        return dto;
    }

    public static Farmer toEntity(FarmerDTO dto) {
        Farmer farmer = new Farmer();
        farmer.setId(dto.getId());
        farmer.setFirstName(dto.getFirstName());
        farmer.setMiddleName(dto.getMiddleName());
        farmer.setLastName(dto.getLastName());
        farmer.setDateOfBirth(dto.getDateOfBirth());
        farmer.setGender(dto.getGender());
        farmer.setContactNumber(dto.getContactNumber());
        farmer.setAlternativeContactNumber(dto.getAlternativeContactNumber());
        farmer.setNationality(dto.getNationality());
        farmer.setCountry(dto.getCountry());
        farmer.setState(dto.getState());
        farmer.setDistrict(dto.getDistrict());
        farmer.setBlock(dto.getBlock());
        farmer.setVillage(dto.getVillage());
        farmer.setPincode(dto.getPincode());
        farmer.setEducation(dto.getEducation());
        farmer.setExperience(dto.getExperience());
        farmer.setCurrentCrop(dto.getCurrentCrop());
        farmer.setProposedCrop(dto.getProposedCrop());
        farmer.setBankName(dto.getBankName());
        farmer.setAccountNumber(dto.getAccountNumber());
        farmer.setBranchName(dto.getBranchName());
        farmer.setIfscCode(dto.getIfscCode());
        farmer.setDocumentType(dto.getDocumentType());
        farmer.setDocumentNumber(dto.getDocumentNumber());
        return farmer;
    }
}
