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
import java.time.LocalDateTime;
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
                                  MultipartFile aadhaar, MultipartFile soilTestCertificate,
                                  MultipartFile panFile, MultipartFile voterFile, MultipartFile ppbFile) {
        try {
            // First save the farmer to get the ID
            Farmer farmer = FarmerMapper.toEntity(dto, null, null, null, null, null, null, null, null);
            farmer.setCreatedAt(LocalDateTime.now());
            farmer.setUpdatedAt(LocalDateTime.now());
            
            Farmer savedFarmer = farmerRepository.save(farmer);
            Long farmerId = savedFarmer.getId();
            
            // Now handle file uploads with the farmer ID
            String photoFile = null;
            if (photo != null && !photo.isEmpty()) {
                String originalFilename = photo.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                photoFile = "farmer_" + farmerId + extension;
                fileStorageService.storeFileWithName(photo, "photos", photoFile);
            }
            
            String passbookFile = null;
            if (passbookPhoto != null && !passbookPhoto.isEmpty()) {
                String originalFilename = passbookPhoto.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                passbookFile = "passbook_" + farmerId + extension;
                fileStorageService.storeFileWithName(passbookPhoto, "passbooks", passbookFile);
            }
            
            String aadhaarFile = null;
            if (aadhaar != null && !aadhaar.isEmpty()) {
                String originalFilename = aadhaar.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                aadhaarFile = "aadhaar_" + farmerId + extension;
                fileStorageService.storeFileWithName(aadhaar, "documents", aadhaarFile);
            }
            
            String soilTestFile = null;
            if (soilTestCertificate != null && !soilTestCertificate.isEmpty()) {
                String originalFilename = soilTestCertificate.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                soilTestFile = "soil_" + farmerId + extension;
                fileStorageService.storeFileWithName(soilTestCertificate, "soil-certificates", soilTestFile);
            }
            
            String panFileName = null;
            if (panFile != null && !panFile.isEmpty()) {
                String originalFilename = panFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                panFileName = "pan_" + farmerId + extension;
                fileStorageService.storeFileWithName(panFile, "documents", panFileName);
            }
            
            String voterFileName = null;
            if (voterFile != null && !voterFile.isEmpty()) {
                String originalFilename = voterFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                voterFileName = "voter_" + farmerId + extension;
                fileStorageService.storeFileWithName(voterFile, "documents", voterFileName);
            }
            
            String voterIdFileName = voterFileName; // Same file for both fields
            String ppbFileName = null;
            if (ppbFile != null && !ppbFile.isEmpty()) {
                String originalFilename = ppbFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                ppbFileName = "ppb_" + farmerId + extension;
                fileStorageService.storeFileWithName(ppbFile, "documents", ppbFileName);
            }

            // Map alternativeType from alternativeRelationType if not set
            if (dto.getAlternativeType() == null && dto.getAlternativeRelationType() != null) {
                dto.setAlternativeType(dto.getAlternativeRelationType());
            }

            // Update the farmer with file names
            Farmer updatedFarmer = FarmerMapper.toEntity(dto, photoFile, passbookFile, aadhaarFile, soilTestFile, 
                                                panFileName, voterFileName, voterIdFileName, ppbFileName);
            updatedFarmer.setId(farmerId);
            updatedFarmer.setCreatedAt(savedFarmer.getCreatedAt());
            updatedFarmer.setUpdatedAt(LocalDateTime.now());
            
            Farmer finalSaved = farmerRepository.save(updatedFarmer);
            return FarmerMapper.toDto(finalSaved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded files", e);
        }
    }

    @Override
    public FarmerDTO getFarmerById(Long id) {
        return farmerRepository.findById(id)
                .map(FarmerMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
    }

    @Override
    public List<FarmerDTO> getAllFarmers() {
        return farmerRepository.findAll().stream()
                .map(FarmerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public FarmerDTO updateFarmer(Long id, FarmerDTO dto,
                                  MultipartFile photo, MultipartFile passbookPhoto,
                                  MultipartFile aadhaar, MultipartFile soilTestCertificate,
                                  MultipartFile panFile, MultipartFile voterFile, MultipartFile ppbFile) {

        Farmer existing = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        try {
            String photoFile = existing.getPhotoFileName();
            if (photo != null && !photo.isEmpty()) {
                String originalFilename = photo.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                photoFile = "farmer_" + id + extension;
                fileStorageService.storeFileWithName(photo, "photos", photoFile);
            }

            String passbookFile = existing.getPassbookFileName();
            if (passbookPhoto != null && !passbookPhoto.isEmpty()) {
                String originalFilename = passbookPhoto.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                passbookFile = "passbook_" + id + extension;
                fileStorageService.storeFileWithName(passbookPhoto, "passbooks", passbookFile);
            }

            String aadhaarFile = existing.getAadhaarFileName();
            if (aadhaar != null && !aadhaar.isEmpty()) {
                String originalFilename = aadhaar.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                aadhaarFile = "aadhaar_" + id + extension;
                fileStorageService.storeFileWithName(aadhaar, "documents", aadhaarFile);
            }

            String soilTestFile = existing.getSoilTestCertificateFileName();
            if (soilTestCertificate != null && !soilTestCertificate.isEmpty()) {
                String originalFilename = soilTestCertificate.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                soilTestFile = "soil_" + id + extension;
                fileStorageService.storeFileWithName(soilTestCertificate, "soil-certificates", soilTestFile);
            }

            String panFileName = existing.getPanFileName();
            if (panFile != null && !panFile.isEmpty()) {
                String originalFilename = panFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                panFileName = "pan_" + id + extension;
                fileStorageService.storeFileWithName(panFile, "documents", panFileName);
            }

            String voterFileName = existing.getVoterFileName();
            if (voterFile != null && !voterFile.isEmpty()) {
                String originalFilename = voterFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                voterFileName = "voter_" + id + extension;
                fileStorageService.storeFileWithName(voterFile, "documents", voterFileName);
            }

            String voterIdFileName = voterFileName; // Same file for both fields
            String ppbFileName = existing.getPpbFileName();
            if (ppbFile != null && !ppbFile.isEmpty()) {
                String originalFilename = ppbFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                ppbFileName = "ppb_" + id + extension;
                fileStorageService.storeFileWithName(ppbFile, "documents", ppbFileName);
            }

            Farmer updated = FarmerMapper.toEntity(dto, photoFile, passbookFile, aadhaarFile, soilTestFile,
                                                panFileName, voterFileName, voterIdFileName, ppbFileName);
            updated.setId(existing.getId());
            updated.setCreatedAt(existing.getCreatedAt());
            updated.setUpdatedAt(LocalDateTime.now());

            Farmer saved = farmerRepository.save(updated);
            return FarmerMapper.toDto(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to update files", e);
        }
    }

    @Override
    public FarmerDTO updateFarmer(Long id, FarmerDTO dto) {
        Farmer existing = farmerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        Farmer updated = FarmerMapper.toEntity(dto,
                existing.getPhotoFileName(),
                existing.getPassbookFileName(),
                existing.getAadhaarFileName(),
                existing.getSoilTestCertificateFileName(),
                existing.getPanFileName(),
                existing.getVoterFileName(),
                existing.getVoterIdFileName(),
                existing.getPpbFileName());

        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        
        return FarmerMapper.toDto(farmerRepository.save(updated));
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
        farmer.setCreatedAt(LocalDateTime.now());
        farmer.setUpdatedAt(LocalDateTime.now());
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
        farmer.setUpdatedAt(LocalDateTime.now());
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
        farmer.setKycReviewedDate(LocalDateTime.now().toLocalDate());
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
        farmer.setKycReviewedDate(LocalDateTime.now().toLocalDate());
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
        farmer.setKycReviewedDate(LocalDateTime.now().toLocalDate());
        farmer.setKycReviewedBy(employeeEmail);
        farmer.setKycRejectionReason(reason);
        farmer.setKycReferBackReason(null);
        
        farmerRepository.save(farmer);
    }
}
