package com.example.zhukov.cloudbox.controller;

import com.example.zhukov.cloudbox.dto.file.ResourceResponse;
import com.example.zhukov.cloudbox.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/resource")
    public ResponseEntity<ResourceResponse> uploadFile(
            @RequestParam(value = "path", required = false) String path,
            @RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ResourceResponse response = fileService.upload(authentication.getName(), path, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/directory")
    public ResponseEntity<List<ResourceResponse>> getDirectory(
            @RequestParam(value = "path", required = false) String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var result = fileService.listDirectory(authentication.getName(), path);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var inputStream = fileService.downloadFile(authentication.getName(), path);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        var lastSlashIndex = path.lastIndexOf("/");
        String fileName = (lastSlashIndex == -1) ? path : path.substring(lastSlashIndex + 1);
        return ResponseEntity.ok()
                .header("Content-Type", "application/octet-stream")
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(inputStreamResource);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<?> deleteFile(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        fileService.deleteFile(authentication.getName(), path);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/directory")
    public ResponseEntity<ResourceResponse> uploadDirectory(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.createDirectory(authentication.getName(), path));
    }

    @GetMapping("/resource")
    public ResponseEntity<ResourceResponse> getResource(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(fileService.getResourceInfo(authentication.getName(), path));
    }

    @GetMapping("/resource/search")
    public ResponseEntity<List<ResourceResponse>> searchResource(@RequestParam String query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(fileService.searchFile(authentication.getName(), query));
    }
}
