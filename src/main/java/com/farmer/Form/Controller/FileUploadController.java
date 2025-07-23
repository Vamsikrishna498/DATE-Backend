package com.farmer.Form.Controller;

import com.farmer.Form.DTO.FileUploadResponse;
import com.farmer.Form.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload/{directory}")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, 
                                      @PathVariable String directory) {
        try {
            String fileName = fileStorageService.storeFile(file, directory);
            String fileUrl = fileStorageService.getFileUrl(fileName, directory);
            
            FileUploadResponse response = new FileUploadResponse(
                fileName, 
                fileUrl, 
                file.getContentType(), 
                file.getSize()
            );
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File upload failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/download/{directory}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String directory, 
                                               @PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName, directory);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + fileName + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/view/{directory}/{fileName:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String directory, 
                                           @PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName, directory);
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Adjust based on file type
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{directory}/{fileName:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String directory, 
                                      @PathVariable String fileName) {
        try {
            fileStorageService.deleteFile(fileName, directory);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            response.put("fileName", fileName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File deletion failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/exists/{directory}/{fileName:.+}")
    public ResponseEntity<?> checkFileExists(@PathVariable String directory, 
                                           @PathVariable String fileName) {
        boolean exists = fileStorageService.fileExists(fileName, directory);
        
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("fileName", fileName);
        response.put("directory", directory);
        
        return ResponseEntity.ok(response);
    }
} 