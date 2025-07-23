package com.farmer.Form.Mapper;

import com.farmer.Form.DTO.FarmerDTO;
import com.farmer.Form.Entity.Farmer;

import java.time.LocalDate;

public class FarmerMapper {

    public static FarmerDTO toDto(Farmer farmer) {
        if (farmer == null) return null;

        return FarmerDTO.builder()
                .id(farmer.getId())
                .photoFileName(farmer.getPhotoFileName())
                .photoUrl(farmer.getPhotoUrl())
                .passbookFileName(farmer.getPassbookFileName())
                .soilTestCertificateFileName(farmer.getSoilTestCertificateFileName())
                .aadhaarFileName(farmer.getAadhaarFileName())
                .panFileName(farmer.getPanFileName())
                .voterFileName(farmer.getVoterFileName())
                .voterIdFileName(farmer.getVoterIdFileName())
                .ppbFileName(farmer.getPpbFileName())
                
                .firstName(farmer.getFirstName())
                .middleName(farmer.getMiddleName())
                .lastName(farmer.getLastName())
                .gender(farmer.getGender())
                .salutation(farmer.getSalutation())
                .nationality(farmer.getNationality())
                .dateOfBirth(farmer.getDateOfBirth() != null ? farmer.getDateOfBirth().toString() : null)
                .fatherName(farmer.getFatherName())
                .contactNumber(farmer.getContactNumber())
                .alternativeContactNumber(farmer.getAlternativeContactNumber())
                .alternativeRelationType(farmer.getAlternativeRelationType())
                .alternativeType(farmer.getAlternativeType())
                .alternativeNumber(farmer.getAlternativeNumber())
                
                .country(farmer.getCountry())
                .state(farmer.getState())
                .district(farmer.getDistrict())
                .block(farmer.getBlock())
                .mandal(farmer.getMandal())
                .village(farmer.getVillage())
                .pincode(farmer.getPincode())
                
                .education(farmer.getEducation())
                .experience(farmer.getExperience())
                
                .surveyNumber(farmer.getSurveyNumber())
                .totalLandHolding(farmer.getTotalLandHolding())
                .geoTag(farmer.getGeoTag())
                .selectCrop(farmer.getSelectCrop())
                .cropName(farmer.getCropName())
                .cropCategory(farmer.getCropCategory())
                .netIncome(farmer.getNetIncome())
                .soilTest(farmer.getSoilTest())
                
                .cropType(farmer.getCropType())
                
                .waterSource(farmer.getWaterSource())
                .borewellDischarge(farmer.getBorewellDischarge())
                .summerDischarge(farmer.getSummerDischarge())
                .borewellLocation(farmer.getBorewellLocation())
                
                .bankName(farmer.getBankName())
                .accountNumber(farmer.getAccountNumber())
                .branchName(farmer.getBranchName())
                .ifscCode(farmer.getIfscCode())
                
                .aadharNumber(farmer.getAadharNumber())
                .panNumber(farmer.getPanNumber())
                .voterId(farmer.getVoterId())
                .ppbNumber(farmer.getPpbNumber())
                .build();
    }

    public static Farmer toEntity(FarmerDTO dto, String photoFileName, String passbookFileName, 
                                String aadhaarFileName, String soilTestCertificateFileName,
                                String panFileName, String voterFileName, String voterIdFileName, String ppbFileName) {
        if (dto == null) return null;

        return Farmer.builder()
                .id(dto.getId())
                .photoFileName(photoFileName)
                .photoUrl(photoFileName != null ? "/uploads/" + photoFileName : null)
                .passbookFileName(passbookFileName)
                .soilTestCertificateFileName(soilTestCertificateFileName)
                .aadhaarFileName(aadhaarFileName)
                .panFileName(panFileName)
                .voterFileName(voterFileName)
                .voterIdFileName(voterIdFileName)
                .ppbFileName(ppbFileName)
                
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .gender(dto.getGender())
                .salutation(dto.getSalutation())
                .nationality(dto.getNationality())
                .dateOfBirth(dto.getDateOfBirth() != null ? LocalDate.parse(dto.getDateOfBirth()) : null)
                .fatherName(dto.getFatherName())
                .contactNumber(dto.getContactNumber())
                .alternativeContactNumber(dto.getAlternativeContactNumber())
                .alternativeRelationType(dto.getAlternativeRelationType())
                .alternativeType(dto.getAlternativeType())
                .alternativeNumber(dto.getAlternativeNumber())
                
                .country(dto.getCountry())
                .state(dto.getState())
                .district(dto.getDistrict())
                .block(dto.getBlock())
                .mandal(dto.getMandal())
                .village(dto.getVillage())
                .pincode(dto.getPincode())
                
                .education(dto.getEducation())
                .experience(dto.getExperience())
                
                .surveyNumber(dto.getSurveyNumber())
                .totalLandHolding(dto.getTotalLandHolding())
                .geoTag(dto.getGeoTag())
                .selectCrop(dto.getSelectCrop())
                .cropName(dto.getCropName())
                .cropCategory(dto.getCropCategory())
                .netIncome(dto.getNetIncome())
                .soilTest(dto.getSoilTest())
                
                .cropType(dto.getCropType())
                
                .waterSource(dto.getWaterSource())
                .borewellDischarge(dto.getBorewellDischarge())
                .summerDischarge(dto.getSummerDischarge())
                .borewellLocation(dto.getBorewellLocation())
                
                .bankName(dto.getBankName())
                .accountNumber(dto.getAccountNumber())
                .branchName(dto.getBranchName())
                .ifscCode(dto.getIfscCode())
                
                .aadharNumber(dto.getAadharNumber())
                .panNumber(dto.getPanNumber())
                .voterId(dto.getVoterId())
                .ppbNumber(dto.getPpbNumber())
                .build();
    }
}