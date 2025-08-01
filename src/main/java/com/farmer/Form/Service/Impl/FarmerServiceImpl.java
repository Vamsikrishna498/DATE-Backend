package com.farmer.Form.Service.Impl;

import com.farmer.Form.DTO.FarmerDTO;
import com.farmer.Form.Entity.Farmer;
import com.farmer.Form.Mapper.FarmerMapper;
import com.farmer.Form.Repository.FarmerRepository;
import com.farmer.Form.Service.FarmerService;
import com.farmer.Form.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FarmerServiceImpl implements FarmerService {

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private com.farmer.Form.Repository.EmployeeRepository employeeRepository;

    @Override
    public FarmerDTO createFarmer(FarmerDTO dto, MultipartFile photo, MultipartFile passbookPhoto,
                                  MultipartFile aadhaar, MultipartFile soilTestCertificate) {
        try {
            String photoFile = (photo != null && !photo.isEmpty())
                    ? fileStorageService.storeFile(photo, "photos") : null;

            String passbookFile = (passbookPhoto != null && !passbookPhoto.isEmpty())
                    ? fileStorageService.storeFile(passbookPhoto, "passbooks") : null;

            String aadhaarFile = (aadhaar != null && !aadhaar.isEmpty())
                    ? fileStorageService.storeFile(aadhaar, "documents") : null;

            String soilTestFile = (soilTestCertificate != null && !soilTestCertificate.isEmpty())
                    ? fileStorageService.storeFile(soilTestCertificate, "soil-tests") : null;

            Farmer farmer = FarmerMapper.toEntity(dto);
            farmer.setPhotoFileName(photoFile);
            farmer.setPassbookFileName(passbookFile);
            farmer.setDocumentFileName(aadhaarFile);
            farmer.setSoilTestCertificateFileName(soilTestFile);
            
            Farmer saved = farmerRepository.save(farmer);
            return FarmerMapper.toDTO(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded files", e);
        }
    }

    @Override
    public FarmerDTO getFarmerById(Long id) {
        return farmerRepository.findById(id)
                .map(FarmerMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
    }

    @Override
    public List<FarmerDTO> getAllFarmers() {
        return farmerRepository.findAll().stream()
                .map(FarmerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FarmerDTO updateFarmer(Long id, FarmerDTO dto,
                                  MultipartFile photo, MultipartFile passbookPhoto,
                                  MultipartFile aadhaar, MultipartFile soilTestCertificate) {

        Farmer existing = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        try {
            String photoFile = (photo != null && !photo.isEmpty())
                    ? fileStorageService.storeFile(photo, "photos")
                    : existing.getPhotoFileName();

            String passbookFile = (passbookPhoto != null && !passbookPhoto.isEmpty())
                    ? fileStorageService.storeFile(passbookPhoto, "passbooks")
                    : existing.getPassbookFileName();

            String aadhaarFile = (aadhaar != null && !aadhaar.isEmpty())
                    ? fileStorageService.storeFile(aadhaar, "documents")
                    : existing.getDocumentFileName();

            String soilTestFile = (soilTestCertificate != null && !soilTestCertificate.isEmpty())
                    ? fileStorageService.storeFile(soilTestCertificate, "soil-tests")
                    : existing.getSoilTestCertificateFileName();

            Farmer updated = FarmerMapper.toEntity(dto);
            updated.setId(existing.getId());
            updated.setPhotoFileName(photoFile);
            updated.setPassbookFileName(passbookFile);
            updated.setDocumentFileName(aadhaarFile);
            updated.setSoilTestCertificateFileName(soilTestFile);

            Farmer saved = farmerRepository.save(updated);
            return FarmerMapper.toDTO(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to update files", e);
        }
    }

    @Override
    public FarmerDTO updateFarmer(Long id, FarmerDTO dto) {
        Farmer existing = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        Farmer updated = FarmerMapper.toEntity(dto);
        updated.setId(existing.getId());
        updated.setPhotoFileName(existing.getPhotoFileName());
        updated.setPassbookFileName(existing.getPassbookFileName());
        updated.setDocumentFileName(existing.getDocumentFileName());
        updated.setSoilTestCertificateFileName(existing.getSoilTestCertificateFileName());

        return FarmerMapper.toDTO(farmerRepository.save(updated));
    }

    @Override
    public void deleteFarmer(Long id) {
        farmerRepository.deleteById(id);
    }

    @Override
    public long getFarmerCount() {
        return farmerRepository.count();
    }

    // --- SUPER ADMIN RAW CRUD ---
    @Override
    public List<Farmer> getAllFarmersRaw() {
        return farmerRepository.findAll();
    }

    @Override
    public Farmer getFarmerRawById(Long id) {
        return farmerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Farmer not found with ID: " + id));
    }

    @Override
    public Farmer createFarmerBySuperAdmin(Farmer farmer) {
        return farmerRepository.save(farmer);
    }

    @Override
    public Farmer updateFarmerBySuperAdmin(Long id, Farmer updatedFarmer) {
        Farmer farmer = getFarmerRawById(id);
        // Update fields as needed
        farmer.setFirstName(updatedFarmer.getFirstName());
        farmer.setLastName(updatedFarmer.getLastName());
        farmer.setDateOfBirth(updatedFarmer.getDateOfBirth());
        farmer.setGender(updatedFarmer.getGender());
        farmer.setContactNumber(updatedFarmer.getContactNumber());
        farmer.setCountry(updatedFarmer.getCountry());
        farmer.setState(updatedFarmer.getState());
        farmer.setDistrict(updatedFarmer.getDistrict());
        farmer.setBlock(updatedFarmer.getBlock());
        farmer.setVillage(updatedFarmer.getVillage());
        farmer.setPincode(updatedFarmer.getPincode());
        // ... update other fields as needed
        return farmerRepository.save(farmer);
    }

    @Override
    public void deleteFarmerBySuperAdmin(Long id) {
        farmerRepository.deleteById(id);
    }

    @Override
    public void assignFarmerToEmployee(Long farmerId, Long employeeId) {
        Farmer farmer = farmerRepository.findById(farmerId)
            .orElseThrow(() -> new RuntimeException("Farmer not found"));
        com.farmer.Form.Entity.Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        farmer.setAssignedEmployee(employee);
        farmerRepository.save(farmer);
    }

    @Override
    public List<Farmer> getFarmersByEmployeeEmail(String email) {
        return farmerRepository.findByAssignedEmployee_Email(email);
    }

    @Override
    public void approveKyc(Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
            .orElseThrow(() -> new RuntimeException("Farmer not found"));
        farmer.setKycApproved(true);
        farmerRepository.save(farmer);
    }
    
    // Enhanced KYC Management Methods
    @Override
    public void approveKycByEmployee(Long farmerId, String employeeEmail) {
        Farmer farmer = farmerRepository.findById(farmerId)
            .orElseThrow(() -> new RuntimeException("Farmer not found"));
        
        // Verify the farmer is assigned to this employee
        if (farmer.getAssignedEmployee() == null || !farmer.getAssignedEmployee().getEmail().equals(employeeEmail)) {
            throw new RuntimeException("Farmer is not assigned to this employee");
        }
        
        farmer.setKycStatus(Farmer.KycStatus.APPROVED);
        farmer.setKycApproved(true);
        farmer.setKycReviewedDate(LocalDate.now());
        farmer.setKycReviewedBy(employeeEmail);
        farmer.setKycRejectionReason(null);
        farmer.setKycReferBackReason(null);
        
        farmerRepository.save(farmer);
    }
    
    @Override
    public void referBackKyc(Long farmerId, String employeeEmail, String reason) {
        Farmer farmer = farmerRepository.findById(farmerId)
            .orElseThrow(() -> new RuntimeException("Farmer not found"));
        
        // Verify the farmer is assigned to this employee
        if (farmer.getAssignedEmployee() == null || !farmer.getAssignedEmployee().getEmail().equals(employeeEmail)) {
            throw new RuntimeException("Farmer is not assigned to this employee");
        }
        
        farmer.setKycStatus(Farmer.KycStatus.REFER_BACK);
        farmer.setKycApproved(false);
        farmer.setKycReviewedDate(LocalDate.now());
        farmer.setKycReviewedBy(employeeEmail);
        farmer.setKycReferBackReason(reason);
        farmer.setKycRejectionReason(null);
        
        farmerRepository.save(farmer);
    }
    
    @Override
    public void rejectKyc(Long farmerId, String employeeEmail, String reason) {
        Farmer farmer = farmerRepository.findById(farmerId)
            .orElseThrow(() -> new RuntimeException("Farmer not found"));
        
        // Verify the farmer is assigned to this employee
        if (farmer.getAssignedEmployee() == null || !farmer.getAssignedEmployee().getEmail().equals(employeeEmail)) {
            throw new RuntimeException("Farmer is not assigned to this employee");
        }
        
        farmer.setKycStatus(Farmer.KycStatus.REJECTED);
        farmer.setKycApproved(false);
        farmer.setKycReviewedDate(LocalDate.now());
        farmer.setKycReviewedBy(employeeEmail);
        farmer.setKycRejectionReason(reason);
        farmer.setKycReferBackReason(null);
        
        farmerRepository.save(farmer);
    }
}
