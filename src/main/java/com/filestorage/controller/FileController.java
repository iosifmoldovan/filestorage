package com.filestorage.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;

import com.filestorage.model.BaseResponse;
import com.filestorage.model.BaseResponseMetadata;
import com.filestorage.model.GetFileResponse;
import com.filestorage.service.FileStorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * REST Controller for file storage operations.
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LogManager.getLogger(FileController.class);
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Handles file upload and stores it in structured storage.
     */
    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("FileController.uploadFile(): In... fileName={}", file.getOriginalFilename());
            String filePath = fileStorageService.saveFile(file);
            logger.info("FileController.uploadFile(): Out...");
            return ResponseEntity.ok(new BaseResponse<>(filePath));
        } catch (Exception e) {
            logger.error("FileController.uploadFile(): Error", e);
            throw e;
        }
    }

    /**
     * Updates an existing file with new content.
     */
    @PutMapping("/update/{fileName}")
    public ResponseEntity<BaseResponse<String>> updateFile(
            @PathVariable String fileName, @RequestParam("file") MultipartFile file) throws Exception {
        try {
            logger.info("FileController.updateFile(): In... fileName={}", fileName);
            String filePath = fileStorageService.updateFile(fileName, file);
            logger.info("FileController.updateFile(): Out...");
            return ResponseEntity.ok(new BaseResponse<>(filePath));
        } catch (Exception e) {
            logger.error("FileController.updateFile(): Error", e);
            throw e;
        }
    }

    /**
     * Serves a file as a downloadable resource.
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) throws Exception {
        try {
            logger.info("FileController.getFile(): In... fileName={}", fileName);
            Path filePath = fileStorageService.getFile(fileName);
            Resource fileResource = new UrlResource(filePath.toUri());

            if (!fileResource.exists() || !fileResource.isReadable()) {
                logger.debug("FileController.getFile(): Out... File not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            logger.info("FileController.getFile(): Out...");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(fileResource);
        } catch (Exception e) {
            logger.error("FileController.getFile(): Error", e);
            throw e;
        }
    }

    /**
     * Deletes a file from storage.
     */
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<BaseResponse<String>> deleteFile(@PathVariable String fileName) throws Exception {
        try {
            logger.info("FileController.deleteFile(): In... fileName={}", fileName);
            fileStorageService.deleteFile(fileName);
            logger.info("FileController.deleteFile(): Out...");
            return ResponseEntity.ok(new BaseResponse<>("File deleted: " + fileName));
        } catch (Exception e) {
            logger.error("FileController.deleteFile(): Error", e);
            throw e;
        }
    }

    /**
     * Lists all files matching a regex with pagination.
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponseMetadata<GetFileResponse>> listFiles(
            @RequestParam String regex,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("FileController.listFiles(): In... regex={}, page={}, size={}", regex, page, size);
            BaseResponseMetadata<GetFileResponse> baseResponseMetadata = fileStorageService.listFilesMatchingRegex(
                    regex,
                    page, size);
            logger.info("FileController.listFiles(): Out...");
            return ResponseEntity.ok(baseResponseMetadata);
        } catch (Exception e) {
            logger.error("FileController.listFiles(): Error", e);
            throw e;
        }
    }

    /**
     * Returns the total number of files stored.
     */
    @GetMapping("/count")
    public ResponseEntity<BaseResponse<Long>> countFiles() {
        try {
            logger.info("FileController.countFiles(): In...");
            long count = fileStorageService.countFiles();
            logger.info("FileController.countFiles(): Out...");
            return ResponseEntity.ok(new BaseResponse<>(count));
        } catch (Exception e) {
            logger.error("FileController.countFiles(): Error", e);
            throw e;
        }
    }
    
}