package com.farmer.Form.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmer.Form.DTO.FarmerDTO;
import com.farmer.Form.Entity.Farmer;
import com.farmer.Form.Service.FarmerService;
import com.farmer.Form.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/farmers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FarmerController {
    
    private final FarmerService farmerService;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createFarmer(
            @RequestParam("farmerDto") MultipartFile farmerDtoFile,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "soilTestCertificate", required = false) MultipartFile soilTestCertificate,
            @RequestParam(value = "aadhaarFile", required = false) MultipartFile aadhaarFile,
            @RequestParam(value = "panFile", required = false) MultipartFile panFile,
            @RequestParam(value = "voterFile", required = false) MultipartFile voterFile,
            @RequestParam(value = "ppbFile", required = false) MultipartFile ppbFile,
            @RequestParam(value = "passbookFile", required = false) MultipartFile passbookFile) {
        
        try {
            // Read the JSON content from the file
            String farmerDtoJson = new String(farmerDtoFile.getBytes());
            FarmerDTO farmerDTO = objectMapper.readValue(farmerDtoJson, FarmerDTO.class);
            
            FarmerDTO createdFarmer = farmerService.createFarmer(farmerDTO, photo, passbookFile, 
                aadhaarFile, soilTestCertificate, panFile, voterFile, ppbFile);
            
            return ResponseEntity.ok(createdFarmer);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Error parsing farmer data: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error reading farmer data file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating farmer: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getFarmerById(@PathVariable Long id) {
        try {
            FarmerDTO farmer = farmerService.getFarmerById(id);
            return ResponseEntity.ok(farmer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching farmer: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllFarmers() {
        try {
            List<FarmerDTO> farmers = farmerService.getAllFarmers();
            return ResponseEntity.ok(farmers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching farmers: " + e.getMessage());
        }
    }
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateFarmer(
            @PathVariable Long id,
            @RequestParam("farmerDto") MultipartFile farmerDtoFile,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "soilTestCertificate", required = false) MultipartFile soilTestCertificate,
            @RequestParam(value = "aadhaarFile", required = false) MultipartFile aadhaarFile,
            @RequestParam(value = "panFile", required = false) MultipartFile panFile,
            @RequestParam(value = "voterFile", required = false) MultipartFile voterFile,
            @RequestParam(value = "ppbFile", required = false) MultipartFile ppbFile,
            @RequestParam(value = "passbookFile", required = false) MultipartFile passbookFile) {
        
        try {
            // Read the JSON content from the file
            String farmerDtoJson = new String(farmerDtoFile.getBytes());
            FarmerDTO farmerDTO = objectMapper.readValue(farmerDtoJson, FarmerDTO.class);
            farmerDTO.setId(id);
            
            FarmerDTO updatedFarmer = farmerService.updateFarmer(id, farmerDTO, photo, passbookFile, 
                aadhaarFile, soilTestCertificate, panFile, voterFile, ppbFile);
            
            return ResponseEntity.ok(updatedFarmer);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Error parsing farmer data: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error reading farmer data file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating farmer: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFarmer(@PathVariable Long id) {
        try {
            farmerService.deleteFarmer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting farmer: " + e.getMessage());
        }
    }
    
    // ✅ NEW ENDPOINT TO SERVE FILES
    @GetMapping("/files/{subDirectory}/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String subDirectory, @PathVariable String fileName) {
        try {
            Resource file = fileStorageService.loadFileAsResource(fileName, subDirectory);
            String contentType = Files.probeContentType(file.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ✅ SIMPLE FILE SERVING ENDPOINT
    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> serveUploadedFile(@PathVariable String fileName) {
        try {
            // Try to find the file in any subdirectory
            String[] subDirectories = {"photos", "passbooks", "documents", "soil-certificates"};
            Resource file = null;
            
            for (String subDir : subDirectories) {
                try {
                    file = fileStorageService.loadFileAsResource(fileName, subDir);
                    if (file.exists()) {
                        break;
                    }
                } catch (Exception e) {
                    // Continue to next directory
                }
            }
            
            if (file != null && file.exists()) {
                String contentType = Files.probeContentType(file.getFile().toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                        .body(file);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ✅ ENDPOINT TO UPDATE EXISTING FARMER DATA
    @PutMapping("/{id}/update-missing-fields")
    public ResponseEntity<?> updateMissingFields(@PathVariable Long id) {
        try {
            FarmerDTO farmer = farmerService.getFarmerById(id);
            
            // Update missing fields based on existing data
            if (farmer.getAlternativeType() == null && farmer.getAlternativeRelationType() != null) {
                farmer.setAlternativeType(farmer.getAlternativeRelationType());
            }
            
            if (farmer.getAlternativeNumber() == null && farmer.getAlternativeContactNumber() != null) {
                farmer.setAlternativeNumber(farmer.getAlternativeContactNumber());
            }
            
            // Update the farmer with the new data
            FarmerDTO updatedFarmer = farmerService.updateFarmer(id, farmer);
            return ResponseEntity.ok(updatedFarmer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating farmer: " + e.getMessage());
        }
    }
    
    // ✅ SIMPLE ENDPOINT TO UPDATE ALTERNATIVE TYPE
    @PutMapping("/{id}/fix-alternative-type")
    public ResponseEntity<?> fixAlternativeType(@PathVariable Long id) {
        try {
            FarmerDTO farmer = farmerService.getFarmerById(id);
            farmer.setAlternativeType(farmer.getAlternativeRelationType());
            farmer.setAlternativeNumber(farmer.getAlternativeContactNumber());
            
            FarmerDTO updatedFarmer = farmerService.updateFarmer(id, farmer);
            return ResponseEntity.ok(updatedFarmer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating farmer: " + e.getMessage());
        }
    }
}